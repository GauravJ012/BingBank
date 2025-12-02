package com.bingbank.accountservice.service;

import com.bingbank.accountservice.dto.AccountDTO;
import com.bingbank.accountservice.dto.BranchDTO;
import com.bingbank.accountservice.model.Account;
import com.bingbank.accountservice.model.Branch;
import com.bingbank.accountservice.repository.AccountRepository;
import com.bingbank.accountservice.repository.BranchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private BranchRepository branchRepository;

    /**
     * Get all accounts for a customer
     */
    public List<AccountDTO> getAccountsByCustomerId(Long customerId) {
        System.out.println("AccountService: Fetching accounts for customer ID: " + customerId);
        List<Account> accounts = accountRepository.findByCustomerId(customerId);
        System.out.println("AccountService: Found " + accounts.size() + " accounts");
        return accounts.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get account by account number
     */
    public AccountDTO getAccountByAccountNumber(String accountNumber) {
        System.out.println("AccountService: Fetching account: " + accountNumber);
        Account account = accountRepository.findById(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountNumber));
        return mapToDTO(account);
    }

    /**
     * Check if account exists
     */
    public boolean accountExists(String accountNumber) {
        return accountRepository.existsByAccountNumber(accountNumber);
    }

    /**
     * Map Account entity to DTO
     */
    private AccountDTO mapToDTO(Account account) {
        AccountDTO dto = new AccountDTO();
        dto.setAccountNumber(account.getAccountNumber());
        dto.setCustomerId(account.getCustomerId());
        dto.setAccountType(account.getAccountType());
        dto.setRoutingNumber(account.getRoutingNumber());
        dto.setBalance(account.getBalance());
        
        if (account.getBranch() != null) {
            dto.setBranch(mapBranchToDTO(account.getBranch()));
        }
        
        return dto;
    }

    /**
     * Map Branch entity to DTO
     */
    private BranchDTO mapBranchToDTO(Branch branch) {
        BranchDTO dto = new BranchDTO();
        dto.setBranchId(branch.getBranchId());
        dto.setBranchName(branch.getBranchName());
        dto.setBranchCode(branch.getBranchCode());
        dto.setCity(branch.getCity());
        dto.setState(branch.getState());
        dto.setZipcode(branch.getZipcode());
        return dto;
    }
}