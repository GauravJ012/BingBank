package com.bingbank.cardsservice.service;

import com.bingbank.cardsservice.dto.CreditCardDTO;
import com.bingbank.cardsservice.dto.CreditCardTransactionDTO;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class CreditCardPdfService {

    private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.BLACK);
    private static final Font HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.BLACK);
    private static final Font NORMAL_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.BLACK);
    private static final Font SMALL_FONT = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL, BaseColor.GRAY);

    /**
     * Generate credit card statement PDF
     */
    public byte[] generateCreditCardStatement(
            CreditCardDTO creditCard,
            List<CreditCardTransactionDTO> transactions,
            int year,
            int month) throws DocumentException, IOException {

        System.out.println("Generating credit card statement for " + year + "-" + month);

        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Add logo only (no bank name)
            addLogoAndHeader(document);

            // Add spacing
            document.add(new Paragraph("\n"));

            // Add statement title
            Paragraph title = new Paragraph("CREDIT CARD STATEMENT", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph("\n"));

            // Add statement period
            LocalDate startDate = LocalDate.of(year, month, 1);
            LocalDate endDate = startDate.plusMonths(1).minusDays(1);
            
            Paragraph period = new Paragraph(
                    "Statement Period: " + startDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) +
                    " - " + endDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                    HEADER_FONT
            );
            period.setAlignment(Element.ALIGN_CENTER);
            document.add(period);

            document.add(new Paragraph("\n"));

            // Add card details section
            addCardDetails(document, creditCard);

            document.add(new Paragraph("\n"));

            // Add account summary
            addAccountSummary(document, creditCard, transactions);

            document.add(new Paragraph("\n"));

            // Add payment information
            addPaymentInformation(document, creditCard);

            document.add(new Paragraph("\n"));

            // Add transactions table
            addTransactionsTable(document, transactions);

            document.add(new Paragraph("\n"));

            // Add footer
            addFooter(document);

            document.close();

            System.out.println("Statement generated successfully");
            return out.toByteArray();

        } catch (Exception e) {
            System.err.println("Error generating PDF: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Add logo only (no bank name text)
     */
    private void addLogoAndHeader(Document document) throws DocumentException, IOException {
        try {
            // Load logo
            ClassPathResource resource = new ClassPathResource("static/logo.png");
            Image logo = Image.getInstance(resource.getURL());
            
            // Scale logo to appropriate size
            logo.scaleToFit(120, 120);
            logo.setAlignment(Element.ALIGN_CENTER);
            
            document.add(logo);
            document.add(new Paragraph("\n")); // Add spacing after logo
            
        } catch (Exception e) {
            System.err.println("Could not load logo: " + e.getMessage());
            // If logo fails, add text header as fallback
            Paragraph bankName = new Paragraph("BingBank", TITLE_FONT);
            bankName.setAlignment(Element.ALIGN_CENTER);
            document.add(bankName);
        }
    }

    /**
     * Add card details section
     */
    private void addCardDetails(Document document, CreditCardDTO creditCard) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);

        // Header
        PdfPCell headerCell = new PdfPCell(new Phrase("CARD DETAILS", HEADER_FONT));
        headerCell.setColspan(2);
        headerCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        headerCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        headerCell.setPadding(8);
        table.addCell(headerCell);

        // Card details
        addDetailRow(table, "Cardholder Name:", creditCard.getCardholderName());
        addDetailRow(table, "Card Number:", maskCardNumber(creditCard.getCardNumber()));
        addDetailRow(table, "Account Number:", creditCard.getAccountNumber());

        document.add(table);
    }

    /**
     * Add account summary
     */
    private void addAccountSummary(Document document, CreditCardDTO creditCard, 
                                   List<CreditCardTransactionDTO> transactions) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);

        // Header
        PdfPCell headerCell = new PdfPCell(new Phrase("ACCOUNT SUMMARY", HEADER_FONT));
        headerCell.setColspan(2);
        headerCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        headerCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        headerCell.setPadding(8);
        table.addCell(headerCell);

        // Calculate totals
        BigDecimal totalPurchases = transactions.stream()
                .filter(t -> "PURCHASE".equals(t.getTransactionType()))
                .map(CreditCardTransactionDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPayments = transactions.stream()
                .filter(t -> "PAYMENT".equals(t.getTransactionType()))
                .map(CreditCardTransactionDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Summary details
        addDetailRow(table, "Previous Balance:", "$" + creditCard.getOutstandingBalance().add(totalPayments).subtract(totalPurchases).toString());
        addDetailRow(table, "Total Purchases:", "$" + totalPurchases.toString());
        addDetailRow(table, "Total Payments:", "$" + totalPayments.toString());
        addDetailRow(table, "Current Balance:", "$" + creditCard.getOutstandingBalance().toString());
        addDetailRow(table, "Credit Limit:", "$" + creditCard.getCreditLimit().toString());
        addDetailRow(table, "Available Credit:", "$" + creditCard.getAvailableCredit().toString());

        document.add(table);
    }

    /**
     * Add payment information
     */
    private void addPaymentInformation(Document document, CreditCardDTO creditCard) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);

        // Header
        PdfPCell headerCell = new PdfPCell(new Phrase("PAYMENT INFORMATION", HEADER_FONT));
        headerCell.setColspan(2);
        headerCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        headerCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        headerCell.setPadding(8);
        table.addCell(headerCell);

        // Payment details
        addDetailRow(table, "Payment Due Date:", 
                creditCard.getPaymentDueDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        addDetailRow(table, "Minimum Payment Due:", "$" + creditCard.getOutstandingBalance().multiply(new BigDecimal("0.05")).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
        addDetailRow(table, "Total Amount Due:", "$" + creditCard.getOutstandingBalance().toString());

        document.add(table);

        // Add payment note
        Paragraph note = new Paragraph(
                "Please ensure payment is made by the due date to avoid late payment fees.",
                SMALL_FONT
        );
        note.setAlignment(Element.ALIGN_LEFT);
        note.setSpacingBefore(5);
        document.add(note);
    }

    /**
     * Add transactions table
     */
    private void addTransactionsTable(Document document, List<CreditCardTransactionDTO> transactions) 
            throws DocumentException {
        
        // Header
        Paragraph header = new Paragraph("TRANSACTION DETAILS", HEADER_FONT);
        header.setSpacingBefore(10);
        document.add(header);

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setWidths(new float[]{2, 3, 2, 2, 1.5f});

        // Table headers
        addTableHeader(table, "Date");
        addTableHeader(table, "Merchant");
        addTableHeader(table, "Category");
        addTableHeader(table, "Type");
        addTableHeader(table, "Amount");

        // Add transactions
        for (CreditCardTransactionDTO transaction : transactions) {
            // Format date properly
            String dateStr = transaction.getTransactionDate() != null ? 
                    transaction.getTransactionDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : "-";
            addTableCell(table, dateStr);
            
            // Merchant name
            String merchantName = transaction.getMerchantName() != null ? transaction.getMerchantName() : "-";
            addTableCell(table, merchantName);
            
            // Category
            String category = transaction.getCategory() != null ? transaction.getCategory() : "-";
            addTableCell(table, category);
            
            // Transaction type
            String type = transaction.getTransactionType() != null ? transaction.getTransactionType() : "-";
            addTableCell(table, type);
            
            // Amount with color
            String amountStr = "$" + (transaction.getAmount() != null ? transaction.getAmount().toString() : "0.00");
            if ("PURCHASE".equals(transaction.getTransactionType())) {
                addTableCell(table, amountStr, BaseColor.RED);
            } else if ("PAYMENT".equals(transaction.getTransactionType())) {
                addTableCell(table, amountStr, BaseColor.GREEN);
            } else {
                addTableCell(table, amountStr);
            }
        }

        document.add(table);
    }

    /**
     * Add footer
     */
    private void addFooter(Document document) throws DocumentException {
        document.add(new Paragraph("\n\n"));
        
        Paragraph footer = new Paragraph(
                "This is a computer-generated statement. For queries, please contact BingBank customer service.\n" +
                "Generated on: " + LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                SMALL_FONT
        );
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
    }

    /**
     * Helper method to add detail row
     */
    private void addDetailRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, HEADER_FONT));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(5);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, NORMAL_FONT));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(5);
        table.addCell(valueCell);
    }

    /**
     * Helper method to add table header
     */
    private void addTableHeader(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, HEADER_FONT));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cell.setPadding(8);
        table.addCell(cell);
    }

    /**
     * Helper method to add table cell
     */
    private void addTableCell(PdfPTable table, String text) {
        addTableCell(table, text, BaseColor.BLACK);
    }

    private void addTableCell(PdfPTable table, String text, BaseColor color) {
        Font font = new Font(NORMAL_FONT);
        font.setColor(color);
        
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5);
        table.addCell(cell);
    }

    /**
     * Mask card number for security
     */
    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return cardNumber;
        }
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }
}