--liquibase formatted sql

--changeset ahmed.elidrissi:100 runOnChange:true
INSERT INTO APPLICATION (ID, TYPE, APP_NAME, TITLE)
VALUES (0, 'IDP', 'PrimaIDP', 'Prima IDP');

--changeset ahmed.elidrissi:200 runOnChange:true
INSERT INTO APPLICATION_ASPECT (APPLICATION_ID, ASPECT_CODE)
VALUES (0, 'RS_JOSE');

--changeset ahmed.elidrissi:201 runOnChange:true
INSERT INTO APPLICATION_ASPECT_ATTR (APPLICATION_ID, ASPECT_CODE, ATTRIBUTE_CODE, ATTRIBUTE_VALUE)
VALUES (0, 'RS_JOSE', 'Auth:RS:JOSE:Entity:ID', 'primainsureIDP');
