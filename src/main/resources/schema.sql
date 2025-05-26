-- Library Management System Database Schema
-- Compatible with MySQL 5.5+ and MariaDB
-- UPDATED to use web_library database and remove IF NOT EXISTS for indexes

USE web_library;

-- Drop tables if they exist (for clean setup)
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS book_copies;
DROP TABLE IF EXISTS book_genres;
DROP TABLE IF EXISTS books;
DROP TABLE IF EXISTS genres;
DROP TABLE IF EXISTS users;

-- Users table
CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       first_name VARCHAR(50) NOT NULL,
                       last_name VARCHAR(50) NOT NULL,
                       role ENUM('READER', 'LIBRARIAN', 'ADMIN') NOT NULL DEFAULT 'READER',
                       status ENUM('ACTIVE', 'BLOCKED') NOT NULL DEFAULT 'ACTIVE',
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Add indexes for users table
CREATE INDEX idx_username ON users(username);
CREATE INDEX idx_email ON users(email);
CREATE INDEX idx_role ON users(role);
CREATE INDEX idx_status ON users(status);

-- Genres table
CREATE TABLE genres (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        name VARCHAR(100) NOT NULL UNIQUE,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add index for genres table
CREATE INDEX idx_genre_name ON genres(name);

-- Books table
CREATE TABLE books (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       title VARCHAR(255) NOT NULL,
                       author VARCHAR(255) NOT NULL,
                       publisher VARCHAR(255),
                       publication_year INT,
                       isbn VARCHAR(20) UNIQUE,
                       description TEXT,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Add indexes for books table
CREATE INDEX idx_title ON books(title);
CREATE INDEX idx_author ON books(author);
CREATE INDEX idx_isbn ON books(isbn);
CREATE INDEX idx_publication_year ON books(publication_year);

-- Book genres many-to-many relationship
CREATE TABLE book_genres (
                             book_id BIGINT NOT NULL,
                             genre_id BIGINT NOT NULL,
                             PRIMARY KEY (book_id, genre_id),
                             FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
                             FOREIGN KEY (genre_id) REFERENCES genres(id) ON DELETE CASCADE
);

-- Book copies table
CREATE TABLE book_copies (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             book_id BIGINT NOT NULL,
                             inventory_number VARCHAR(50) NOT NULL UNIQUE,
                             status ENUM('AVAILABLE', 'ISSUED', 'RESERVED', 'DAMAGED', 'LOST') NOT NULL DEFAULT 'AVAILABLE',
                             location VARCHAR(100),
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                             FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE
);

-- Add indexes for book_copies table
CREATE INDEX idx_book_copies_book_id ON book_copies(book_id);
CREATE INDEX idx_inventory_number ON book_copies(inventory_number);
CREATE INDEX idx_book_copy_status ON book_copies(status);

-- Orders table
CREATE TABLE orders (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        user_id BIGINT NOT NULL,
                        book_copy_id BIGINT NOT NULL,
                        order_type ENUM('HOME', 'READING_ROOM') NOT NULL,
                        order_status ENUM('PENDING', 'APPROVED', 'REJECTED', 'RETURNED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
                        order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        approval_date TIMESTAMP NULL,
                        return_date TIMESTAMP NULL,
                        actual_return_date TIMESTAMP NULL,
                        processed_by BIGINT NULL,
                        notes TEXT,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                        FOREIGN KEY (book_copy_id) REFERENCES book_copies(id) ON DELETE CASCADE,
                        FOREIGN KEY (processed_by) REFERENCES users(id) ON DELETE SET NULL
);

-- Add indexes for orders table
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_book_copy_id ON orders(book_copy_id);
CREATE INDEX idx_orders_status ON orders(order_status);
CREATE INDEX idx_orders_date ON orders(order_date);
CREATE INDEX idx_orders_return_date ON orders(return_date);
CREATE INDEX idx_orders_user_status ON orders(user_id, order_status);
CREATE INDEX idx_orders_overdue ON orders(order_status, return_date, actual_return_date);
CREATE INDEX idx_book_copies_book_status ON book_copies(book_id, status);