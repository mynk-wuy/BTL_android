CREATE DATABASE smart_home;
USE smart_home;
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE,
    password VARCHAR(100),
    full_name VARCHAR(100),
    email VARCHAR(100),
    phone VARCHAR(20)
);
CREATE TABLE light_status (
    id INT AUTO_INCREMENT PRIMARY KEY,
    device VARCHAR(100),
    status VARCHAR(10),
    updated_at DATETIME
);
CREATE TABLE history_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100),
    device VARCHAR(100),
    status VARCHAR(10),
    time DATETIME
);
ALTER TABLE history_log
CHANGE username fullname VARCHAR(100);

ALTER TABLE users
CHANGE COLUMN full_name fullname VARCHAR(255);
ALTER TABLE users ADD COLUMN fingerprint_id VARCHAR(100) UNIQUE;




