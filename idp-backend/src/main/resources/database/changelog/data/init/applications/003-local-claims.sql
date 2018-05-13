--liquibase formatted sql

--changeset ahmed.elidrissi:100 runOnChange:true context:dev
INSERT INTO AUTHENTICATION_INFO (ID, LOGIN, PASSWORD, STATUS, EFFECTIVE_AT, EXPIRES_AT)
VALUES (4, 'local-claims', '$2a$04$5CfkC5repHlB.00fvuip4eKRntLdLToMK0xYO54pa3W/CmyldkNhK',
'ACTIVE', '2018-01-14 18:28:39', '2222-02-14 18:28:56');

INSERT INTO AUTHENTICATION_INFO_BY_LOGIN (ID, AUTHENTICATION_INFO_ID, LOGIN)
VALUES (4, '4', 'l***l******s');

INSERT INTO APPLICATION (ID, TYPE, APP_NAME, TITLE, AUTHENTICATION_INFO_ID)
VALUES (4, 'CLAIMS', 'LocalClaims', 'Local Claims', 4);

--changeset ahmed.elidrissi:200 runOnChange:true context:dev
INSERT INTO APPLICATION_PROFILE (APPLICATION_ID, PROFILE_CODE)
VALUES (4, 'FEEDER');

--changeset ahmed.elidrissi:300 runOnChange:true context:dev
INSERT INTO APPLICATION_ASPECT (APPLICATION_ID, ASPECT_CODE)
VALUES (4, 'SP_SAML');

--changeset ahmed.elidrissi:400 runOnChange:true context:dev
INSERT INTO APPLICATION_ASPECT_ATTR (APPLICATION_ID, ASPECT_CODE, ATTRIBUTE_CODE, ATTRIBUTE_VALUE)
VALUES (4, 'SP_SAML', 'Auth:SP:SAML:EndPoint', 'http://localhost:7080/sgi-claims/sp/saml/sso');

INSERT INTO APPLICATION_ASPECT_ATTR (APPLICATION_ID, ASPECT_CODE, ATTRIBUTE_CODE, ATTRIBUTE_VALUE)
VALUES (4, 'SP_SAML', 'Auth:SP:SAML:Entity:ID', 'claims4sgi');

INSERT INTO APPLICATION_ASPECT_ATTR (APPLICATION_ID, ASPECT_CODE, ATTRIBUTE_CODE, ATTRIBUTE_VALUE)
VALUES (4, 'SP_SAML', 'Auth:SP:SAML:Encryption:Certificate',
        'MIIFFTCCA/2gAwIBAgIRAJC77w46ZihMZ1XjYS8RfRYwDQYJKoZIhvcNAQELBQAw
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

--changeset ahmed.elidrissi:500 runOnChange:true context:dev
INSERT INTO APPLICATION_ASPECT (APPLICATION_ID, ASPECT_CODE)
VALUES (4, 'CL_JOSE');

--changeset ahmed.elidrissi:600 runOnChange:true context:dev
INSERT INTO APPLICATION_ASPECT_ATTR (APPLICATION_ID, ASPECT_CODE, ATTRIBUTE_CODE, ATTRIBUTE_VALUE)
VALUES (4, 'CL_JOSE', 'Auth:CL:JOSE:Context', 'http://localhost:7080');

INSERT INTO APPLICATION_ASPECT_ATTR (APPLICATION_ID, ASPECT_CODE, ATTRIBUTE_CODE, ATTRIBUTE_VALUE)
VALUES (4, 'CL_JOSE', 'Auth:CL:JOSE:Entity:ID', 'local-claims');

INSERT INTO APPLICATION_ASPECT_ATTR (APPLICATION_ID, ASPECT_CODE, ATTRIBUTE_CODE, ATTRIBUTE_VALUE)
VALUES (4, 'CL_JOSE', 'Auth:CL:JOSE:Encryption:Certificate',
        'MIIFFTCCA/2gAwIBAgIRAJC77w46ZihMZ1XjYS8RfRYwDQYJKoZIhvcNAQELBQAw
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

--changeset ahmed.elidrissi:700 runOnChange:true context:dev
INSERT INTO TOKEN (ID, VALIDATION_MODE, REFRESH_MODE, STAMP, CHECKSUM, TYPE, NORM, PURPOSE, ISSUANCE_TIME,
EFFECTIVE_TIME, EXPIRY_TIME, STATUS, APPLICATION_ID, AUTHENTICATION_SESSION_ID)
VALUES (4, 'STRICT', 'EACH_TIME', '31904197fd7e93d8874f229473a22c994ca34d8358153d03d84b8b18aa6adab5',
'82e8b44216c8d0860ad73439cf2f9127e6c042ed899b5b6609ebb0d90f5db276', 'REFRESH', 'JOSE', 'DELEGATION',
'2018-03-18 14:06:02', '2018-03-18 14:07:27', '2021-03-18 14:07:32', 'READY', 4, null);

INSERT INTO TOKEN_CLAIM (ID, CODE, VALUE, TOKEN_ID)
VALUES (4, 'audience', 'local-claims', 4);