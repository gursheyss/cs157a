-- Drop tables if they exist to ensure clean initialization
DROP TABLE IF EXISTS event_attendees;
DROP TABLE IF EXISTS events;
DROP TABLE IF EXISTS users;

-- Create users table
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    role ENUM('ADMIN', 'ORGANIZER', 'USER') NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create events table
CREATE TABLE events (
    event_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    location VARCHAR(255) NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    organizer_id INT NOT NULL,
    max_attendees INT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (organizer_id) REFERENCES users(user_id)
);

-- Create event_attendees join table
CREATE TABLE event_attendees (
    event_id INT NOT NULL,
    user_id INT NOT NULL,
    registration_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('REGISTERED', 'ATTENDED', 'CANCELLED') DEFAULT 'REGISTERED',
    PRIMARY KEY (event_id, user_id),
    FOREIGN KEY (event_id) REFERENCES events(event_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- Add sample admin user
INSERT INTO users (username, email, password_hash, first_name, last_name, role)
VALUES ('admin', 'admin@sjsu.edu', '$2a$12$1234567890123456789012uqpeDu8Pd9bS3VnbQjuNhUUJZjhV6Yq', 'Admin', 'User', 'ADMIN');

-- Add sample events
INSERT INTO events (title, description, location, start_time, end_time, organizer_id, max_attendees, is_active)
VALUES 
('Spring Tech Fair', 'Annual tech fair showcasing student projects and industry partners.', 'SJSU Engineering Building', '2024-05-15 10:00:00', '2024-05-15 16:00:00', 1, 500, TRUE),
('Guest Lecture: AI Ethics', 'A talk by Dr. Jane Doe on the ethical implications of artificial intelligence.', 'SJSU King Library Room 210', '2024-05-20 14:00:00', '2024-05-20 15:30:00', 1, 100, TRUE),
('Music Department Concert', 'End-of-semester concert featuring student ensembles.', 'SJSU Music Building Concert Hall', '2024-05-22 19:30:00', '2024-05-22 21:00:00', 1, 200, TRUE),
('Campus Movie Night', 'Outdoor screening of a popular movie.', 'Tower Lawn', '2024-05-25 20:00:00', '2024-05-25 22:00:00', 1, 300, FALSE); -- Example of an inactive event 