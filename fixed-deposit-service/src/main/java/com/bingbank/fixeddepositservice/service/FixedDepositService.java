package com.bingbank.fixeddepositservice.service;

import com.bingbank.fixeddepositservice.dto.*;
import com.bingbank.fixeddepositservice.model.FixedDeposit;
import com.bingbank.fixeddepositservice.repository.FixedDepositRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FixedDepositService {

    @Autowired
    private FixedDepositRepository fdRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${app.fd.interest-rate}")
    private Double interestRate;

    /**
     * Get all active FDs for a customer
     */
    public List<FixedDepositDTO> getActiveFDs(Long customerId) {
        System.out.println("FDService: Fetching active FDs for customer: " + customerId);
        List<FixedDeposit> fds = fdRepository.findByCustomerIdAndStatus(customerId, "ACTIVE");
        
        return fds.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all FDs (active and closed) for a customer
     */
    public List<FixedDepositDTO> getAllFDs(Long customerId) {
        System.out.println("FDService: Fetching all FDs for customer: " + customerId);
        List<FixedDeposit> fds = fdRepository.findByCustomerId(customerId);
        
        return fds.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Create a new FD
     */
    @Transactional
    public FixedDepositDTO createFD(CreateFDRequest request, String authHeader) {
        System.out.println("FDService: Creating FD for customer: " + request.getCustomerId());
        
        // Validate minimum amount
        if (request.getPrincipalAmount().compareTo(BigDecimal.valueOf(100)) < 0) {
            throw new RuntimeException("Minimum FD amount is $100");
        }
        
        // Verify account balance
        if (!verifyAccountBalance(request.getAccountNumber(), request.getPrincipalAmount(), authHeader)) {
            throw new RuntimeException("Insufficient balance in account");
        }
        
        // Calculate maturity details
        LocalDate startDate = LocalDate.now();
        LocalDate maturityDate = startDate.plusYears(request.getTenureYears());
        BigDecimal maturityAmount = calculateMaturityAmount(
                request.getPrincipalAmount(), 
                request.getTenureYears()
        );
        
        // Create FD
        FixedDeposit fd = new FixedDeposit();
        fd.setCustomerId(request.getCustomerId());
        fd.setAccountNumber(request.getAccountNumber());
        fd.setPrincipalAmount(request.getPrincipalAmount());
        fd.setInterestRate(BigDecimal.valueOf(interestRate));
        fd.setTenureYears(request.getTenureYears());
        fd.setStartDate(startDate);
        fd.setMaturityDate(maturityDate);
        fd.setMaturityAmount(maturityAmount);
        fd.setCurrentValue(request.getPrincipalAmount());
        fd.setStatus("ACTIVE");
        
        FixedDeposit savedFD = fdRepository.save(fd);
        System.out.println("FDService: FD created with ID: " + savedFD.getFdId());
        
        // Debit from account
        debitFromAccount(request.getAccountNumber(), request.getPrincipalAmount(), savedFD.getFdId(), authHeader);
        
        // Create transaction entry
        createTransaction(request.getAccountNumber(), request.getPrincipalAmount(), "DEBIT", 
                "FD Opening - FD#" + savedFD.getFdId(), authHeader);
        
        return mapToDTO(savedFD);
    }

    /**
     * Close an existing FD
     */
    @Transactional
    public FixedDepositDTO closeFD(CloseFDRequest request, String authHeader) {
        System.out.println("FDService: Closing FD: " + request.getFdId());
        
        FixedDeposit fd = fdRepository.findById(request.getFdId())
                .orElseThrow(() -> new RuntimeException("FD not found"));
        
        if (!fd.getCustomerId().equals(request.getCustomerId())) {
            throw new RuntimeException("Unauthorized to close this FD");
        }
        
        if (!"ACTIVE".equals(fd.getStatus())) {
            throw new RuntimeException("FD is already closed");
        }
        
        // Calculate current value
        BigDecimal currentValue = calculateCurrentValue(fd);
        
        // Update FD status
        fd.setStatus("CLOSED");
        fd.setCurrentValue(currentValue);
        fd.setClosedAt(LocalDateTime.now());
        
        FixedDeposit closedFD = fdRepository.save(fd);
        System.out.println("FDService: FD closed with current value: " + currentValue);
        
        // Credit to account
        creditToAccount(fd.getAccountNumber(), currentValue, fd.getFdId(), authHeader);
        
        // Create transaction entry
        createTransaction(fd.getAccountNumber(), currentValue, "CREDIT", 
                "FD Closing - FD#" + fd.getFdId(), authHeader);
        
        return mapToDTO(closedFD);
    }

    /**
     * Get growth data for FD tracking
     */
    public GrowthDataDTO getGrowthData(Long fdId) {
        System.out.println("FDService: Fetching growth data for FD: " + fdId);
        
        FixedDeposit fd = fdRepository.findById(fdId)
                .orElseThrow(() -> new RuntimeException("FD not found"));
        
        GrowthDataDTO growthData = new GrowthDataDTO();
        growthData.setFdId(fd.getFdId());
        growthData.setPrincipalAmount(fd.getPrincipalAmount());
        growthData.setCurrentValue(calculateCurrentValue(fd));
        growthData.setMaturityAmount(fd.getMaturityAmount());
        growthData.setStartDate(fd.getStartDate());
        growthData.setMaturityDate(fd.getMaturityDate());
        growthData.setTenureYears(fd.getTenureYears());
        growthData.setInterestRate(fd.getInterestRate());
        
        // Generate growth points based on tenure
        List<GrowthDataDTO.DataPoint> growthPoints = new ArrayList<>();
        LocalDate currentDate = fd.getStartDate();
        LocalDate endDate = "ACTIVE".equals(fd.getStatus()) ? 
                (LocalDate.now().isBefore(fd.getMaturityDate()) ? LocalDate.now() : fd.getMaturityDate()) 
                : fd.getMaturityDate();
        
        // Determine interval based on tenure
        int monthsInterval;
        if (fd.getTenureYears() == 1) {
            monthsInterval = 3; // Every 3 months for 1 year
        } else if (fd.getTenureYears() <= 3) {
            monthsInterval = 6; // Every 6 months for 2-3 years
        } else if (fd.getTenureYears() <= 5) {
            monthsInterval = 12; // Every 1 year for 4-5 years
        } else {
            monthsInterval = 24; // Every 2 years for 6+ years
        }
        
        // Add start point
        growthPoints.add(new GrowthDataDTO.DataPoint(
                fd.getStartDate(),
                fd.getPrincipalAmount(),
                0
        ));
        
        // Add interval points
        while (currentDate.isBefore(endDate)) {
            currentDate = currentDate.plusMonths(monthsInterval);
            
            // Don't go beyond end date
            if (currentDate.isAfter(endDate)) {
                break;
            }
            
            long daysFromStart = ChronoUnit.DAYS.between(fd.getStartDate(), currentDate);
            BigDecimal value = calculateValueAtDate(fd, currentDate);
            
            growthPoints.add(new GrowthDataDTO.DataPoint(
                    currentDate,
                    value,
                    (int) daysFromStart
            ));
        }
        
        // Add current date point if FD is active and not already added
        if ("ACTIVE".equals(fd.getStatus()) && !currentDate.equals(LocalDate.now())) {
            LocalDate today = LocalDate.now();
            if (today.isAfter(fd.getStartDate()) && today.isBefore(fd.getMaturityDate())) {
                long daysFromStart = ChronoUnit.DAYS.between(fd.getStartDate(), today);
                growthPoints.add(new GrowthDataDTO.DataPoint(
                        today,
                        calculateCurrentValue(fd),
                        (int) daysFromStart
                ));
            }
        }
        
        // Add maturity point
        long daysFromStart = ChronoUnit.DAYS.between(fd.getStartDate(), fd.getMaturityDate());
        growthPoints.add(new GrowthDataDTO.DataPoint(
                fd.getMaturityDate(),
                fd.getMaturityAmount(),
                (int) daysFromStart
        ));
        
        growthData.setGrowthPoints(growthPoints);
        
        return growthData;
    }

    /**
     * Calculate maturity amount using compound interest
     * A = P(1 + r)^t
     */
    private BigDecimal calculateMaturityAmount(BigDecimal principal, Integer years) {
        double p = principal.doubleValue();
        double r = interestRate;
        int t = years;
        
        double amount = p * Math.pow(1 + r, t);
        
        return BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate current value of FD
     */
    private BigDecimal calculateCurrentValue(FixedDeposit fd) {
        if ("CLOSED".equals(fd.getStatus())) {
            return fd.getCurrentValue();
        }
        
        return calculateValueAtDate(fd, LocalDate.now());
    }

    /**
     * Calculate value at a specific date
     */
    private BigDecimal calculateValueAtDate(FixedDeposit fd, LocalDate date) {
        long totalDays = ChronoUnit.DAYS.between(fd.getStartDate(), fd.getMaturityDate());
        long elapsedDays = ChronoUnit.DAYS.between(fd.getStartDate(), date);
        
        if (elapsedDays <= 0) {
            return fd.getPrincipalAmount();
        }
        
        if (elapsedDays >= totalDays) {
            return fd.getMaturityAmount();
        }
        
        // Calculate proportional compound interest
        double yearsElapsed = elapsedDays / 365.0;
        double p = fd.getPrincipalAmount().doubleValue();
        double r = fd.getInterestRate().doubleValue();
        
        double currentValue = p * Math.pow(1 + r, yearsElapsed);
        
        return BigDecimal.valueOf(currentValue).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Verify if account has sufficient balance
     */
    private boolean verifyAccountBalance(String accountNumber, BigDecimal amount, String authHeader) {
        try {
            String url = "http://localhost:8082/api/accounts/" + accountNumber;
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authHeader);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Object balanceObj = response.getBody().get("balance");
                BigDecimal balance = new BigDecimal(balanceObj.toString());
                return balance.compareTo(amount) >= 0;
            }
        } catch (Exception e) {
            System.err.println("Error verifying account balance: " + e.getMessage());
        }
        return false;
    }

    /**
     * Debit amount from account
     */
    private void debitFromAccount(String accountNumber, BigDecimal amount, Long fdId, String authHeader) {
        try {
            String url = "http://localhost:8082/api/accounts/" + accountNumber + "/debit";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authHeader);
            headers.set("Content-Type", "application/json");
            
            Map<String, Object> request = new HashMap<>();
            request.put("amount", amount);
            request.put("description", "FD Opening - FD#" + fdId);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            
            System.out.println("Debited " + amount + " from account: " + accountNumber);
        } catch (Exception e) {
            System.err.println("Error debiting from account: " + e.getMessage());
            throw new RuntimeException("Failed to debit from account");
        }
    }

    /**
     * Credit amount to account
     */
    private void creditToAccount(String accountNumber, BigDecimal amount, Long fdId, String authHeader) {
        try {
            String url = "http://localhost:8082/api/accounts/" + accountNumber + "/credit";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authHeader);
            headers.set("Content-Type", "application/json");
            
            Map<String, Object> request = new HashMap<>();
            request.put("amount", amount);
            request.put("description", "FD Closing - FD#" + fdId);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            
            System.out.println("Credited " + amount + " to account: " + accountNumber);
        } catch (Exception e) {
            System.err.println("Error crediting to account: " + e.getMessage());
            throw new RuntimeException("Failed to credit to account");
        }
    }

    /**
     * Create transaction entry
     */
    private void createTransaction(String accountNumber, BigDecimal amount, String type, String description, String authHeader) {
        try {
            String url = "http://localhost:8083/api/transactions/create";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authHeader);
            headers.set("Content-Type", "application/json");
            
            Map<String, Object> request = new HashMap<>();
            request.put("accountNumber", accountNumber);
            request.put("amount", amount);
            request.put("transactionType", type);
            request.put("sourceAccountNumber", accountNumber);
            request.put("targetAccountNumber", "FIXED_DEPOSIT");
            request.put("description", description);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            
            System.out.println("Created " + type + " transaction for account: " + accountNumber);
        } catch (Exception e) {
            System.err.println("Error creating transaction: " + e.getMessage());
            // Don't throw exception as transaction is secondary
        }
    }

    /**
     * Map entity to DTO
     */
    private FixedDepositDTO mapToDTO(FixedDeposit fd) {
        FixedDepositDTO dto = new FixedDepositDTO();
        dto.setFdId(fd.getFdId());
        dto.setCustomerId(fd.getCustomerId());
        dto.setAccountNumber(fd.getAccountNumber());
        dto.setPrincipalAmount(fd.getPrincipalAmount());
        dto.setInterestRate(fd.getInterestRate());
        dto.setTenureYears(fd.getTenureYears());
        dto.setStartDate(fd.getStartDate());
        dto.setMaturityDate(fd.getMaturityDate());
        dto.setMaturityAmount(fd.getMaturityAmount());
        dto.setCurrentValue(calculateCurrentValue(fd));
        dto.setStatus(fd.getStatus());
        dto.setCreatedAt(fd.getCreatedAt());
        dto.setClosedAt(fd.getClosedAt());
        
        // Calculate progress
        long totalDays = ChronoUnit.DAYS.between(fd.getStartDate(), fd.getMaturityDate());
        long elapsedDays = ChronoUnit.DAYS.between(fd.getStartDate(), 
                "ACTIVE".equals(fd.getStatus()) ? LocalDate.now() : fd.getMaturityDate());
        
        dto.setDaysElapsed((int) Math.max(0, elapsedDays));
        dto.setTotalDays((int) totalDays);
        
        return dto;
    }
}