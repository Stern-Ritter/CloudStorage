DROP TABLE IF EXISTS users;
CREATE TABLE users (
id int auto_increment primary key,
login varchar(50) not null,
password varchar(255) not null
);