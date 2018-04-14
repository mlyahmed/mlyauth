--liquibase formatted sql
--changeset mlyahmed:1 runOnChange:true
DELETE FROM AUTO_NAVIGATION;
INSERT INTO AUTO_NAVIGATION (ID, ROLE, APPLICATION_TYPE) VALUES (1, 'CLIENT', 'POLICY');