-- Insert roles
INSERT INTO roles (id, name)
VALUES (1, 'ROLE_USER'),
       (2, 'ROLE_ADMIN');

INSERT INTO users (email,
                   username,
                   password,
                   first_name,
                   last_name,
                   father_name,
                   address,
                   zip_code,
                   city,
                   country,
                   link_to_first_passport_page,
                   link_to_second_passport_page,
                   is_enabled,
                   is_non_locked,
                   failed_login_attempts,
                   last_login_attempt
)
VALUES ('admin@gmail.com', -- email
        'admin', -- username
        '$2a$12$WD9GpGar..m.CKxfCV5B9.lKSJA5JrvluAai2tqvH836usZ1EjLiC', -- BCrypt encoded password for 'admin'
        'Admin', -- first_name
        'User', -- last_name
        NULL, -- father_name
        NULL, -- address
        NULL, -- zip_codeUser account is locked
        NULL, -- city
        NULL, -- country
        NULL, -- link_to_first_passport_page
        NULL, -- link_to_second_passport_page
        true,
        true,
        0,
        NULL);

-- Insert user_roles (assigning 'ROLE_ADMIN' to the first user)
INSERT INTO user_roles (user_id, role_id)
VALUES (1, 2);

INSERT INTO wallet (wallet, used, user_id)
VALUES ('wallet1', false, 1),
       ('wallet2', false, NULL),
       ('wallet3', false, NULL);