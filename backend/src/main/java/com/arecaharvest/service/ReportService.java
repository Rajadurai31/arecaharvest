package com.arecaharvest.service;

import com.arecaharvest.entity.Attendance;
import com.arecaharvest.repository.*;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates PDF and CSV/Excel reports for all report types.
 * Apache POI is used for Excel; iText 5 for PDF.
 */
@Service
@RequiredArgsConstructor
public class ReportService {

    private final WorkerRepository workerRepository;
    private final WageLedgerRepository wageLedgerRepository;
    private final AttendanceRepository attendanceRepository;
    private final HarvestSessionRepository sessionRepository;
    private final FieldRepository fieldRepository;
    private final SaleRepository saleRepository;
    private final BuyerRepository buyerRepository;
    private final BuyerPaymentRepository buyerPaymentRepository;
    private final ExpenseRepository expenseRepository;

    // ── Data records ─────────────────────────────────────────────────────────

    public record WorkerWageRow(String workerName, String skillType, int daysWorked,
                                 BigDecimal totalEarned, BigDecimal totalPaid, BigDecimal outstanding) {}

    public record SaleRow(String saleNumber, String buyerName, LocalDate saleDate,
                           BigDecimal quantityKg, BigDecimal ratePerUnit,
                           BigDecimal grossAmount, BigDecimal netAmount, String paymentStatus) {}

    public record ExpenseRow(LocalDate date, String category, String description,
                              BigDecimal amount, String paymentMethod) {}

    public record ProfitRow(String period, BigDecimal revenue, BigDecimal expenses, BigDecimal profit) {}

