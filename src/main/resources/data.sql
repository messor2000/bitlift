-- Insert roles
INSERT INTO roles (id, name) VALUES (1, 'ROLE_USER'), (2, 'ROLE_ADMIN');

-- Insert users
INSERT INTO users (email, username, password, first_name, last_name, picture_url)
VALUES (
           'admin@gmail.com',
           'admin',
           '$2a$12$WD9GpGar..m.CKxfCV5B9.lKSJA5JrvluAai2tqvH836usZ1EjLiC', -- BCrypt encoded password for 'admin'
           'Admin',
           'User',
           'admin-picture-url'
       );

-- Insert user_roles (assigning 'ROLE_ADMIN' to the first user)
INSERT INTO user_roles (user_id, role_id) VALUES (1, 2);