SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

DROP SCHEMA IF EXISTS `bs-auth` ;
CREATE SCHEMA IF NOT EXISTS `bs-auth` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci ;
USE `bs-auth` ;

-- -----------------------------------------------------
-- Table `sys_users`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `sys_users` ;

CREATE TABLE IF NOT EXISTS `sys_users` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(255) NOT NULL,
  `password` VARCHAR(255) NULL,
  `external_domain_link` VARCHAR(500) NULL,
  `email` VARCHAR(500) NULL,
  `default_access` VARCHAR(45) NOT NULL DEFAULT 'READONLY',
  `change_password` TINYINT(1) NOT NULL DEFAULT false,
  `active` TINYINT(1) NOT NULL DEFAULT true,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `username_UNIQUE` (`username` ASC))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `sys_profiles`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `sys_profiles` ;

CREATE TABLE IF NOT EXISTS `sys_profiles` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL,
  `description` VARCHAR(255) NULL DEFAULT NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_general_ci;


-- -----------------------------------------------------
-- Table `sys_rules`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `sys_rules` ;

CREATE TABLE IF NOT EXISTS `sys_rules` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL,
  `type` VARCHAR(10) NOT NULL,
  `value` VARCHAR(50) NOT NULL,
  `access` VARCHAR(10) NULL DEFAULT NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_general_ci;


-- -----------------------------------------------------
-- Table `sys_profile_rules_rel`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `sys_profile_rules_rel` ;

CREATE TABLE IF NOT EXISTS `sys_profile_rules_rel` (
  `profile_id` INT(11) NOT NULL,
  `rule_id` INT(11) NOT NULL,
  PRIMARY KEY (`profile_id`, `rule_id`),
  INDEX `fk_prr_2_idx` (`rule_id` ASC),
  CONSTRAINT `fk_prr_1`
    FOREIGN KEY (`profile_id`)
    REFERENCES `sys_profiles` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_prr_2`
    FOREIGN KEY (`rule_id`)
    REFERENCES `sys_rules` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_general_ci;


-- -----------------------------------------------------
-- Table `sys_user_profiles_rel`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `sys_user_profiles_rel` ;

CREATE TABLE IF NOT EXISTS `sys_user_profiles_rel` (
  `user_id` INT(11) NOT NULL,
  `profile_id` INT(11) NOT NULL,
  PRIMARY KEY (`user_id`, `profile_id`),
  INDEX `fk_upr_2_idx` (`profile_id` ASC),
  CONSTRAINT `fk_upr_1`
    FOREIGN KEY (`user_id`)
    REFERENCES `sys_users` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_upr_2`
    FOREIGN KEY (`profile_id`)
    REFERENCES `sys_profiles` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_general_ci;


-- -----------------------------------------------------
-- Table `sys_configuration`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `sys_configuration` ;

CREATE TABLE IF NOT EXISTS `sys_configuration` (
  `name` VARCHAR(255) NOT NULL,
  `value` VARCHAR(4000) NULL DEFAULT NULL,
  `description` VARCHAR(255) NULL DEFAULT NULL,
  PRIMARY KEY (`name`),
  UNIQUE INDEX `name_UNIQUE` (`name` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_general_ci;


-- -----------------------------------------------------
-- Table `sys_groups`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `sys_groups` ;

CREATE TABLE IF NOT EXISTS `sys_groups` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `sys_user_groups_rel`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `sys_user_groups_rel` ;

CREATE TABLE IF NOT EXISTS `sys_user_groups_rel` (
  `user_id` INT NOT NULL,
  `group_id` INT NOT NULL,
  PRIMARY KEY (`user_id`, `group_id`),
  INDEX `fk_ugr_2_idx` (`group_id` ASC),
  CONSTRAINT `fk_ugr_1`
    FOREIGN KEY (`user_id`)
    REFERENCES `sys_users` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_ugr_2`
    FOREIGN KEY (`group_id`)
    REFERENCES `sys_groups` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;




SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
