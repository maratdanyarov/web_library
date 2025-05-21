-- Insert sample users (password is 'password' hashed with BCrypt)
INSERT INTO users (username, password_hash, email, first_name, last_name, role)
VALUES
    ('admin', '$2a$10$rU9HVBfOKDFI8wNSxrVxwu63K4UoYO6s2YhYh.OjdXwS3Xzz1dXpS', 'admin@library.com', 'Admin', 'User', 'ADMIN'),
    ('librarian', '$2a$10$rU9HVBfOKDFI8wNSxrVxwu63K4UoYO6s2YhYh.OjdXwS3Xzz1dXpS', 'librarian@library.com', 'John', 'Librarian', 'LIBRARIAN'),
    ('reader1', '$2a$10$rU9HVBfOKDFI8wNSxrVxwu63K4UoYO6s2YhYh.OjdXwS3Xzz1dXpS', 'reader1@example.com', 'Alice', 'Reader', 'READER'),
    ('reader2', '$2a$10$rU9HVBfOKDFI8wNSxrVxwu63K4UoYO6s2YhYh.OjdXwS3Xzz1dXpS', 'reader2@example.com', 'Bob', 'Reader', 'READER');

-- Insert genres
INSERT INTO genres (name) VALUES
                              ('Fiction'),
                              ('Science Fiction'),
                              ('Mystery'),
                              ('Romance'),
                              ('Biography'),
                              ('History'),
                              ('Science'),
                              ('Computer Science'),
                              ('Philosophy');

-- Insert sample books
INSERT INTO books (title, author, publisher, publication_year, isbn, description)
VALUES
    ('The Great Gatsby', 'F. Scott Fitzgerald', 'Scribner', 1925, '9780743273565', 'A novel about the mysterious millionaire Jay Gatsby and his obsession with the beautiful Daisy Buchanan.'),
    ('To Kill a Mockingbird', 'Harper Lee', 'J.B. Lippincott & Co.', 1960, '9780061120084', 'The story of racial inequality and moral growth as seen through the eyes of a young girl in Alabama.'),
    ('1984', 'George Orwell', 'Secker & Warburg', 1949, '9780451524935', 'A dystopian novel about a totalitarian regime where critical thought is suppressed.'),
    ('Pride and Prejudice', 'Jane Austen', 'T. Egerton', 1813, '9780141439518', 'A romantic novel following the character development of Elizabeth Bennet.'),
    ('The Hobbit', 'J.R.R. Tolkien', 'George Allen & Unwin', 1937, '9780618260300', 'A fantasy novel and prelude to The Lord of the Rings trilogy.'),
    ('Brave New World', 'Aldous Huxley', 'Chatto & Windus', 1932, '9780060850524', 'A dystopian novel examining the tension between individuality and societal conformity.'),
    ('The Catcher in the Rye', 'J.D. Salinger', 'Little, Brown and Company', 1951, '9780316769488', 'A novel depicting teenage angst and alienation.'),
    ('Clean Code', 'Robert C. Martin', 'Prentice Hall', 2008, '9780132350884', 'A handbook of agile software craftsmanship.'),
    ('Design Patterns', 'Erich Gamma et al.', 'Addison-Wesley', 1994, '9780201633610', 'Elements of Reusable Object-Oriented Software.');

-- Link books to genres
INSERT INTO book_genres (book_id, genre_id) VALUES
                                                (1, 1), -- Great Gatsby - Fiction
                                                (2, 1), -- To Kill a Mockingbird - Fiction
                                                (3, 1), (3, 2), -- 1984 - Fiction, Science Fiction
                                                (4, 1), (4, 4), -- Pride and Prejudice - Fiction, Romance
                                                (5, 1), (5, 2), -- The Hobbit - Fiction, Science Fiction
                                                (6, 1), (6, 2), -- Brave New World - Fiction, Science Fiction
                                                (7, 1), -- Catcher in the Rye - Fiction
                                                (8, 8), -- Clean Code - Computer Science
                                                (9, 8); -- Design Patterns - Computer Science

-- Create book copies
INSERT INTO book_copies (book_id, inventory_number, status) VALUES
                                                                (1, 'GATSBY-001', 'AVAILABLE'),
                                                                (1, 'GATSBY-002', 'AVAILABLE'),
                                                                (2, 'MOCKINGBIRD-001', 'AVAILABLE'),
                                                                (2, 'MOCKINGBIRD-002', 'AVAILABLE'),
                                                                (3, '1984-001', 'AVAILABLE'),
                                                                (3, '1984-002', 'AVAILABLE'),
                                                                (4, 'PRIDE-001', 'AVAILABLE'),
                                                                (5, 'HOBBIT-001', 'AVAILABLE'),
                                                                (6, 'BRAVE-001', 'AVAILABLE'),
                                                                (7, 'CATCHER-001', 'AVAILABLE'),
                                                                (8, 'CLEANCODE-001', 'AVAILABLE'),
                                                                (9, 'PATTERNS-001', 'AVAILABLE');

-- Sample orders
INSERT INTO orders (user_id, book_copy_id, order_type, status, approval_date, return_date, processed_by)
VALUES
    (3, 2, 'HOME', 'APPROVED', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 10 DAY), DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 10 DAY), 2),
    (4, 5, 'READING_ROOM', 'APPROVED', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 2),
    (3, 8, 'HOME', 'PENDING', NULL, NULL, NULL);

-- Update book copy status for issued books
UPDATE book_copies SET status = 'ISSUED' WHERE id IN (2, 5);
UPDATE book_copies SET status = 'RESERVED' WHERE id IN (8);