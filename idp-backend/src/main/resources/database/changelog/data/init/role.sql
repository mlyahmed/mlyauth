--liquibase formatted SQL
--changeset mlyahmed:1 runOnChange:FALSE
DELETE FROM ROLE;
INSERT INTO ROLE (CODE, DESCRIPTION) VALUES ('ADMIN', 'Administrator');
INSERT INTO ROLE (CODE, DESCRIPTION) VALUES ('MANAGER', 'Portfolios Manager');
INSERT INTO ROLE (CODE, DESCRIPTION) VALUES ('CLIENT', 'A client / Insured');