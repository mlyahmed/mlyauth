--liquibase formatted sql
--changeset mlyahmed:1 runOnChange:false
DELETE FROM ATTRIBUTE_CATALOG;
INSERT INTO ATTRIBUTE_CATALOG (CODE, TITLE, DESCRIPTION) VALUES ('Auth:Basic:EndPoint', 'Endpoint', NULL);
INSERT INTO ATTRIBUTE_CATALOG (CODE, TITLE, DESCRIPTION) VALUES ('Auth:Basic:Password', 'Password', NULL);
INSERT INTO ATTRIBUTE_CATALOG (CODE, TITLE, DESCRIPTION) VALUES ('Auth:Basic:Username', 'Username', NULL);

INSERT INTO ATTRIBUTE_CATALOG (CODE, TITLE, DESCRIPTION) VALUES ('Auth:SP:SAML:Entity:ID', 'Audience URI (SP Entity ID)', NULL);
INSERT INTO ATTRIBUTE_CATALOG (CODE, TITLE, DESCRIPTION) VALUES ('Auth:SP:SAML:EndPoint', 'Single sign on URL', NULL);
INSERT INTO ATTRIBUTE_CATALOG (CODE, TITLE, DESCRIPTION) VALUES ('Auth:SP:SAML:Encryption:Certificate', 'Encryption Certificate', NULL);

INSERT INTO ATTRIBUTE_CATALOG (CODE, TITLE, DESCRIPTION) VALUES ('Auth:IDP:JOSE:Entity:ID', 'The IDP ID', NULL);
INSERT INTO ATTRIBUTE_CATALOG (CODE, TITLE, DESCRIPTION) VALUES ('Auth:IDP:JOSE:EndPoint', 'Single sign on URL', NULL);
INSERT INTO ATTRIBUTE_CATALOG (CODE, TITLE, DESCRIPTION) VALUES ('Auth:IDP:JOSE:Encryption:Certificate', 'Encryption Certificate', NULL);

INSERT INTO ATTRIBUTE_CATALOG (CODE, TITLE, DESCRIPTION) VALUES ('Auth:CL:JOSE:Entity:ID', 'The Client ID', NULL);
INSERT INTO ATTRIBUTE_CATALOG (CODE, TITLE, DESCRIPTION) VALUES ('Auth:CL:JOSE:Context', 'The client application context URL', NULL);
INSERT INTO ATTRIBUTE_CATALOG (CODE, TITLE, DESCRIPTION) VALUES ('Auth:CL:JOSE:Encryption:Certificate', 'Encryption Certificate', NULL);
--rollback DELETE FROM ATTRIBUTE_CATALOG;