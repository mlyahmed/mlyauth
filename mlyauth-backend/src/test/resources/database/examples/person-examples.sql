--liquibase formatted sql
--changeset me:test1 runOnChange:true
INSERT INTO person (id, external_id, username, password, firstname, lastname, email)
VALUES (9000, '9000', 'mlyattach', 'mlyattach', '', 'ATTACH', 'moulay.attach@gmail.com');