    // ── Report data builders ─────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<WorkerWageRow> buildWorkerWageReport(LocalDate from, LocalDate to) {
        return workerRepository.findAll().stream()
                .filter(w -> w.getStatus() == com.arecaharvest.entity.enums.WorkerStatus.ACTIVE)
                .map(w -> {
                    long days = attendanceRepository
                            .searchAttendance(w.getId(), null, from, to,
                                    org.springframework.data.domain.PageRequest.of(0, 1000))
                            .getContent().stream()
                            .filter(a -> a.getStatus() == Attendance.AttendanceStatus.PRESENT
                                    || a.getStatus() == Attendance.AttendanceStatus.HALF_DAY)
                            .count();
                    BigDecimal earned = wageLedgerRepository.calculateTotalEarned(w.getId());
                    BigDecimal paid   = wageLedgerRepository.calculateTotalPaid(w.getId());
                    BigDecimal outstanding = wageLedgerRepository.calculateOutstandingBalance(w.getId());
                    return new WorkerWageRow(w.getName(), w.getSkillType().name(),
                            (int) days, earned, paid, outstanding);
                }).toList();
    }

    @Transactional(readOnly = true)
    public List<SaleRow> buildSalesReport(LocalDate from, LocalDate to) {
        return saleRepository.searchSales(null, null, from, to,
                        org.springframework.data.domain.PageRequest.of(0, 10000))
                .getContent().stream()
                .map(s -> new SaleRow(s.getSaleNumber(), s.getBuyer().getName(),
                        s.getSaleDate(), s.getQuantityKg(), s.getRatePerUnit(),
                        s.getGrossAmount(), s.getNetAmount(), s.getPaymentStatus().name()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ExpenseRow> buildExpenseReport(LocalDate from, LocalDate to) {
        return expenseRepository.searchExpenses(null, null, from, to,
                        org.springframework.data.domain.PageRequest.of(0, 10000))
                .getContent().stream()
                .map(e -> new ExpenseRow(e.getExpenseDate(), e.getCategory().getName(),
                        e.getDescription(), e.getAmount(), e.getPaymentMethod().name()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProfitRow> buildProfitReport(LocalDate from, LocalDate to) {
        List<ProfitRow> rows = new ArrayList<>();
        LocalDate cursor = from.withDayOfMonth(1);
        while (!cursor.isAfter(to)) {
            LocalDate end = cursor.withDayOfMonth(cursor.lengthOfMonth());
            LocalDate effectiveTo = end.isAfter(to) ? to : end;
            BigDecimal rev = saleRepository.sumRevenueForPeriod(cursor, effectiveTo);
            BigDecimal exp = expenseRepository.sumExpensesForPeriod(cursor, effectiveTo);
            rows.add(new ProfitRow(cursor.format(DateTimeFormatter.ofPattern("MMM yyyy")),
                    rev, exp, rev.subtract(exp)));
            cursor = cursor.plusMonths(1);
        }
        return rows;
    }

    // ── CSV Export ────────────────────────────────────────────────────────────

    public byte[] exportWorkerWagesCSV(LocalDate from, LocalDate to) {
        List<WorkerWageRow> rows = buildWorkerWageReport(from, to);
        StringBuilder sb = new StringBuilder("Worker,Skill,Days Worked,Total Earned (Rs),Total Paid (Rs),Outstanding (Rs)\n");
        for (var r : rows) {
            sb.append(csv(r.workerName())).append(",").append(r.skillType()).append(",")
              .append(r.daysWorked()).append(",")
              .append(r.totalEarned()).append(",").append(r.totalPaid()).append(",")
              .append(r.outstanding()).append("\n");
        }
        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    public byte[] exportSalesCSV(LocalDate from, LocalDate to) {
        List<SaleRow> rows = buildSalesReport(from, to);
        StringBuilder sb = new StringBuilder("Sale #,Buyer,Date,Quantity (KG),Rate (Rs/KG),Gross (Rs),Net (Rs),Status\n");
        for (var r : rows) {
            sb.append(csv(r.saleNumber())).append(",").append(csv(r.buyerName())).append(",")
              .append(r.saleDate()).append(",").append(r.quantityKg()).append(",")
              .append(r.ratePerUnit()).append(",").append(r.grossAmount()).append(",")
              .append(r.netAmount()).append(",").append(r.paymentStatus()).append("\n");
        }
        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    public byte[] exportExpensesCSV(LocalDate from, LocalDate to) {
        List<ExpenseRow> rows = buildExpenseReport(from, to);
        StringBuilder sb = new StringBuilder("Date,Category,Description,Amount (Rs),Method\n");
        for (var r : rows) {
            sb.append(r.date()).append(",").append(r.category()).append(",")
              .append(csv(r.description())).append(",").append(r.amount()).append(",")
              .append(r.paymentMethod()).append("\n");
        }
        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    public byte[] exportProfitCSV(LocalDate from, LocalDate to) {
        List<ProfitRow> rows = buildProfitReport(from, to);
        StringBuilder sb = new StringBuilder("Period,Revenue (Rs),Expenses (Rs),Profit (Rs)\n");
        for (var r : rows) {
            sb.append(r.period()).append(",").append(r.revenue()).append(",")
              .append(r.expenses()).append(",").append(r.profit()).append("\n");
        }
        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    // ── Excel Export ──────────────────────────────────────────────────────────

    public byte[] exportWorkerWagesExcel(LocalDate from, LocalDate to) throws IOException {
        List<WorkerWageRow> rows = buildWorkerWageReport(from, to);
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Worker Wages");
            String[] headers = {"Worker", "Skill", "Days Worked", "Total Earned (Rs)", "Total Paid (Rs)", "Outstanding (Rs)"};
            createHeaderRow(sheet, headers);
            int rowIdx = 1;
            for (var r : rows) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(r.workerName());
                row.createCell(1).setCellValue(r.skillType());
                row.createCell(2).setCellValue(r.daysWorked());
                row.createCell(3).setCellValue(r.totalEarned().doubleValue());
                row.createCell(4).setCellValue(r.totalPaid().doubleValue());
                row.createCell(5).setCellValue(r.outstanding().doubleValue());
            }
            autoSizeColumns(sheet, headers.length);
            return toBytes(wb);
        }
    }

    public byte[] exportSalesExcel(LocalDate from, LocalDate to) throws IOException {
        List<SaleRow> rows = buildSalesReport(from, to);
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Sales");
            String[] headers = {"Sale #", "Buyer", "Date", "Qty KG", "Rate Rs/KG", "Gross Rs", "Net Rs", "Status"};
            createHeaderRow(sheet, headers);
            int rowIdx = 1;
            for (var r : rows) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(r.saleNumber());
                row.createCell(1).setCellValue(r.buyerName());
                row.createCell(2).setCellValue(r.saleDate().toString());
                row.createCell(3).setCellValue(r.quantityKg().doubleValue());
                row.createCell(4).setCellValue(r.ratePerUnit().doubleValue());
                row.createCell(5).setCellValue(r.grossAmount().doubleValue());
                row.createCell(6).setCellValue(r.netAmount().doubleValue());
                row.createCell(7).setCellValue(r.paymentStatus());
            }
            autoSizeColumns(sheet, headers.length);
            return toBytes(wb);
        }
    }

    // ── PDF Export ────────────────────────────────────────────────────────────

    public byte[] exportProfitPDF(LocalDate from, LocalDate to) throws DocumentException {
        List<ProfitRow> rows = buildProfitReport(from, to);
        Document document = new Document();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, baos);
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9);

        document.add(new Paragraph("ArecaHarvest - Profit & Loss Report", titleFont));
        document.add(new Paragraph("Period: " + from + " to " + to, cellFont));
        document.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        for (String h : new String[]{"Period", "Revenue (Rs)", "Expenses (Rs)", "Profit (Rs)"}) {
            PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
            cell.setBackgroundColor(new BaseColor(34, 197, 94));
            cell.setPadding(6);
            table.addCell(cell);
        }
        BigDecimal totalRev = BigDecimal.ZERO;
        BigDecimal totalExp = BigDecimal.ZERO;
        for (var r : rows) {
            table.addCell(new Phrase(r.period(), cellFont));
            table.addCell(new Phrase(r.revenue().toString(), cellFont));
            table.addCell(new Phrase(r.expenses().toString(), cellFont));
            table.addCell(new Phrase(r.profit().toString(), cellFont));
            totalRev = totalRev.add(r.revenue());
            totalExp = totalExp.add(r.expenses());
        }
        Font boldCell = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
        table.addCell(new Phrase("TOTAL", boldCell));
        table.addCell(new Phrase(totalRev.toString(), boldCell));
        table.addCell(new Phrase(totalExp.toString(), boldCell));
        table.addCell(new Phrase(totalRev.subtract(totalExp).toString(), boldCell));

        document.add(table);
        document.close();
        return baos.toByteArray();
    }

    public byte[] exportWorkerWagesPDF(LocalDate from, LocalDate to) throws DocumentException {
        List<WorkerWageRow> rows = buildWorkerWageReport(from, to);
        Document document = new Document(PageSize.A4.rotate());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, baos);
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 8);

        document.add(new Paragraph("ArecaHarvest - Worker Wage Report", titleFont));
        document.add(new Paragraph("Period: " + from + " to " + to, cellFont));
        document.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        for (String h : new String[]{"Worker", "Skill", "Days", "Earned Rs", "Paid Rs", "Outstanding Rs"}) {
            PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
            cell.setBackgroundColor(new BaseColor(34, 197, 94));
            cell.setPadding(5);
            table.addCell(cell);
        }
        for (var r : rows) {
            table.addCell(new Phrase(r.workerName(), cellFont));
            table.addCell(new Phrase(r.skillType(), cellFont));
            table.addCell(new Phrase(String.valueOf(r.daysWorked()), cellFont));
            table.addCell(new Phrase(r.totalEarned().toString(), cellFont));
            table.addCell(new Phrase(r.totalPaid().toString(), cellFont));
            table.addCell(new Phrase(r.outstanding().toString(), cellFont));
        }
        document.add(table);
        document.close();
        return baos.toByteArray();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String csv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private void createHeaderRow(Sheet sheet, String[] headers) {
        Row headerRow = sheet.createRow(0);
        CellStyle style = sheet.getWorkbook().createCellStyle();
        org.apache.poi.ss.usermodel.Font font = sheet.getWorkbook().createFont();
        font.setBold(true);
        style.setFont(font);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
        }
    }

    private void autoSizeColumns(Sheet sheet, int count) {
        for (int i = 0; i < count; i++) sheet.autoSizeColumn(i);
    }

    private byte[] toBytes(Workbook wb) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        wb.write(baos);
        return baos.toByteArray();
    }
}
