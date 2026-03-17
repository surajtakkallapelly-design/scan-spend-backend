CREATE DATABASE IF NOT EXISTS scanspend CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE scanspend;

CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE expenses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    merchant VARCHAR(255) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    category VARCHAR(120) NOT NULL,
    date DATE NOT NULL,
    receipt_image VARCHAR(255),
    user_id BIGINT,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE receipts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    image_path VARCHAR(255),
    ocr_text TEXT,
    expense_id BIGINT UNIQUE,
    CONSTRAINT fk_expense FOREIGN KEY (expense_id) REFERENCES expenses(id)
);
