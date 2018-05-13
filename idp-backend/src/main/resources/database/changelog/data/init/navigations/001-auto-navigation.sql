--liquibase formatted sql

--changeset ahmed.elidrissi:1 runOnChange:true
INSERT INTO AUTO_NAVIGATION (ID, ROLE, APPLICATION_TYPE) VALUES (1, 'CLIENT', 'POLICY');