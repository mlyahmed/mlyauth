--liquibase formatted sql
--changeset me:test1 runOnChange:true
INSERT INTO person (id, username, password, firstname, lastname, email) VALUES (125, 'mlyahmed', 'mlyahmed', 'Ahmed', 'EL IDRISSI', 'ahmed.elidrissi@prima-solutions.com');