-- Seed data for real-time chat application

-- Chat rooms
INSERT INTO chat_rooms (name, description, type, created_by, max_participants)
VALUES
    ('Java Developers', 'A room for Java enthusiasts to discuss code, frameworks, and best practices', 'GROUP', 1, 150),
    ('Project Alpha',   'Team channel for Project Alpha — sprint planning and daily standups',         'GROUP', 1, 20);

-- Messages
INSERT INTO messages (room_id, sender_id, sender_name, content, message_type)
VALUES
    (1, 1, 'Alex Chen',  'Welcome to the Java Developers room! Introduce yourselves.',               'TEXT'),
    (1, 2, 'Sarah Williams', 'Hey everyone! Excited to be here. Currently learning Spring Boot 3.',    'TEXT'),
    (1, 3, 'Mike Torres',    'Hey Sarah! Spring Boot 3 is great. The virtual threads support is amazing.','TEXT'),
    (1, 4, 'Emma Foster',    'Has anyone tried the new structured concurrency API in Java 21?',        'TEXT'),
    (1, 1, 'Alex Chen',  'Yes! StructuredTaskScope is a game changer for managing subtasks.',         'TEXT'),
    (1, 2, 'Sarah Williams', 'I will check that out. Thanks for the tip!',                             'TEXT'),
    (1, 5, 'James Park',     'Just joined. Java dev and foodie. Anyone else into both?',               'TEXT'),
    (1, 3, 'Mike Torres',    'Haha James, there is a surprising overlap between devs and cooking.',    'TEXT'),
    (2, 1, 'Alex Chen',  'Good morning team! Standup in 5 min. Who wants to start?',                 'TEXT'),
    (2, 3, 'Mike Torres',    'Morning! I will go first. Finished the API integration yesterday.',      'TEXT'),
    (2, 4, 'Emma Foster',    'Working on the database migration for the new schema. Should be done today.','TEXT'),
    (2, 5, 'James Park',     'UI changes are ready for review. Will open a PR after standup.',         'TEXT'),
    (2, 1, 'Alex Chen',  'Great progress team. Let us sync on the deployment timeline this afternoon.','TEXT'),
    (1, 2, 'Sarah Williams', 'By the way, any good Java conference recommendations for this year?',    'TEXT'),
    (1, 1, 'Alex Chen',  'Devoxx and JavaOne are the big ones. Also check out your local JUG meetups!','TEXT');
