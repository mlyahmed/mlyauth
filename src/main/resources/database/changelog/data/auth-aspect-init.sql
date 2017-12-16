--liquibase formatted sql
--changeset mlyahmed:1 runOnChange:false
DELETE FROM AUTH_ASPECT;
INSERT INTO AUTH_ASPECT (TYPE, TITLE, DESCRIPTION) VALUES ('AUTH_BASIC', 'Basic AUthentication', 'Http Basic AUthentication');
--rollback DELETE FROM AUTH_ASPECT;