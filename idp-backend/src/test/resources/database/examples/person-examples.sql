--liquibase formatted sql

--changeset ahmed.elidrissi:1 runOnChange:true context:test
UPDATE person
SET firstname = 'ENC(MJHeuzNiuOxQ3GasAeleA323P36NGBLzccIkLra4MRI=)',
    lastname = 'ENC(ceSqPgyQOWrFI4UNAP+BszW5mnh1bWj6CggXF85iecg=)',
    email = 'ENC(AoYBfbRjSQkHLYkxDI7LR2hkCJ8DmThH8Bwg29AA6D6bOucCI60qCrpuYM1vlZUmKp4wge3UKMh38LFjx3VNcQ==)'
WHERE id=0;

--changeset ahmed.elidrissi:2 runOnChange:true context:test
UPDATE PERSON_BY_EMAIL
SET PERSON_ID = 'ENC(awvGbWnM2sQDG3Luorx6haOC/iKKwwEFDHOoF0SF1FU=)'
WHERE EMAIL = 'ahmed.elidr***********@gmail.com';

--changeset ahmed.elidrissi:3 runOnChange:true context:test
INSERT INTO AUTHENTICATION_INFO (ID, LOGIN, PASSWORD, STATUS, EFFECTIVE_AT, EXPIRES_AT) VALUES (9000, 'moulay.attach@gmail.com', '$2a$13$zEbHV1Wwiq/bEUyhOGhnyOkVvg/JzUOVIBN5a7gclZbgiV/2mGeNK', 'ACTIVE', '2018-01-14 18:28:39', '2222-02-14 18:28:56');
INSERT INTO person (id, role, external_id, firstname, lastname, birthdate, email, authentication_info_id) VALUES (9000, 'MANAGER', '9000', 'Moulay', 'ATTACH', '1984-10-17', 'moulay.attach@gmail.com', 9000);
INSERT INTO PERSON_BY_EMAIL (ID, PERSON_ID, EMAIL) VALUES (110, '9000', 'moulay*******@gmail.com');

--changeset ahmed.elidrissi:4 runOnChange:true context:test
INSERT INTO AUTHENTICATION_INFO (ID, LOGIN, PASSWORD, STATUS, EFFECTIVE_AT, EXPIRES_AT) VALUES (9001, 'fatima.elidrissi@gmail.com', '$2a$13$zEbHV1Wwiq/bEUyhOGhnyOkVvg/JzUOVIBN5a7gclZbgiV/2mGeNK', 'ACTIVE', '2018-01-14 18:28:39', '2222-02-14 18:28:56');
INSERT INTO person (id, role, external_id, firstname, lastname, birthdate, email, authentication_info_id) VALUES (9001, 'CLIENT', '9001', 'Fatima', 'EL IDRISSI', '1984-10-17', 'fatima.elidrissi@gmail.com', 9001);
INSERT INTO PERSON_BY_EMAIL (ID, PERSON_ID, EMAIL) VALUES (111, '9001', 'fatima.e********@gmail.com');