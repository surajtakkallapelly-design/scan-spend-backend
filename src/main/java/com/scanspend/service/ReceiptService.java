package com.scanspend.service;

import com.scanspend.dto.ExpenseDto;
import com.scanspend.dto.ReceiptUploadResponse;
import com.scanspend.model.Expense;
import com.scanspend.model.Receipt;
import com.scanspend.repository.ReceiptRepository;
import com.scanspend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ReceiptService {

    private final OcrService ocrService;
    private final ExpenseService expenseService;
    private final ReceiptRepository receiptRepository;
    private final UserRepository userRepository;

    public ReceiptService(OcrService ocrService, ExpenseService expenseService, ReceiptRepository receiptRepository, UserRepository userRepository) {
        this.ocrService = ocrService;
        this.expenseService = expenseService;
        this.receiptRepository = receiptRepository;
        this.userRepository = userRepository;
    }

    public ReceiptUploadResponse upload(String email, MultipartFile file) {
        var ocr = ocrService.process(file);
        ExpenseDto dto = new ExpenseDto(null, ocr.merchant(), ocr.amount(), ocr.category(), ocr.date(), ocr.savedPath());
        Expense expense = expenseService.create(email, dto);
        Receipt receipt = new Receipt();
        receipt.setImagePath(ocr.savedPath());
        receipt.setOcrText(ocr.rawText());
        receipt.setExpense(expense);
        receiptRepository.save(receipt);
        return new ReceiptUploadResponse(expense.getId(), ocr.merchant(), ocr.amount().toPlainString(),
                ocr.date().toString(), ocr.category(), ocr.savedPath());
    }
}
