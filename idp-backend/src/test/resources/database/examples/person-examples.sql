--liquibase formatted sql
--changeset me:test1 runOnChange:true

INSERT INTO AUTHENTICATION_INFO (ID, LOGIN, PASSWORD, STATUS, EFFECTIVE_AT, EXPIRES_AT) VALUES (9000, 'moulay.attach@gmail.com', '$2a$13$zEbHV1Wwiq/bEUyhOGhnyOkVvg/JzUOVIBN5a7gclZbgiV/2mGeNK', 'ACTIVE', '2018-01-14 18:28:39', '2222-02-14 18:28:56');
INSERT INTO person (id, role, external_id, firstname, lastname, birthdate, email, authentication_info_id) VALUES (9000, 'MANAGER', '9000', '0CR9ZhmN67x8idZmGB1Rb4WWxLLW1oLx7IukaNkkj+U=', 'ATTACH', '1984-10-17', 'moulay.attach@gmail.com', 9000);


INSERT INTO AUTHENTICATION_INFO (ID, LOGIN, PASSWORD, STATUS, EFFECTIVE_AT, EXPIRES_AT) VALUES (9001, 'fatima.elidrissi@gmail.com', '$2a$13$zEbHV1Wwiq/bEUyhOGhnyOkVvg/JzUOVIBN5a7gclZbgiV/2mGeNK', 'ACTIVE', '2018-01-14 18:28:39', '2222-02-14 18:28:56');
INSERT INTO person (id, role, external_id, firstname, lastname, birthdate, email, authentication_info_id) VALUES (9001, 'CLIENT', '9001', 'LEnKjkDK4aoLsN102Hll0/DeyxL2xWc2mPhMZD/Oaiw=', 'EL IDRISSI', '1984-10-17', 'fatima.elidrissi@gmail.com', 9001);