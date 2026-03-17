package com.scanspend.service;

import com.scanspend.util.CategoryGuesser;
import com.scanspend.util.OcrParser;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.UUID;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.color.ColorSpace;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.geom.AffineTransform;
import javax.imageio.ImageIO;

@Service
public class OcrService {

    static {
        // Best effort to preload native lib for Tess4J on macOS/Homebrew
        String libPath = "/opt/homebrew/opt/tesseract/lib/libtesseract.dylib";
        try {
            System.load(libPath);
        } catch (UnsatisfiedLinkError ignored) {
            // fall back to JNA path resolution
        }
    }

    private final Path uploadDir;

    public record OcrResult(String merchant, BigDecimal amount, LocalDate date, String category, String rawText, String savedPath) { }

    public OcrService(@Value("${app.upload-dir:uploads}") String uploadDir) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath();
        try {
            Files.createDirectories(this.uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to prepare upload directory", e);
        }
    }

    public OcrResult process(MultipartFile file) {
        String original = file.getOriginalFilename();
        String ext = (original != null && original.contains(".")) ? original.substring(original.lastIndexOf('.')) : ".jpg";
        String storedName = UUID.randomUUID() + ext;
        Path saved = uploadDir.resolve(storedName);
        try {
            Files.copy(file.getInputStream(), saved);

            String text = extractText(saved);
            BigDecimal amount = extractAmountWithFallback(saved, text);
            String merchant = OcrParser.extractMerchant(text);
            LocalDate date = OcrParser.extractDate(text);
            String category = CategoryGuesser.guess(merchant, text);
            String publicPath = "/uploads/" + storedName;
            return new OcrResult(merchant, amount, date, category, text, publicPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to process receipt", e);
        }
    }

    private BigDecimal extractAmountWithFallback(Path saved, String text) {
        BigDecimal primary = OcrParser.extractAmount(text);
        BigDecimal col = runDigitsPass(saved);
        BigDecimal fullDigits = runDigitsOnly(saved);
        BigDecimal max = primary.max(col).max(fullDigits);
        if (max.compareTo(new BigDecimal("500")) < 0) {
            return primary.max(col).max(fullDigits);
        }
        return max;
    }

    private String extractText(Path saved) {
        try {
            // Ensure JNA knows where tesseract native libs and data live
            System.setProperty("jna.library.path", "/opt/homebrew/opt/tesseract/lib");
            System.setProperty("TESSDATA_PREFIX", "/opt/homebrew/share/tessdata");

            Tesseract tesseract = new Tesseract();
            // Homebrew path for tessdata on macOS; override with TESSDATA_PREFIX if present
            String dataPath = System.getenv().getOrDefault("TESSDATA_PREFIX", "/opt/homebrew/share/tessdata");
            tesseract.setDatapath(dataPath);
            tesseract.setLanguage("eng");
            tesseract.setPageSegMode(6); // Assume a block of text
            tesseract.setOcrEngineMode(1); // LSTM only
            tesseract.setTessVariable("user_defined_dpi", "300");
            tesseract.setTessVariable("tessedit_char_whitelist", "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789:/.- ");

            BufferedImage pre = preprocess(saved);
            if (pre == null) {
                return saved.getFileName().toString();
            }
            return tesseract.doOCR(pre);
        } catch (TesseractException e) {
            // Fallback: return filename to keep pipeline alive instead of hanging UI
            return saved.getFileName().toString();
        }
    }

    private BigDecimal runDigitsOnly(Path saved) {
        try {
            BufferedImage pre = preprocess(saved);
            if (pre == null) return BigDecimal.ZERO;
            Tesseract tesseract = new Tesseract();
            String dataPath = System.getenv().getOrDefault("TESSDATA_PREFIX", "/opt/homebrew/share/tessdata");
            tesseract.setDatapath(dataPath);
            tesseract.setLanguage("eng");
            tesseract.setPageSegMode(6);
            tesseract.setOcrEngineMode(1);
            tesseract.setTessVariable("user_defined_dpi", "300");
            tesseract.setTessVariable("tessedit_char_whitelist", "0123456789.");
            String text = tesseract.doOCR(pre);
            return OcrParser.extractAmount(text);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal runDigitsPass(Path saved) {
        try {
            BufferedImage src = ImageIO.read(saved.toFile());
            if (src == null) return BigDecimal.ZERO;
            // crop rightmost 35% where totals usually sit
            int x = (int) (src.getWidth() * 0.65);
            int w = src.getWidth() - x;
            BufferedImage crop = src.getSubimage(x, 0, w, src.getHeight());

            BufferedImage pre = preprocess(crop);
            Tesseract tesseract = new Tesseract();
            String dataPath = System.getenv().getOrDefault("TESSDATA_PREFIX", "/opt/homebrew/share/tessdata");
            tesseract.setDatapath(dataPath);
            tesseract.setLanguage("eng");
            tesseract.setPageSegMode(6);
            tesseract.setOcrEngineMode(1);
            tesseract.setTessVariable("user_defined_dpi", "300");
            tesseract.setTessVariable("tessedit_char_whitelist", "0123456789.");

            String text = tesseract.doOCR(pre);
            return OcrParser.extractAmount(text);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private BufferedImage preprocess(Path saved) {
        try {
            BufferedImage src = ImageIO.read(saved.toFile());
            if (src == null) return null;
            return preprocess(src);
        } catch (IOException e) {
            return null;
        }
    }

    private BufferedImage preprocess(BufferedImage src) {
        // Upscale 2x for better OCR on small text
        int w = src.getWidth() * 2;
        int h = src.getHeight() * 2;
        Image scaled = src.getScaledInstance(w, h, Image.SCALE_SMOOTH);
        BufferedImage up = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = up.createGraphics();
        g2d.drawImage(scaled, 0, 0, null);
        g2d.dispose();

        // Grayscale
        ColorConvertOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
        BufferedImage gray = op.filter(up, null);

        // Simple threshold
        BufferedImage bin = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g = bin.createGraphics();
        g.drawImage(gray, 0, 0, null);
        g.dispose();
        return bin;
    }
}
