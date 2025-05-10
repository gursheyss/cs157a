-- Insert test users
INSERT INTO users (username, email, password_hash, first_name, last_name, role) 
VALUES 
('testadmin', 'admin@test.com', '$2a$10$1gJJgBlL75OIjkSgkYPXI.mV7ihEpjqTLMKNFnNnmLsX0LweouLVW', 'Test', 'Admin', 'ADMIN'),
('testorganizer', 'organizer@test.com', '$2a$10$1gJJgBlL75OIjkSgkYPXI.mV7ihEpjqTLMKNFnNnmLsX0LweouLVW', 'Test', 'Organizer', 'ORGANIZER'),
('testuser', 'user@test.com', '$2a$10$1gJJgBlL75OIjkSgkYPXI.mV7ihEpjqTLMKNFnNnmLsX0LweouLVW', 'Test', 'User', 'USER');

-- Insert test events
INSERT INTO events (title, description, location, start_time, end_time, category, organizer_id, max_attendees) 
VALUES 
('Test Conference', 'A test conference for testing', 'Test Location 1', '2023-12-01 09:00:00', '2023-12-01 17:00:00', 'Conference', 2, 100),
('Test Workshop', 'A test workshop for testing', 'Test Location 2', '2023-12-15 13:00:00', '2023-12-15 16:00:00', 'Workshop', 2, 50),
('Test Seminar', 'A test seminar for testing', 'Test Location 3', '2023-12-20 10:00:00', '2023-12-20 12:00:00', 'Seminar', 2, NULL);

-- Insert test registrations
INSERT INTO registrations (user_id, event_id, registration_time) 
VALUES 
(3, 1, '2023-11-15 12:00:00'),
(3, 2, '2023-11-16 10:30:00'),
(1, 3, '2023-11-17 09:45:00'),
(2, 3, '2023-11-18 14:20:00'); 