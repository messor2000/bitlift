CREATE TABLE roles (
                       id INTEGER PRIMARY KEY,
                       name VARCHAR(64) NOT NULL
);

CREATE TABLE users (
                       id SERIAL PRIMARY KEY,
                       email VARCHAR(255) NOT NULL,
                       username VARCHAR(64),
                       password VARCHAR(255),
                       first_name VARCHAR(64),
                       last_name VARCHAR(64),
                       picture_url VARCHAR(255),
                       UNIQUE(email)
);

CREATE TABLE user_roles (
                            user_id INTEGER,
                            role_id INTEGER,
                            PRIMARY KEY (user_id, role_id),
                            FOREIGN KEY (user_id) REFERENCES users (id),
                            FOREIGN KEY (role_id) REFERENCES roles (id)
);

-- Create wallet table
CREATE TABLE wallet (
                        id SERIAL PRIMARY KEY,
                        wallet VARCHAR(255) NOT NULL,
                        used BOOLEAN DEFAULT false,
                        user_id INTEGER,
                        FOREIGN KEY (user_id) REFERENCES users (id)
);
