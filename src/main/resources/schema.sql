CREATE TABLE IF NOT EXISTS dog
(
    id   INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS urls
(
    id      INT AUTO_INCREMENT PRIMARY KEY,
    url     VARCHAR(250) NOT NULL UNIQUE,
    dog_id INT          NOT NULL,
    UNIQUE KEY `url_UNIQUE` (`url`),
    KEY `fk_url_dogs_idx` (`dog_id`),
    CONSTRAINT `fk_url_dogs` FOREIGN KEY (`dog_id`) REFERENCES `dog` (`id`) ON DELETE CASCADE
);
