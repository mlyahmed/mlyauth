--liquibase formatted sql
--changeset mlyahmed:1 runOnChange:false
DELETE FROM PERSON;
DELETE FROM AUTHENTICATION_INFO;

INSERT
INTO AUTHENTICATION_INFO (ID, LOGIN, PASSWORD, STATUS, EFFECTIVE_ON, EXPIRE_ON)
VALUES (1, 'ahmed.elidrissi.attach@gmail.com', '$2a$13$zEbHV1Wwiq/bEUyhOGhnyOkVvg/JzUOVIBN5a7gclZbgiV/2mGeNK', 'ACTIVE',
        '2018-01-14 18:28:39', '2222-02-14 18:28:56');

INSERT
INTO PERSON (id, external_id, firstname, lastname, email, auth_information_id)
VALUES (1, '1', 'Ahmed', 'EL IDRISSI', 'ahmed.elidrissi.attach@gmail.com', 1);

