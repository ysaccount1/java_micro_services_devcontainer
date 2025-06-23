-- Insert a test user if none exists
INSERT INTO users (username, password, email)
SELECT 'admin', 'password', 'admin@example.com'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');

INSERT INTO users (username, password, email)
SELECT 'user', 'password', 'user@example.com'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'user');