--liquibase formatted sql
--changeset mlyahmed:1 runOnChange:false
DELETE FROM PERSON
WHERE ID IN (1);
DELETE FROM AUTHENTICATION_INFO
WHERE ID IN (1);
DELETE FROM PERSON_PROFILE
WHERE PERSON_ID IN (1);
INSERT INTO AUTHENTICATION_INFO (ID, LOGIN, PASSWORD, STATUS, EFFECTIVE_AT, EXPIRES_AT) VALUES (1, 'ahmed.elidrissi.attach@gmail.com', '$2a$13$zEbHV1Wwiq/bEUyhOGhnyOkVvg/JzUOVIBN5a7gclZbgiV/2mGeNK', 'ACTIVE', '2018-01-14 18:28:39', '2222-02-14 18:28:56');
INSERT INTO PERSON (id, external_id, firstname, lastname, birthdate, email, authentication_info_id) VALUES (1, 'gestF', 'Ahmed', 'EL IDRISSI', '1984-10-17', 'ahmed.elidrissi.attach@gmail.com', 1);
INSERT INTO PERSON_PROFILE (PERSON_ID, PROFILE_CODE) VALUES (1, 'MASTER');

