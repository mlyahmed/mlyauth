--liquibase formatted sql
--changeset mlyahmed:1 runOnChange:false
DELETE FROM ATTRIBUTE_CATALOG;
INSERT INTO ATTRIBUTE_CATALOG (CODE, CATEGORY, DEFAULT_VALUE, TITLE, DESCRIPTION) VALUES ('Auth:Basic:EndPoint', 'AUTHENTICATION', null, 'Endpoint', null);
INSERT INTO ATTRIBUTE_CATALOG (CODE, CATEGORY, DEFAULT_VALUE, TITLE, DESCRIPTION)
VALUES ('Auth:Basic:Password', 'AUTHENTICATION', NULL, 'Password', NULL);
INSERT INTO ATTRIBUTE_CATALOG (CODE, CATEGORY, DEFAULT_VALUE, TITLE, DESCRIPTION)
VALUES ('Auth:Basic:Username', 'AUTHENTICATION', NULL, 'Username', NULL);

INSERT INTO ATTRIBUTE_CATALOG (CODE, CATEGORY, DEFAULT_VALUE, TITLE, DESCRIPTION)
VALUES ('Auth:SP:SAML:Entity:ID', 'AUTHENTICATION', NULL, 'Audience URI (SP Entity ID)', NULL);
INSERT INTO ATTRIBUTE_CATALOG (CODE, CATEGORY, DEFAULT_VALUE, TITLE, DESCRIPTION)
VALUES ('Auth:SP:SAML:EndPoint', 'AUTHENTICATION', NULL, 'Single sign on URL', NULL);
INSERT INTO ATTRIBUTE_CATALOG (CODE, CATEGORY, DEFAULT_VALUE, TITLE, DESCRIPTION)
VALUES ('Auth:SP:SAML:Encryption:Certificate', 'AUTHENTICATION', NULL, 'Encryption Certificate', NULL);

INSERT INTO ATTRIBUTE_CATALOG (CODE, CATEGORY, DEFAULT_VALUE, TITLE, DESCRIPTION)
VALUES ('Auth:IDP:JOSE:Entity:ID', 'AUTHENTICATION', NULL, 'The IDP ID', NULL);
INSERT INTO ATTRIBUTE_CATALOG (CODE, CATEGORY, DEFAULT_VALUE, TITLE, DESCRIPTION)
VALUES ('Auth:IDP:JOSE:EndPoint', 'AUTHENTICATION', NULL, 'Single sign on URL', NULL);
INSERT INTO ATTRIBUTE_CATALOG (CODE, CATEGORY, DEFAULT_VALUE, TITLE, DESCRIPTION)
VALUES ('Auth:IDP:JOSE:Encryption:Certificate', 'AUTHENTICATION', NULL, 'Encryption Certificate', NULL);

--rollback DELETE FROM ATTRIBUTE_CATALOG;