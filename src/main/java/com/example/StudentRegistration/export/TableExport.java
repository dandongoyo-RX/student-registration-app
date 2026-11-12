package com.example.StudentRegistration.export;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Turns a table (header row + data rows) into a CSV file or an Excel (.xlsx) file.
 * Written with plain Java, so no extra library is needed.
 * Numbers stay numbers; everything else is written as text.
 */
public final class TableExport {

    private TableExport() {
    }

    // ------------------------------------------------------------------ CSV

    /** CSV with a UTF-8 marker at the start so Excel shows accented names correctly. */
    public static byte[] toCsv(List<String> headers, List<List<Object>> rows) {
        StringBuilder sb = new StringBuilder("﻿");
        appendCsvLine(sb, headers);
        for (List<Object> row : rows) {
            appendCsvLine(sb, row);
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static void appendCsvLine(StringBuilder sb, List<?> cells) {
        for (int i = 0; i < cells.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(csvCell(cells.get(i)));
        }
        sb.append("\r\n");
    }

    private static String csvCell(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Number) {
            return value.toString();
        }
        String text = value.toString();
        // Stop spreadsheet programs treating text like "=SUM(...)" as a formula
        if (!text.isEmpty() && "=+-@".indexOf(text.charAt(0)) >= 0) {
            text = "'" + text;
        }
        if (text.contains(",") || text.contains("\"") || text.contains("\n") || text.contains("\r")) {
            text = "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }

    // ----------------------------------------------------------------- XLSX

    /** A one-sheet Excel workbook with a bold, frozen header row. */
    public static byte[] toXlsx(String sheetName, List<String> headers, List<List<Object>> rows, int[] columnWidths) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes, StandardCharsets.UTF_8)) {
            put(zip, "[Content_Types].xml",
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                    + "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
                    + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
                    + "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
                    + "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>"
                    + "<Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>"
                    + "<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>"
                    + "</Types>");
            put(zip, "_rels/.rels",
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                    + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                    + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>"
                    + "</Relationships>");
            put(zip, "xl/workbook.xml",
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                    + "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" "
                    + "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
                    + "<sheets><sheet name=\"" + xml(sheetName) + "\" sheetId=\"1\" r:id=\"rId1\"/></sheets>"
                    + "</workbook>");
            put(zip, "xl/_rels/workbook.xml.rels",
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                    + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                    + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/>"
                    + "<Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>"
                    + "</Relationships>");
            put(zip, "xl/styles.xml",
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                    + "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
                    + "<fonts count=\"2\"><font><sz val=\"11\"/><name val=\"Calibri\"/></font>"
                    + "<font><b/><sz val=\"11\"/><name val=\"Calibri\"/></font></fonts>"
                    + "<fills count=\"2\"><fill><patternFill patternType=\"none\"/></fill>"
                    + "<fill><patternFill patternType=\"gray125\"/></fill></fills>"
                    + "<borders count=\"1\"><border><left/><right/><top/><bottom/><diagonal/></border></borders>"
                    + "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>"
                    + "<cellXfs count=\"2\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/>"
                    + "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\"/></cellXfs>"
                    + "<cellStyles count=\"1\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/></cellStyles>"
                    + "</styleSheet>");
            put(zip, "xl/worksheets/sheet1.xml", sheetXml(headers, rows, columnWidths));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return bytes.toByteArray();
    }

    private static String sheetXml(List<String> headers, List<List<Object>> rows, int[] columnWidths) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>")
          .append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">")
          .append("<sheetViews><sheetView workbookViewId=\"0\">")
          .append("<pane ySplit=\"1\" topLeftCell=\"A2\" activePane=\"bottomLeft\" state=\"frozen\"/>")
          .append("</sheetView></sheetViews>");
        if (columnWidths != null && columnWidths.length > 0) {
            sb.append("<cols>");
            for (int i = 0; i < columnWidths.length; i++) {
                sb.append("<col min=\"").append(i + 1).append("\" max=\"").append(i + 1)
                  .append("\" width=\"").append(columnWidths[i]).append("\" customWidth=\"1\"/>");
            }
            sb.append("</cols>");
        }
        sb.append("<sheetData>");
        appendRow(sb, 1, headers, true);
        int r = 2;
        for (List<Object> row : rows) {
            appendRow(sb, r++, row, false);
        }
        sb.append("</sheetData></worksheet>");
        return sb.toString();
    }

    private static void appendRow(StringBuilder sb, int rowNumber, List<?> cells, boolean bold) {
        sb.append("<row r=\"").append(rowNumber).append("\">");
        for (int c = 0; c < cells.size(); c++) {
            Object value = cells.get(c);
            if (value == null) {
                continue;
            }
            String ref = columnName(c) + rowNumber;
            String style = bold ? " s=\"1\"" : "";
            if (value instanceof Number) {
                sb.append("<c r=\"").append(ref).append("\"").append(style).append("><v>")
                  .append(value).append("</v></c>");
            } else {
                sb.append("<c r=\"").append(ref).append("\"").append(style).append(" t=\"inlineStr\"><is><t>")
                  .append(xml(value.toString())).append("</t></is></c>");
            }
        }
        sb.append("</row>");
    }

    /** 0 -> A, 25 -> Z, 26 -> AA ... */
    static String columnName(int index) {
        StringBuilder name = new StringBuilder();
        int n = index + 1;
        while (n > 0) {
            int rem = (n - 1) % 26;
            name.insert(0, (char) ('A' + rem));
            n = (n - 1) / 26;
        }
        return name.toString();
    }

    private static String xml(String text) {
        StringBuilder out = new StringBuilder(text.length());
        for (char ch : text.toCharArray()) {
            switch (ch) {
                case '&' -> out.append("&amp;");
                case '<' -> out.append("&lt;");
                case '>' -> out.append("&gt;");
                case '"' -> out.append("&quot;");
                default -> {
                    // drop control characters that are not allowed in XML
                    if (ch >= 0x20 || ch == '\t' || ch == '\n' || ch == '\r') {
                        out.append(ch);
                    }
                }
            }
        }
        return out.toString();
    }

    private static void put(ZipOutputStream zip, String name, String content) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(content.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }
}
