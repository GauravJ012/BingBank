package com.bingbank.transactionservice.service;

import com.bingbank.transactionservice.dto.TransactionDTO;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class PdfService {

    private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
    private static final Font HEADING_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
    private static final Font NORMAL_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
    private static final Font SMALL_FONT = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL);

    /**
     * Generate bank statement PDF
     */
    public byte[] generateBankStatement(
            Map<String, Object> customerInfo,
            Map<String, Object> accountInfo,
            List<TransactionDTO> transactions) throws DocumentException {
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, out);
        
        document.open();
        
        // Add bank name header
        addBankHeader(document);
        
        // Add customer information
        addCustomerInfo(document, customerInfo);
        
        // Add account information
        addAccountInfo(document, accountInfo);
        
        // Add statement period
        addStatementPeriod(document, transactions);
        
        // Add transactions table
        addTransactionsTable(document, transactions);
        
        // Add summary
        addSummary(document, transactions);
        
        document.close();
        
        return out.toByteArray();
    }

    private void addBankHeader(Document document) throws DocumentException {
        Paragraph bank = new Paragraph("BingBank", TITLE_FONT);
        bank.setAlignment(Element.ALIGN_CENTER);
        bank.setSpacingAfter(5);
        document.add(bank);
        
        Paragraph subtitle = new Paragraph("Account Statement", HEADING_FONT);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(20);
        document.add(subtitle);
        
        // Add line separator
        LineSeparator line = new LineSeparator();
        document.add(new Chunk(line));
        document.add(Chunk.NEWLINE);
    }

    private void addCustomerInfo(Document document, Map<String, Object> customerInfo) throws DocumentException {
        Paragraph customerTitle = new Paragraph("Customer Information", HEADING_FONT);
        customerTitle.setSpacingBefore(10);
        customerTitle.setSpacingAfter(5);
        document.add(customerTitle);
        
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingAfter(15);
        
        addInfoRow(table, "Customer ID:", String.valueOf(customerInfo.get("customerId")));
        addInfoRow(table, "Name:", customerInfo.get("firstName") + " " + customerInfo.get("lastName"));
        addInfoRow(table, "Email:", String.valueOf(customerInfo.get("email")));
        addInfoRow(table, "Phone:", String.valueOf(customerInfo.get("mobile")));
        addInfoRow(table, "Address:", String.valueOf(customerInfo.get("address")));
        
        document.add(table);
    }

    private void addAccountInfo(Document document, Map<String, Object> accountInfo) throws DocumentException {
        Paragraph accountTitle = new Paragraph("Account Information", HEADING_FONT);
        accountTitle.setSpacingBefore(10);
        accountTitle.setSpacingAfter(5);
        document.add(accountTitle);
        
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingAfter(15);
        
        addInfoRow(table, "Account Number:", String.valueOf(accountInfo.get("accountNumber")));
        addInfoRow(table, "Account Type:", String.valueOf(accountInfo.get("accountType")));
        addInfoRow(table, "Branch Code:", String.valueOf(accountInfo.get("branchCode")));
        addInfoRow(table, "Current Balance:", "₹" + accountInfo.get("balance"));
        
        document.add(table);
    }

    private void addStatementPeriod(Document document, List<TransactionDTO> transactions) throws DocumentException {
        if (transactions.isEmpty()) return;
        
        LocalDate startDate = transactions.get(transactions.size() - 1).getTransactionDate();
        LocalDate endDate = transactions.get(0).getTransactionDate();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
        String period = startDate.format(formatter) + " to " + endDate.format(formatter);
        
        Paragraph periodPara = new Paragraph("Statement Period: " + period, NORMAL_FONT);
        periodPara.setSpacingBefore(5);
        periodPara.setSpacingAfter(10);
        document.add(periodPara);
    }

    private void addTransactionsTable(Document document, List<TransactionDTO> transactions) throws DocumentException {
        Paragraph transTitle = new Paragraph("Transaction Details", HEADING_FONT);
        transTitle.setSpacingBefore(10);
        transTitle.setSpacingAfter(10);
        document.add(transTitle);
        
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.5f, 1.5f, 1.2f, 2f, 2f, 2f});
        
        // Add headers
        addTableHeader(table, "Date");
        addTableHeader(table, "Trans ID");
        addTableHeader(table, "Type");
        addTableHeader(table, "Amount");
        addTableHeader(table, "From Account");
        addTableHeader(table, "To Account");
        
        // Add transaction rows
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (TransactionDTO transaction : transactions) {
            addTableCell(table, transaction.getTransactionDate().format(formatter));
            addTableCell(table, String.valueOf(transaction.getTransactionId()));
            addTableCell(table, transaction.getTransactionType());
            
            // Color code the amount based on type
            PdfPCell amountCell = new PdfPCell(new Phrase("₹" + transaction.getAmount(), SMALL_FONT));
            if ("CREDIT".equals(transaction.getTransactionType())) {
                amountCell.setBackgroundColor(new BaseColor(200, 255, 200)); // Light green
            } else {
                amountCell.setBackgroundColor(new BaseColor(255, 200, 200)); // Light red
            }
            amountCell.setPadding(5);
            table.addCell(amountCell);
            
            addTableCell(table, transaction.getSourceAccountNumber());
            addTableCell(table, transaction.getTargetAccountNumber() != null ? 
                    transaction.getTargetAccountNumber() : "N/A");
        }
        
        document.add(table);
    }

    private void addSummary(Document document, List<TransactionDTO> transactions) throws DocumentException {
        Paragraph summaryTitle = new Paragraph("Transaction Summary", HEADING_FONT);
        summaryTitle.setSpacingBefore(20);
        summaryTitle.setSpacingAfter(10);
        document.add(summaryTitle);
        
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        int debitCount = 0;
        int creditCount = 0;
        
        for (TransactionDTO transaction : transactions) {
            if ("DEBIT".equals(transaction.getTransactionType())) {
                totalDebit = totalDebit.add(transaction.getAmount());
                debitCount++;
            } else {
                totalCredit = totalCredit.add(transaction.getAmount());
                creditCount++;
            }
        }
        
        PdfPTable summaryTable = new PdfPTable(2);
        summaryTable.setWidthPercentage(50);
        summaryTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        
        addInfoRow(summaryTable, "Total Transactions:", String.valueOf(transactions.size()));
        addInfoRow(summaryTable, "Total Debits (" + debitCount + "):", "₹" + totalDebit);
        addInfoRow(summaryTable, "Total Credits (" + creditCount + "):", "₹" + totalCredit);
        
        document.add(summaryTable);
        
        // Add footer
        Paragraph footer = new Paragraph("\nThis is a computer-generated statement and does not require a signature.", SMALL_FONT);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(30);
        document.add(footer);
        
        Paragraph generatedDate = new Paragraph("Generated on: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")), SMALL_FONT);
        generatedDate.setAlignment(Element.ALIGN_CENTER);
        document.add(generatedDate);
    }

    private void addInfoRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, NORMAL_FONT));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(5);
        table.addCell(labelCell);
        
        PdfPCell valueCell = new PdfPCell(new Phrase(value, NORMAL_FONT));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(5);
        table.addCell(valueCell);
    }

    private void addTableHeader(PdfPTable table, String header) {
        PdfPCell cell = new PdfPCell(new Phrase(header, HEADING_FONT));
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void addTableCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, SMALL_FONT));
        cell.setPadding(5);
        table.addCell(cell);
    }
}