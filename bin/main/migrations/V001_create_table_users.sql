CREATE TABLE IF NOT EXISTS users (
    id bigint PRIMARY KEY AUTO_INCREMENT,
    name varchar(50) NOT NULL,
    last_name varchar(50) NOT NULL,
    birth_date datetime NOT NULL,
    email varchar(100) NOT NULL,
    username varchar(50) NOT NULL,
    password varchar(20)  NOT NULL,
    status tinyint(1) NOT NULL,
    id_role bigint NOT NULL1
);