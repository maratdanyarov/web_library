-- Initial data for Library Management System
USE library;

-- Insert initial genres
INSERT INTO genres (name) VALUES
                              ('Fiction'),
                              ('Non-Fiction'),
                              ('Science Fiction'),
                              ('Mystery'),
                              ('Romance'),
                              ('Biography'),
                              ('History'),
                              ('Science'),
                              ('Technology'),
                              ('Philosophy'),
                              ('Poetry'),
                              ('Drama');

-- Insert initial admin user (password: admin123)
INSERT INTO users (username, password, email, first_name, last_name, role, status) VALUES
    ('admin', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'admin@library.com', 'Admin', 'User', 'ADMIN', 'ACTIVE');

-- Insert initial librarian (password: librarian123)
INSERT INTO users (username, password, email, first_name, last_name, role, status) VALUES
    ('librarian', '$2a$10$OvAK7z2QjKjTl0Q5M2qZ8uJV9dYkZKqQ0TY5F3x7vKZGF0.yT4Lgi', 'librarian@library.com', 'John', 'Librarian', 'LIBRARIAN', 'ACTIVE');

-- Insert sample reader users (password: reader123)
INSERT INTO users (username, password, email, first_name, last_name, role, status) VALUES
                                                                                       ('reader1', '$2a$10$OvAK7z2QjKjTl0Q5M2qZ8uJV9dYkZKqQ0TY5F3x7vKZGF0.yT4Lgi', 'reader1@example.com', 'Alice', 'Johnson', 'READER', 'ACTIVE'),
                                                                                       ('reader2', '$2a$10$OvAK7z2QjKjTl0Q5M2qZ8uJV9dYkZKqQ0TY5F3x7vKZGF0.yT4Lgi', 'reader2@example.com', 'Bob', 'Smith', 'READER', 'ACTIVE'),
                                                                                       ('reader3', '$2a$10$OvAK7z2QjKjTl0Q5M2qZ8uJV9dYkZKqQ0TY5F3x7vKZGF0.yT4Lgi', 'reader3@example.com', 'Carol', 'Davis', 'READER', 'ACTIVE');

-- Insert sample books
INSERT INTO books (title, author, publisher, publication_year, isbn, description) VALUES
                                                                                      ('To Kill a Mockingbird', 'Harper Lee', 'J.B. Lippincott & Co.', 1960, '9780061120084', 'A classic American novel dealing with serious issues of rape and racial inequality.'),
                                                                                      ('1984', 'George Orwell', 'Secker & Warburg', 1949, '9780451524935', 'A dystopian social science fiction novel about totalitarian control.'),
                                                                                      ('Pride and Prejudice', 'Jane Austen', 'T. Egerton', 1813, '9780141439518', 'A romantic novel of manners set in Georgian England.'),
                                                                                      ('The Great Gatsby', 'F. Scott Fitzgerald', 'Charles Scribners Sons', 1925, '9780743273565', 'A classic American novel set in the Jazz Age.'),
                                                                                      ('Dune', 'Frank Herbert', 'Chilton Books', 1965, '9780441172719', 'A science fiction novel set in the distant future.'),
                                                                                      ('The Catcher in the Rye', 'J.D. Salinger', 'Little, Brown and Company', 1951, '9780316769174', 'A controversial novel about teenage rebellion.'),
                                                                                      ('Lord of the Flies', 'William Golding', 'Faber & Faber', 1954, '9780571056866', 'A novel about British boys stranded on an uninhabited island.'),
                                                                                      ('Jane Eyre', 'Charlotte Brontë', 'Smith, Elder & Co.', 1847, '9780142437209', 'A bildungsroman following the experiences of its eponymous heroine.'),
                                                                                      ('The Hobbit', 'J.R.R. Tolkien', 'George Allen & Unwin', 1937, '9780547928227', 'A fantasy adventure novel and prelude to The Lord of the Rings.'),
                                                                                      ('Brave New World', 'Aldous Huxley', 'Chatto & Windus', 1932, '9780060850524', 'A dystopian social science fiction novel.');

