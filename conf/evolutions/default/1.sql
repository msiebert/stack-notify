# patch 1
# initial setup

# --- !Ups
CREATE TABLE `users` (
	`id` BINARY(16) NOT NULL,
	`name` VARCHAR(50) NOT NULL,
	`google_id` VARCHAR(200) NOT NULL,
	`channel_id` VARCHAR(200),
	`access_token` VARCHAR(200),
	PRIMARY KEY (`id`),
	KEY `google_id` (`google_id`)
) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_general_ci;

CREATE TABLE `tags` (
	`id` BINARY(16) NOT NULL,
	`tag` VARCHAR(100) NOT NULL,
	PRIMARY KEY(`tag`)
) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_general_ci;

CREATE TABLE `users_tags` (
	`user_id` BINARY(16) NOT NULL,
	`tag_id` BINARY(16) NOT NULL,
	PRIMARY KEY(`user_id`, `tag_id`)
) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_general_ci;

# --- !Downs
DROP TABLE `users`;