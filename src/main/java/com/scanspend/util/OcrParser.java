package com.scanspend.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OcrParser {
    // capture numbers with optional currency markers almost anywhere
    private static final Pattern AMOUNT = Pattern.compile("(?i)(rs\\.?|inr|amount|total|grand)[:\\s]*\\$?([0-9]+(?:\\.[0-9]{1,2})?)");
    private static final Pattern ANY_NUMBER = Pattern.compile("([0-9]+(?:\\.[0-9]{1,2})?)");
    private static final Pattern DATE_ISO = Pattern.compile("(20[0-9]{2}[-/][01]?[0-9][-/][0-3]?[0-9])");
    private static final Pattern DATE_DMY = Pattern.compile("([0-3]?[0-9][-/][01]?[0-9][-/](20[0-9]{2}))");
    private static final Pattern DATE_DMY_DOTS = Pattern.compile("([0-3]?[0-9][.][01]?[0-9][.](20[0-9]{2}))");
    private static final Pattern MERCHANT_KEY = Pattern.compile("(?i)merchant\\s*[:\\-]?\\s*([A-Za-z0-9 '&.-]{3,40})");
    private static final Pattern MERCHANT_LINE = Pattern.compile("^([A-Za-z0-9 '&.-]{3,40})$", Pattern.MULTILINE);

    public static BigDecimal extractAmount(String text) {
        Matcher m = AMOUNT.matcher(text);
        if (m.find()) {
            return new BigDecimal(m.group(2));
        }
        // fallback: pick the largest numeric value present
        BigDecimal max = BigDecimal.ZERO;
        Matcher any = ANY_NUMBER.matcher(text);
        while (any.find()) {
            try {
                BigDecimal v = new BigDecimal(any.group(1));
                if (v.compareTo(max) > 0) max = v;
            } catch (NumberFormatException ignored) {}
        }
        return max;
    }

    public static LocalDate extractDate(String text) {
        Matcher iso = DATE_ISO.matcher(text);
        if (iso.find()) {
            String raw = iso.group(1).replace('/', '-');
            try {
                return LocalDate.parse(raw, DateTimeFormatter.ofPattern("yyyy-M-d"));
            } catch (DateTimeParseException ignored) {}
        }
        Matcher dmy = DATE_DMY.matcher(text);
        if (dmy.find()) {
            String raw = dmy.group(1).replace('/', '-');
            try {
                return LocalDate.parse(raw, DateTimeFormatter.ofPattern("d-M-yyyy"));
            } catch (DateTimeParseException ignored) {}
        }
        Matcher dmyDot = DATE_DMY_DOTS.matcher(text);
        if (dmyDot.find()) {
            String raw = dmyDot.group(1).replace('.', '-');
            try {
                return LocalDate.parse(raw, DateTimeFormatter.ofPattern("d-M-yyyy"));
            } catch (DateTimeParseException ignored) {}
        }
        return LocalDate.now();
    }

    public static String extractMerchant(String text) {
        Matcher key = MERCHANT_KEY.matcher(text);
        if (key.find()) {
            return key.group(1).trim();
        }
        Matcher line = MERCHANT_LINE.matcher(text.trim());
        if (line.find()) {
            return line.group(1).trim();
        }
        // fallback to first plausible line with letters
        for (String l : text.split("\\R")) {
            String t = l.replaceAll("[^A-Za-z0-9 &.-]", "").trim();
            if (t.length() >= 3 && !t.toLowerCase().contains("total") && !t.toLowerCase().contains("amount")) {
                return t;
            }
        }
        return "Unknown";
    }
}
