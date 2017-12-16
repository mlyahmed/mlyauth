--liquibase formatted sql
--changeset mlyahmed:1 runOnChange:false
DELETE FROM person;
INSERT INTO person (id, username, password, firstname, lastname, email) VALUES (10000, 'root', 'root', 'Ahmed', 'EL IDRISSI', 'ahmed.elidrissi.attach@gmail.com');