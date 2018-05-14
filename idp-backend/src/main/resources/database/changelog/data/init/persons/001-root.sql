--liquibase formatted sql

--changeset ahmed.elidrissi:100 runOnChange:true
INSERT INTO AUTHENTICATION_INFO (ID, LOGIN, PASSWORD, STATUS, EFFECTIVE_AT, EXPIRES_AT)
VALUES (0, 'ENC(h0WlMYSqq/8jnD4V8yJIzOUIypoAP+f/WSOVXZEXVKREPfFdM+2kdPsd0kncoLNRv44d02OKn4LrWukYXlYDtQ==)',
'$2a$13$zEbHV1Wwiq/bEUyhOGhnyOkVvg/JzUOVIBN5a7gclZbgiV/2mGeNK', 'ACTIVE', '2018-01-14 18:28:39', '2222-02-14 18:28:56');

INSERT INTO AUTHENTICATION_INFO_BY_LOGIN (ID, AUTHENTICATION_INFO_ID, LOGIN)
VALUES (0, 'ENC(pAlPv+iFgCHbLuKvW96soyVGNIJGj4dqSITG/K9PIOb0hvMz1l5Sv8DaH6ZV92ngwx8tNMwSlTZhW2anD7eX4ZaacnldRdUufZ2hkXVFA6B9Qv535e55jpoqanXW3EPf)',
'ahmed.e***************@gmail.com');

INSERT INTO PERSON (id, role, external_id, firstname, lastname, birthdate, email, authentication_info_id)
VALUES (0, 'ADMIN', 'gestF', 'ENC(PZB8TELfqD8ZGe3ZS3ZQdy9jLJi1QG9597uxnaMZW3g=)',
'ENC(By5P5eseJ9eLQA7Qlf9i54EKtCCAY9JveV5j5xj8mT8=)', '1984-10-17',
'ENC(bQQIp9T8uFZWLNJ4iCXWVgKv3ooMNNiFpFNu6BsNsHob97n2F9AVnGR6kD4HnQSHXUIMcSmbek0+6ffHaR6lkg==)', 0);

INSERT INTO PERSON_BY_EMAIL (ID, PERSON_ID, EMAIL)
VALUES (11, 'ENC(NSDvKTdBfspA0x5pZtBpm4A8kivkzGp2XVZCXeLzPKU=)', 'ahmed.e***************@gmail.com');

INSERT INTO PERSON_PROFILE (PERSON_ID, PROFILE_CODE)
VALUES (0, 'MASTER');

--changeset ahmed.elidrissi:200 runOnChange:true context:dev
INSERT INTO PERSON_APPLICATION (PERSON_ID, APPLICATION_ID) VALUES (0, 1);
INSERT INTO PERSON_APPLICATION (PERSON_ID, APPLICATION_ID) VALUES (0, 4);

