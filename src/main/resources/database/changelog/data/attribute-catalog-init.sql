--liquibase formatted sql
--changeset mlyahmed:1 runOnChange:false
DELETE FROM ATTRIBUTE_CATALOG;
INSERT INTO ATTRIBUTE_CATALOG (CODE, CATEGORY, DEFAULT_VALUE, TITLE, DESCRIPTION) VALUES ('Auth:Basic:Username', 'AUTHENTICATION', null, 'Username', null);
INSERT INTO ATTRIBUTE_CATALOG (CODE, CATEGORY, DEFAULT_VALUE, TITLE, DESCRIPTION) VALUES ('Auth:Basic:Password', 'AUTHENTICATION', null, 'Password', null);
INSERT INTO ATTRIBUTE_CATALOG (CODE, CATEGORY, DEFAULT_VALUE, TITLE, DESCRIPTION) VALUES ('Auth:Basic:EndPoint', 'AUTHENTICATION', null, 'Endpoint', null);
--rollback DELETE FROM ATTRIBUTE_CATALOG;