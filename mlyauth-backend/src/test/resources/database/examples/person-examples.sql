--liquibase formatted sql
--changeset me:test1 runOnChange:true
INSERT INTO person (id, external_id, username, password, firstname, lastname, email)
VALUES (9000, '9000', 'mlyattach', 'mlyattach', 'Mly', 'ATTACH', 'moulay.attach@gmail.com');
INSERT INTO person (id, external_id, username, password, firstname, lastname, email)
VALUES (9001, '9001', 'fatima.elidrissi', '123', 'Fatima', 'EL IDRISSI', 'fatima.elidrissi@gmail.com');