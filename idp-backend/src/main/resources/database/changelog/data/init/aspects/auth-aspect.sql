--liquibase formatted sql
--changeset mlyahmed:1 runOnChange:true
INSERT INTO AUTH_ASPECT (TYPE, TITLE, DESCRIPTION) VALUES ('SP_BASIC', 'Basic AUthentication', 'Http Basic AUthentication');
INSERT INTO AUTH_ASPECT (TYPE, TITLE, DESCRIPTION) VALUES ('SP_SAML', 'SAML SP Authentication', 'SAML SP Authentication');
INSERT INTO AUTH_ASPECT (TYPE, TITLE, DESCRIPTION) VALUES ('IDP_JOSE', 'JOSE IDP Authentication', 'JOSE IDP Authentication');
INSERT INTO AUTH_ASPECT (TYPE, TITLE, DESCRIPTION) VALUES ('CL_JOSE', 'JOSE Client Authentication', 'JOSE Client Authentication');
INSERT INTO AUTH_ASPECT (TYPE, TITLE, DESCRIPTION) VALUES ('RS_JOSE', 'JOSE Resource Server Authentication', 'JOSE Resource Server Authentication');