-- Seed data for social media platform

-- Users
INSERT INTO users (username, email, password_hash, display_name, bio, avatar_url, is_verified)
VALUES
    ('alex_dev',    'alex@example.com',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Alex Chen',      'Full-stack developer & coffee enthusiast',     '/avatars/alex.jpg',  true),
    ('sarah_arts',  'sarah@example.com',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Sarah Williams', 'Digital artist | Oil painter',                   '/avatars/sarah.jpg', true),
    ('mike_runs',   'mike@example.com',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Mike Torres',    'Marathon runner, tech enthusiast',              '/avatars/mike.jpg',  false),
    ('emma_reads',  'emma@example.com',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Emma Foster',    'Bookworm | LitRPG author',                       '/avatars/emma.jpg',  false),
    ('james_foodie','james@example.com',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'James Park',     'Food blogger exploring hidden gems',             '/avatars/james.jpg', true);

-- Posts
INSERT INTO posts (user_id, content, image_url, visibility, like_count, comment_count)
VALUES
    (1, 'Just shipped a new feature using virtual threads in Java 21. The performance improvement is incredible!',       NULL,                    'PUBLIC',  0, 0),
    (1, 'My home office setup for 2025. Finally got the cable management right!',                                       '/images/office.jpg',    'PUBLIC',  0, 0),
    (2, 'Finished a new digital painting today. Took about 20 hours over two weeks.',                                   '/images/art.jpg',       'PUBLIC',  0, 0),
    (2, 'Oil painting workshop this weekend! Who else is going?',                                                        NULL,                    'PUBLIC',  0, 0),
    (3, 'New personal record: sub-3-hour marathon at Chicago! Hard work pays off.',                                      '/images/marathon.jpg',  'PUBLIC',  0, 0),
    (3, 'Best running shoes for spring training — my top 5 picks for 2025',                                              NULL,                    'PUBLIC',  0, 0),
    (4, 'Just finished "Project Hail Mary" by Andy Weir. Best sci-fi I have read this year!',                            NULL,                    'PUBLIC',  0, 0),
    (4, 'Writing update: Chapter 12 of my LitRPG novel is done. 50k words and counting!',                                NULL,                    'PUBLIC',  0, 0),
    (5, 'Found an amazing ramen spot in the East Village. The broth was simmered for 18 hours!',                         '/images/ramen.jpg',     'PUBLIC',  0, 0),
    (5, 'Homemade sourdough from my starter "Breadator". Best crust yet!',                                               '/images/sourdough.jpg', 'PUBLIC',  0, 0);

-- Comments
INSERT INTO comments (post_id, user_id, parent_id, content)
VALUES
    (1,  2, NULL, 'Virtual threads are a game changer! What framework are you using?'),
    (1,  1, 1,    'Spring Boot 3.2 with Tomcat — it was basically a config change.'),
    (1,  3, 1,    'Have you encountered any pinning issues with synchronized blocks?'),
    (3,  5, NULL, 'The color palette is stunning! What software do you use?'),
    (3,  2, 4,    'Thank you! This is Procreate on iPad Pro with custom brushes.'),
    (3,  4, 4,    'Do you sell prints? I would love this for my living room.'),
    (5,  1, NULL, 'Incredible time! What was your training split like?'),
    (5,  3, 7,    'Thanks! 5 runs/week with two speed sessions and one long run.'),
    (5,  2, 7,    'Congratulations Mike! That is amazing!'),
    (5,  4, 7,    'So inspiring! Do you follow any specific nutrition plan?'),
    (7,  1, NULL, 'That book is incredible. The audiobook is great too!'),
    (7,  3, 11,   'Rocky has nothing on Ryland Grace.'),
    (7,  5, 11,   'Have you read The Martian? Also by Andy Weir, also fantastic.'),
    (9,  2, NULL, 'Address please! I need to try this spot.'),
    (9,  5, 14,   'Tiny ramen joint on 3rd St between A and B. Hidden gem!'),
    (9,  4, 14,   'The tonkotsu broth looks incredible.'),
    (10, 1, NULL, 'That crumb looks perfect! What hydration percentage?'),
    (10, 5, 17,   '75% hydration with a 12-hour cold ferment. King Arthur flour.'),
    (10, 2, 17,   'The name Breadator is everything.'),
    (2,  3, NULL, 'Love the monitor setup! What model is that ultra-wide?');

-- Likes
INSERT INTO likes (user_id, likeable_type, likeable_id)
VALUES
    (2,  'POST', 1), (3,  'POST', 1), (4,  'POST', 1),
    (1,  'POST', 2), (5,  'POST', 2),
    (1,  'POST', 3), (4,  'POST', 3), (5,  'POST', 3),
    (1,  'POST', 5), (2,  'POST', 5), (4,  'POST', 5),
    (1,  'POST', 7), (3,  'POST', 7), (5,  'POST', 7),
    (1,  'POST', 9), (2,  'POST', 9), (4,  'POST', 9),
    (3,  'POST', 10), (1, 'POST', 10),
    (3,  'COMMENT', 1), (5,  'COMMENT', 1),
    (1,  'COMMENT', 4), (5,  'COMMENT', 4),
    (2,  'COMMENT', 7), (4,  'COMMENT', 7),
    (1,  'COMMENT', 14), (2, 'COMMENT', 14);

-- Friendships
INSERT INTO friendships (requester_id, addressee_id, status)
VALUES
    (1, 2, 'ACCEPTED'),
    (2, 3, 'ACCEPTED'),
    (3, 4, 'ACCEPTED'),
    (4, 5, 'ACCEPTED'),
    (1, 3, 'ACCEPTED'),
    (2, 5, 'ACCEPTED'),
    (1, 4, 'PENDING');
