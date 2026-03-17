package com.scanspend.controller;

import com.scanspend.dto.ReceiptUploadResponse;
import com.scanspend.service.ReceiptService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/receipts")
public class ReceiptController {

    private final ReceiptService receiptService;

    public ReceiptController(ReceiptService receiptService) {
        this.receiptService = receiptService;
    }

    @PostMapping("/upload")
    public ResponseEntity<ReceiptUploadResponse> upload(@AuthenticationPrincipal UserDetails user,
                                                         @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(receiptService.upload(user.getUsername(), file));
    }
}
