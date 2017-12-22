--liquibase formatted SQL
--changeset mlyahmed:1 runOnChange:FALSE
DELETE FROM APPLICATION;
INSERT INTO APPLICATION (ID, APP_NAME, AUTH_ASPECT, TITLE) VALUES (1, 'Okta', 'AUTH_BASIC', 'Okta');