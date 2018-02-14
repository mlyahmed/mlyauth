--liquibase formatted sql
--changeset me:test1 runOnChange:true

INSERT
INTO AUTH_INFORMATION (ID, LOGIN, PASSWORD, STATUS, EFFECTIVE_ON, EXPIRE_ON)
VALUES (9000, 'moulay.attach@gmail.com', '$2a$13$zEbHV1Wwiq/bEUyhOGhnyOkVvg/JzUOVIBN5a7gclZbgiV/2mGeNK', 'ACTIVE',
        '2018-01-14 18:28:39', '2222-02-14 18:28:56');

INSERT INTO person (id, external_id, firstname, lastname, email, auth_information_id)
VALUES (9000, '9000', 'Mly', 'ATTACH', 'moulay.attach@gmail.com', 9000);


INSERT
INTO AUTH_INFORMATION (ID, LOGIN, PASSWORD, STATUS, EFFECTIVE_ON, EXPIRE_ON)
VALUES (9001, 'fatima.elidrissi@gmail.com', '$2a$13$zEbHV1Wwiq/bEUyhOGhnyOkVvg/JzUOVIBN5a7gclZbgiV/2mGeNK', 'ACTIVE',
        '2018-01-14 18:28:39', '2222-02-14 18:28:56');

INSERT INTO person (id, external_id, firstname, lastname, email, auth_information_id)
VALUES (9001, '9001', 'Fatima', 'EL IDRISSI', 'fatima.elidrissi@gmail.com', 9001);