-- Associate books with genres
INSERT INTO book_genres (book_id, genre_id) VALUES
                                                (1, 1), -- To Kill a Mockingbird - Fiction
                                                (2, 1), (2, 3), -- 1984 - Fiction, Science Fiction
                                                (3, 1), (3, 5), -- Pride and Prejudice - Fiction, Romance
                                                (4, 1), -- The Great Gatsby - Fiction
                                                (5, 3), -- Dune - Science Fiction
                                                (6, 1), -- The Catcher in the Rye - Fiction
                                                (7, 1), -- Lord of the Flies - Fiction
                                                (8, 1), (8, 5), -- Jane Eyre - Fiction, Romance
                                                (9, 1), -- The Hobbit - Fiction
                                                (10, 1), (10, 3); -- Brave New World - Fiction, Science Fiction

-- Insert book copies
INSERT INTO book_copies (book_id, inventory_number, status, location) VALUES
-- To Kill a Mockingbird (3 copies)
(1, 'LIB-001-001', 'AVAILABLE', 'A-1-01'),
(1, 'LIB-001-002', 'AVAILABLE', 'A-1-02'),
(1, 'LIB-001-003', 'ISSUED', 'A-1-03'),

-- 1984 (2 copies)
(2, 'LIB-002-001', 'AVAILABLE', 'A-1-04'),
(2, 'LIB-002-002', 'AVAILABLE', 'A-1-05'),

-- Pride and Prejudice (2 copies)
(3, 'LIB-003-001', 'AVAILABLE', 'A-2-01'),
(3, 'LIB-003-002', 'RESERVED', 'A-2-02'),

-- The Great Gatsby (1 copy)
(4, 'LIB-004-001', 'AVAILABLE', 'A-2-03'),

-- Dune (3 copies)
(5, 'LIB-005-001', 'AVAILABLE', 'B-1-01'),
(5, 'LIB-005-002', 'AVAILABLE', 'B-1-02'),
(5, 'LIB-005-003', 'ISSUED', 'B-1-03'),

-- The Catcher in the Rye (2 copies)
(6, 'LIB-006-001', 'AVAILABLE', 'A-3-01'),
(6, 'LIB-006-002', 'AVAILABLE', 'A-3-02'),

-- Lord of the Flies (1 copy)
(7, 'LIB-007-001', 'AVAILABLE', 'A-3-03'),

-- Jane Eyre (2 copies)
(8, 'LIB-008-001', 'AVAILABLE', 'A-4-01'),
(8, 'LIB-008-002', 'AVAILABLE', 'A-4-02'),

-- The Hobbit (4 copies)
(9, 'LIB-009-001', 'AVAILABLE', 'B-2-01'),
(9, 'LIB-009-002', 'AVAILABLE', 'B-2-02'),
(9, 'LIB-009-003', 'ISSUED', 'B-2-03'),
(9, 'LIB-009-004', 'AVAILABLE', 'B-2-04'),

-- Brave New World (1 copy)
(10, 'LIB-010-001', 'AVAILABLE', 'A-5-01');

-- Insert sample orders
INSERT INTO orders (user_id, book_copy_id, order_type, order_status, order_date, approval_date, return_date, processed_by) VALUES
-- Active orders
(3, 3, 'HOME', 'APPROVED', '2025-05-15 10:00:00', '2025-05-15 14:00:00', '2025-05-29 23:59:59', 2),
(4, 11, 'HOME', 'APPROVED', '2025-05-18 09:30:00', '2025-05-18 11:00:00', '2025-06-01 23:59:59', 2),
(5, 19, 'READING_ROOM', 'APPROVED', '2025-05-20 14:15:00', '2025-05-20 15:00:00', '2025-05-22 18:00:00', 2),

-- Pending orders
(3, 6, 'HOME', 'PENDING', '2025-05-21 16:20:00', NULL, NULL, NULL),
(4, 10, 'READING_ROOM', 'PENDING', '2025-05-22 11:45:00', NULL, NULL, NULL),

-- Historical completed orders
(3, 1, 'HOME', 'RETURNED', '2025-05-01 10:00:00', '2025-05-01 14:00:00', '2025-05-15 23:59:59', 2),
(4, 2, 'READING_ROOM', 'RETURNED', '2025-05-05 09:00:00', '2025-05-05 10:30:00', '2025-05-05 17:00:00', 2),
(5, 4, 'HOME', 'RETURNED', '2025-05-08 13:20:00', '2025-05-08 15:00:00', '2025-05-22 23:59:59', 2);

-- Update the status of reserved book copy
UPDATE book_copies SET status = 'RESERVED' WHERE id = 6;