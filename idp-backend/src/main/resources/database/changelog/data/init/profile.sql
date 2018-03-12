--liquibase formatted SQL
--changeset mlyahmed:1 runOnChange:FALSE
DELETE FROM PROFILE;
INSERT INTO PROFILE (CODE, DESCRIPTION) VALUES ('MASTER', 'The application master');
INSERT INTO PROFILE (CODE, DESCRIPTION) VALUES ('NAVIGATOR', 'A navigator from an IDP to a SP');