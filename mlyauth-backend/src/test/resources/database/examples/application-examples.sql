--liquibase formatted SQL
--changeset mlyahmed:1 runOnChange:FALSE
DELETE FROM APPLICATION_ASPECT_ATTR;
DELETE FROM APPLICATION_ASPECT;
DELETE FROM PERSON_APPLICATION;
DELETE FROM APPLICATION;

INSERT INTO APPLICATION (ID, APP_NAME, TITLE) VALUES (9000, 'PolicyDev', 'Policy Dev');

INSERT INTO APPLICATION_ASPECT (APPLICATION_ID, ASPECT_CODE) VALUES (9000, 'SP_BASIC');
INSERT INTO APPLICATION_ASPECT (APPLICATION_ID, ASPECT_CODE) VALUES (9000, 'SP_SAML');
INSERT INTO APPLICATION_ASPECT (APPLICATION_ID, ASPECT_CODE) VALUES (9000, 'IDP_JOSE');

INSERT INTO APPLICATION_ASPECT_ATTR (APPLICATION_ID, ASPECT_CODE, ATTRIBUTE_CODE, ATTRIBUTE_VALUE)
VALUES (9000, 'SP_SAML', 'Auth:SP:SAML:EndPoint', 'http://localhost:8889/primainsure/S/S/O/saml/SSO');
INSERT INTO APPLICATION_ASPECT_ATTR (APPLICATION_ID, ASPECT_CODE, ATTRIBUTE_CODE, ATTRIBUTE_VALUE)
VALUES (9000, 'SP_SAML', 'Auth:SP:SAML:Entity:ID', 'primainsure4sgi');
INSERT INTO APPLICATION_ASPECT_ATTR (APPLICATION_ID, ASPECT_CODE, ATTRIBUTE_CODE, ATTRIBUTE_VALUE)
VALUES (9000, 'SP_SAML', 'Auth:SP:SAML:Encryption:Certificate', 'MIIFFTCCA/2gAwIBAgIRAJC77w46ZihMZ1XjYS8RfRYwDQYJKoZIhvcNAQELBQAw
XzELMAkGA1UEBhMCRlIxDjAMBgNVBAgTBVBhcmlzMQ4wDAYDVQQHEwVQYXJpczEO
MAwGA1UEChMFR2FuZGkxIDAeBgNVBAMTF0dhbmRpIFN0YW5kYXJkIFNTTCBDQSAy
MB4XDTE3MDUyNDAwMDAwMFoXDTE4MDUyNDIzNTk1OVowYjEhMB8GA1UECxMYRG9t
YWluIENvbnRyb2wgVmFsaWRhdGVkMRswGQYDVQQLExJHYW5kaSBTdGFuZGFyZCBT
U0wxIDAeBgNVBAMTF3NnaS5wcmltYS1zb2x1dGlvbnMuY29tMIIBIjANBgkqhkiG
9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnpSeVdowAqtclGgAHlFQ+rruYgsNYZ+7xeFS
tOB0Rrr7FCQAKvPXNoAD1lgKny/4Bs+UWtrhXwNxpibVvo0yCVSXctn+yQRBnLKJ
LsK8+2IfWHZrBHQiOAe2bc8mtW90XTRc2Jeb6ljPu61Uai17lXKXvHCafDkK6Xr5
F0SQKGMA65sqqnlVZyT45ZUO8Jgypqd/94COB+9nBeIsVrKBlSPbwFhd2olyGqQr
/yIlyNU7RnHtpSP+8JdNVH6S7dQR7wQt3oK907TfNSPa6RcD4yykrWDmz3yzqMLj
wsh7j6LCqjC37PEMk45Bq4r9ei2c6xx6AjyNYypo8KbYktctTwIDAQABo4IBxzCC
AcMwHwYDVR0jBBgwFoAUs5Cn2MmvTs1hPJ98rV1/Qf1pMOowHQYDVR0OBBYEFGJy
eFB1ZFxi8Oiu7k7VuJb2Z/fBMA4GA1UdDwEB/wQEAwIFoDAMBgNVHRMBAf8EAjAA
MB0GA1UdJQQWMBQGCCsGAQUFBwMBBggrBgEFBQcDAjBLBgNVHSAERDBCMDYGCysG
AQQBsjEBAgIaMCcwJQYIKwYBBQUHAgEWGWh0dHBzOi8vY3BzLnVzZXJ0cnVzdC5j
b20wCAYGZ4EMAQIBMEEGA1UdHwQ6MDgwNqA0oDKGMGh0dHA6Ly9jcmwudXNlcnRy
dXN0LmNvbS9HYW5kaVN0YW5kYXJkU1NMQ0EyLmNybDBzBggrBgEFBQcBAQRnMGUw
PAYIKwYBBQUHMAKGMGh0dHA6Ly9jcnQudXNlcnRydXN0LmNvbS9HYW5kaVN0YW5k
YXJkU1NMQ0EyLmNydDAlBggrBgEFBQcwAYYZaHR0cDovL29jc3AudXNlcnRydXN0
LmNvbTA/BgNVHREEODA2ghdzZ2kucHJpbWEtc29sdXRpb25zLmNvbYIbd3d3LnNn
aS5wcmltYS1zb2x1dGlvbnMuY29tMA0GCSqGSIb3DQEBCwUAA4IBAQCBHTW3H+WN
fMMEBVj93GshddJ+MgoGht6GBGSaBG09bAKmuiXOhNZU4QkOLBrNsUdg6NfbUytD
9m3GVo4TjJoEPFk+889Bz4kTQ4bwwPUa5BCkXsWUyPf8al2rTCjVCi8jkjzlo2++
ts3//2XzUUuFQpLzs47Qf8fUw+QPUDSSqYmG8Cw7AiTfyHkAXJwMfb1GxDcG+fLE
i76m5TOU6OWZoxXioggVufmge6rehuHQ4GHsM7qJUdBEekZNiDAauA+KgeT7Uzbd
pvPK19Sdo2FhgJvQvP34S7GsT7/7W7BeO8xNY1YiyIrcqzxwCB8kIsCTGCP5HLgl
YlSjs+QT20el');