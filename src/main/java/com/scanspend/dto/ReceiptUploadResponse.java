package com.scanspend.dto;

public record ReceiptUploadResponse(Long expenseId,
                                    String merchant,
                                    String amount,
                                    String date,
                                    String category,
                                    String receiptImage) {}
