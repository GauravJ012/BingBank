package com.bingbank.transactionservice.controller;

import com.bingbank.transactionservice.dto.StatementRequest;
import com.bingbank.transactionservice.dto.TransactionDTO;
import com.bingbank.transactionservice.dto.TransactionFilterRequest;
import com.bingbank.transactionservice.service.PdfService;
import com.bingbank.transactionservice.service.TransactionService;
import com.itextpdf.text.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private PdfService pdfService;

    /**
     * Get latest 5 transactions for dashboard
     */
    @GetMapping("/latest/{accountNumber}")
    public ResponseEntity<List<TransactionDTO>> getLatestTransactions(@PathVariable String accountNumber) {
        System.out.println("TransactionController: Fetching latest transactions for account: " + accountNumber);
        List<TransactionDTO> transactions = transactionService.getLatestTransactions(accountNumber);
        return ResponseEntity.ok(transactions);
    }

    /**
     * Get all transactions for an account
     */
    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<List<TransactionDTO>> getAllTransactions(@PathVariable String accountNumber) {
        System.out.println("TransactionController: Fetching all transactions for account: " + accountNumber);
        List<TransactionDTO> transactions = transactionService.getAllTransactions(accountNumber);
        return ResponseEntity.ok(transactions);
    }

    /**
     * Get filtered and sorted transactions
     */
    @PostMapping("/filter")
    public ResponseEntity<List<TransactionDTO>> getFilteredTransactions(
            @RequestBody TransactionFilterRequest request) {
        System.out.println("TransactionController: Filtering transactions for account: " + request.getAccountNumber());
        List<TransactionDTO> transactions = transactionService.getFilteredTransactions(request);
        return ResponseEntity.ok(transactions);
    }

    /**
     * Generate and download bank statement PDF
     */
    @PostMapping("/statement/pdf")
    public ResponseEntity<byte[]> generateStatement(@RequestBody StatementRequest request) {
        try {
            System.out.println("TransactionController: Generating statement for account: " + request.getAccountNumber());
            
            // Get transactions
            List<TransactionDTO> transactions;
            if (request.getTransactionIds() != null && !request.getTransactionIds().isEmpty()) {
                transactions = transactionService.getTransactionsByIds(request.getTransactionIds());
            } else {
                transactions = transactionService.getAllTransactions(request.getAccountNumber());
            }
            
            // Note: In a real application, you would fetch actual customer and account info
            // from their respective services. For now, we'll use placeholder data.
            java.util.Map<String, Object> customerInfo = new java.util.HashMap<>();
            customerInfo.put("customerId", request.getCustomerId());
            customerInfo.put("firstName", "Customer");
            customerInfo.put("lastName", "Name");
            customerInfo.put("email", "customer@email.com");
            customerInfo.put("mobile", "1234567890");
            customerInfo.put("address", "Customer Address");
            
            java.util.Map<String, Object> accountInfo = new java.util.HashMap<>();
            accountInfo.put("accountNumber", request.getAccountNumber());
            accountInfo.put("accountType", "Savings");
            accountInfo.put("branchCode", "BR001");
            accountInfo.put("balance", "10000.00");
            
            // Generate PDF
            byte[] pdfBytes = pdfService.generateBankStatement(customerInfo, accountInfo, transactions);
            
            // Set headers for PDF download
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            String filename = "bank_statement_" + request.getAccountNumber() + "_" + 
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf";
            headers.setContentDispositionFormData("attachment", filename);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
            
        } catch (DocumentException e) {
            System.err.println("Error generating PDF: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}