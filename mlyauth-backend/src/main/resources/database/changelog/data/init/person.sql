--liquibase formatted sql
--changeset mlyahmed:1 runOnChange:false
DELETE FROM person;
INSERT INTO person (id, external_id, username, password, firstname, lastname, email) VALUES
  (1, '1', 'root', '$2a$13$zEbHV1Wwiq/bEUyhOGhnyOkVvg/JzUOVIBN5a7gclZbgiV/2mGeNK', 'Ahmed', 'EL IDRISSI',
   'ahmed.elidrissi.attach@gmail.com');