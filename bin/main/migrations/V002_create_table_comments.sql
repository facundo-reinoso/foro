CREATE TABLE comments (
    id bigint PRIMARY KEY AUTO_INCREMENT,
    parent_id varchar(50) NOT NULL,
    user_id varchar(50) NOT NULL,
    message varchar(150) NOT NULL,
    creation_date datetime NOT NULL,
    status varchar(50) NOT NULL,
);