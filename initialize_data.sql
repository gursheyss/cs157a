INSERT INTO users (username, email, password_hash, first_name, last_name, role)
VALUES
('admin', 'admin@sjsu.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Admin', 'User', 'ADMIN'),
('organizer1', 'org1@sjsu.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Event', 'Planner', 'ORGANIZER'),
('student1', 'stu1@sjsu.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Alice', 'Smith', 'USER'),
('student2', 'stu2@sjsu.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Bob', 'Johnson', 'USER'),
('student3', 'stu3@sjsu.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Charlie', 'Brown', 'USER'),
('student4', 'stu4@sjsu.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Diana', 'Garcia', 'USER'),
('student5', 'stu5@sjsu.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Ethan', 'Miller', 'USER'),
('organizer2', 'org2@sjsu.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Campus', 'Life', 'ORGANIZER'),
('faculty1', 'fac1@sjsu.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Prof', 'Davis', 'USER'),
('student6', 'stu6@sjsu.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Fiona', 'Rodriguez', 'USER'),
('student7', 'stu7@sjsu.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'George', 'Martinez', 'USER'),
('student8', 'stu8@sjsu.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Hannah', 'Lee', 'USER'),
('student9', 'stu9@sjsu.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Ivan', 'Walker', 'USER'),
('student10', 'stu10@sjsu.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Julia', 'Hall', 'USER'),
('student11', 'stu11@sjsu.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Kevin', 'Allen', 'USER');

INSERT INTO events (title, description, location, start_time, end_time, category, organizer_id)
VALUES
('Spring Tech Fair', 'Annual tech fair showcasing student projects and industry partners.', 'SJSU Engineering Building', '2024-09-15 10:00:00', '2024-09-15 16:00:00', 'Technology', 2),
('Guest Lecture: AI Ethics', 'A talk by Dr. Jane Doe on the ethical implications of AI.', 'SJSU King Library Room 210', '2024-09-20 14:00:00', '2024-09-20 15:30:00', 'Academics', 1),
('Music Dept Concert', 'End-of-semester concert featuring student ensembles.', 'SJSU Music Building Concert Hall', '2024-12-05 19:30:00', '2024-12-05 21:00:00', 'Arts & Culture', 8),
('Campus Movie Night: Sci-Fi Classic', 'Outdoor screening of a classic Sci-Fi movie.', 'Tower Lawn', '2024-09-28 20:00:00', '2024-09-28 22:00:00', 'Entertainment', 8),
('Career Center Workshop: Resumes', 'Learn how to build an effective resume.', 'Career Center Room 101', '2024-10-02 11:00:00', '2024-10-02 12:00:00', 'Career Development', 1),
('Study Abroad Information Session', 'Find out about opportunities to study internationally.', 'Clark Hall 202', '2024-10-05 13:00:00', '2024-10-05 14:30:00', 'Academics', 2),
('Homecoming Football Game', 'SJSU Spartans vs. Rival Team.', 'CEFCU Stadium', '2024-10-12 15:00:00', '2024-10-12 18:00:00', 'Sports', 8),
('Volunteer Day: Park Cleanup', 'Join us to help clean up a local park.', 'Guadalupe River Park', '2024-10-19 09:00:00', '2024-10-19 12:00:00', 'Community Service', 2),
('Coding Club Meeting', 'Weekly meeting for coding enthusiasts.', 'MacQuarrie Hall 331', '2024-10-23 18:00:00', '2024-10-23 19:00:00', 'Student Organization', 1),
('Art Exhibit Opening', 'Opening reception for the student art gallery exhibition.', 'Art Building Gallery', '2024-10-25 17:00:00', '2024-10-25 19:00:00', 'Arts & Culture', 8),
('Yoga on the Lawn', 'Relaxing morning yoga session.', 'Tower Lawn', '2024-10-30 08:00:00', '2024-10-30 09:00:00', 'Health & Wellness', 8),
('Entrepreneurship Panel', 'Hear from successful local entrepreneurs.', 'Business Tower 110', '2024-11-06 16:00:00', '2024-11-06 17:30:00', 'Career Development', 2),
('Debate Club Tournament', 'Watch the SJSU Debate Club compete.', 'Student Union Ballroom', '2024-11-09 10:00:00', '2024-11-09 15:00:00', 'Student Organization', 1),
('International Students Mixer', 'Meet and greet for international students.', 'International House Lounge', '2024-11-15 18:00:00', '2024-11-15 20:00:00', 'Social', 8),
('Thanksgiving Potluck', 'Community potluck before the Thanksgiving break.', 'Campus Village Courtyard', '2024-11-21 17:00:00', '2024-11-21 19:00:00', 'Social', 2),
('Finals Week Stress Relief', 'Therapy dogs and relaxation activities.', 'King Library Lobby', '2024-12-10 11:00:00', '2024-12-10 14:00:00', 'Health & Wellness', 8);

INSERT INTO registrations (user_id, event_id)
VALUES
(3, 1), -- Alice Smith -> Spring Tech Fair
(4, 1), -- Bob Johnson -> Spring Tech Fair
(5, 2), -- Charlie Brown -> Guest Lecture: AI Ethics
(6, 3), -- Diana Garcia -> Music Dept Concert
(7, 4), -- Ethan Miller -> Campus Movie Night
(9, 5), -- Prof Davis -> Career Center Workshop
(10, 6), -- Fiona Rodriguez -> Study Abroad Info Session
(11, 7), -- George Martinez -> Homecoming Football Game
(12, 7), -- Hannah Lee -> Homecoming Football Game
(13, 8), -- Ivan Walker -> Volunteer Day
(14, 9), -- Julia Hall -> Coding Club Meeting
(15, 10), -- Kevin Allen -> Art Exhibit Opening
(3, 11), -- Alice Smith -> Yoga on the Lawn
(4, 12), -- Bob Johnson -> Entrepreneurship Panel
(5, 13), -- Charlie Brown -> Debate Club Tournament
(6, 14), -- Diana Garcia -> International Students Mixer
(7, 15), -- Ethan Miller -> Thanksgiving Potluck
(10, 16), -- Fiona Rodriguez -> Finals Week Stress Relief
(11, 1); -- George Martinez -> Spring Tech Fair 