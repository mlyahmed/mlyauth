--liquibase formatted SQL
--changeset mlyahmed:1 runOnChange:FALSE
DELETE FROM PERSON_APPLICATION;
INSERT INTO PERSON_APPLICATION (PERSON_ID, APPLICATION_ID) VALUES (9000, 9000);