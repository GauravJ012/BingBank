package com.bingbank.cardsservice.service;

import com.bingbank.cardsservice.dto.ChangePinRequest;
import com.bingbank.cardsservice.dto.DeactivateCardRequest;
import com.bingbank.cardsservice.dto.DebitCardDTO;
import com.bingbank.cardsservice.model.DebitCard;
import com.bingbank.cardsservice.repository.DebitCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.format.DateTimeFormatter;

@Service
public class DebitCardService {

    @Autowired
    private DebitCardRepository debitCardRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * Get debit card by customer ID
     */
    public DebitCardDTO getDebitCardByCustomerId(Long customerId) {
        System.out.println("DebitCardService: Fetching debit card for customer: " + customerId);
        
        DebitCard card = debitCardRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new RuntimeException("Debit card not found for customer"));
        
        return mapToDTO(card);
    }

    /**
     * Change ATM PIN - No OTP required
     */
    @Transactional
    public DebitCardDTO changeAtmPin(ChangePinRequest request, String authHeader) {
        System.out.println("DebitCardService: Changing ATM PIN for card: " + request.getCardId());
        
        DebitCard card = debitCardRepository.findById(request.getCardId())
                .orElseThrow(() -> new RuntimeException("Debit card not found"));
        
        // Verify ownership
        if (!card.getCustomerId().equals(request.getCustomerId())) {
            throw new RuntimeException("Unauthorized to change PIN for this card");
        }
        
        // Check if card is active
        if (!"ACTIVE".equals(card.getCardStatus())) {
            throw new RuntimeException("Card is not active");
        }
        
        // Encrypt and update PIN directly (no OTP check)
        String encryptedPin = passwordEncoder.encode(request.getNewPin());
        card.setAtmPin(encryptedPin);
        
        DebitCard updatedCard = debitCardRepository.save(card);
        System.out.println("DebitCardService: ATM PIN changed successfully");
        
        return mapToDTO(updatedCard);
    }

    /**
     * Deactivate debit card - No OTP required
     */
    @Transactional
    public DebitCardDTO deactivateCard(DeactivateCardRequest request, String authHeader) {
        System.out.println("DebitCardService: Deactivating card: " + request.getCardId());
        
        DebitCard card = debitCardRepository.findById(request.getCardId())
                .orElseThrow(() -> new RuntimeException("Debit card not found"));
        
        // Verify ownership
        if (!card.getCustomerId().equals(request.getCustomerId())) {
            throw new RuntimeException("Unauthorized to deactivate this card");
        }
        
        // Check if already inactive
        if ("INACTIVE".equals(card.getCardStatus())) {
            throw new RuntimeException("Card is already inactive");
        }
        
        // Deactivate card directly (no OTP check)
        card.setCardStatus("INACTIVE");
        
        DebitCard updatedCard = debitCardRepository.save(card);
        System.out.println("DebitCardService: Card deactivated successfully");
        
        return mapToDTO(updatedCard);
    }

    /**
     * Map entity to DTO
     */
    private DebitCardDTO mapToDTO(DebitCard card) {
        DebitCardDTO dto = new DebitCardDTO();
        dto.setCardId(card.getCardId());
        dto.setCustomerId(card.getCustomerId());
        dto.setAccountNumber(card.getAccountNumber());
        dto.setCardNumber(card.getCardNumber());
        dto.setCardholderName(card.getCardholderName());
        dto.setExpiryDate(card.getExpiryDate());
        dto.setCvv(card.getCvv());
        dto.setCardStatus(card.getCardStatus());
        
        // Format expiry date as MM/YY
        if (card.getExpiryDate() != null) {
            dto.setExpiryMonth(card.getExpiryDate().format(DateTimeFormatter.ofPattern("MM")));
            dto.setExpiryYear(card.getExpiryDate().format(DateTimeFormatter.ofPattern("yy")));
        }
        
        return dto;
    }
}