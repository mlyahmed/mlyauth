--liquibase formatted SQL
--changeset mlyahmed:1 runOnChange:FALSE
DELETE FROM TOKEN_CLAIM;
DELETE FROM TOKEN;
DELETE FROM APPLICATION_ASPECT_ATTR;
DELETE FROM APPLICATION_ASPECT;
DELETE FROM APPLICATION;

INSERT INTO APPLICATION (ID, APP_NAME, TITLE) VALUES (0, 'PrimaIDP', 'Prima IDP');
INSERT INTO APPLICATION_ASPECT (APPLICATION_ID, ASPECT_CODE) VALUES (0, 'RS_JOSE');
INSERT INTO APPLICATION_ASPECT_ATTR (APPLICATION_ID, ASPECT_CODE, ATTRIBUTE_CODE, ATTRIBUTE_VALUE) VALUES (0, 'RS_JOSE', 'Auth:RS:JOSE:Entity:ID', 'primainsureIDP');

INSERT INTO AUTHENTICATION_INFO (ID, LOGIN, PASSWORD, STATUS, EFFECTIVE_AT, EXPIRES_AT) VALUES (1, 'cl.local-policy', '$2a$04$93MygtOiSNEL/4vmzdMJ1Ov1/8Vg13uCdjfUorfD/ToXnZfCJNUl6', 'ACTIVE', '2018-01-14 18:28:39', '2222-02-14 18:28:56');
INSERT INTO APPLICATION (ID, APP_NAME, TITLE, AUTHENTICATION_INFO_ID) VALUES (1, 'LocalPolicy', 'Localhost Policy', 1);

INSERT INTO APPLICATION_ASPECT (APPLICATION_ID, ASPECT_CODE) VALUES (1, 'SP_BASIC');

INSERT INTO APPLICATION_ASPECT (APPLICATION_ID, ASPECT_CODE) VALUES (1, 'SP_SAML');
INSERT INTO APPLICATION_ASPECT_ATTR (APPLICATION_ID, ASPECT_CODE, ATTRIBUTE_CODE, ATTRIBUTE_VALUE) VALUES (1, 'SP_SAML', 'Auth:SP:SAML:EndPoint', 'http://localhost:8889/primainsure/S/S/O/saml/SSO');
INSERT INTO APPLICATION_ASPECT_ATTR (APPLICATION_ID, ASPECT_CODE, ATTRIBUTE_CODE, ATTRIBUTE_VALUE) VALUES (1, 'SP_SAML', 'Auth:SP:SAML:Entity:ID', 'primainsure4sgi');
INSERT INTO APPLICATION_ASPECT_ATTR (APPLICATION_ID, ASPECT_CODE, ATTRIBUTE_CODE, ATTRIBUTE_VALUE)
VALUES (1, 'SP_SAML', 'Auth:SP:SAML:Encryption:Certificate',
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

INSERT INTO APPLICATION_ASPECT (APPLICATION_ID, ASPECT_CODE) VALUES (1, 'CL_JOSE');
INSERT INTO APPLICATION_ASPECT_ATTR (APPLICATION_ID, ASPECT_CODE, ATTRIBUTE_CODE, ATTRIBUTE_VALUE) VALUES (1, 'CL_JOSE', 'Auth:CL:JOSE:Context', 'http://localhost:8889');
INSERT INTO APPLICATION_ASPECT_ATTR (APPLICATION_ID, ASPECT_CODE, ATTRIBUTE_CODE, ATTRIBUTE_VALUE) VALUES (1, 'CL_JOSE', 'Auth:CL:JOSE:Entity:ID', 'local-policy');
INSERT INTO APPLICATION_ASPECT_ATTR (APPLICATION_ID, ASPECT_CODE, ATTRIBUTE_CODE, ATTRIBUTE_VALUE) VALUES (1, 'CL_JOSE', 'Auth:CL:JOSE:Encryption:Certificate',
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

INSERT INTO TOKEN (ID, VALIDATION_MODE, REFRESH_MODE, STAMP, CHECKSUM, TYPE, NORM, PURPOSE, ISSUANCE_TIME, EFFECTIVE_TIME, EXPIRY_TIME, STATUS, APPLICATION_ID, AUTHENTICATION_SESSION_ID) VALUES (1, 'STRICT', 'EACH_TIME', 'ace69807fed2faa4d2c3f79a27e68995681c8d91c0af2161e31bb008338877c7', '76e225585a1c3bc5addd258ebc3e41342971b4ccf31ab4866e7c366b9584d78b', 'REFRESH', 'JOSE', 'DELEGATION', '2018-03-18 14:06:02', '2018-03-18 14:07:27', '2021-03-18 14:07:32', 'READY', 1, null);
INSERT INTO TOKEN_CLAIM (ID, CODE, VALUE, TOKEN_ID) VALUES (1, 'audience', 'local-policy', 1);




INSERT INTO APPLICATION (ID, APP_NAME, TITLE) VALUES (4, 'LocalClaims', 'Localhost Claims');

INSERT INTO APPLICATION_ASPECT (APPLICATION_ID, ASPECT_CODE) VALUES (4, 'SP_SAML');
INSERT INTO APPLICATION_ASPECT_ATTR (APPLICATION_ID, ASPECT_CODE, ATTRIBUTE_CODE, ATTRIBUTE_VALUE) VALUES (4, 'SP_SAML', 'Auth:SP:SAML:EndPoint', 'http://localhost:7080/sgi-claims/sp/saml/sso');
INSERT INTO APPLICATION_ASPECT_ATTR (APPLICATION_ID, ASPECT_CODE, ATTRIBUTE_CODE, ATTRIBUTE_VALUE) VALUES (4, 'SP_SAML', 'Auth:SP:SAML:Entity:ID', 'claims4sgi');
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


INSERT INTO APPLICATION (ID, APP_NAME, TITLE) VALUES (2, 'SampleIDP', 'Sample IDP');
INSERT INTO APPLICATION_ASPECT (APPLICATION_ID, ASPECT_CODE) VALUES (2, 'IDP_JOSE');
INSERT INTO APPLICATION_ASPECT_ATTR (APPLICATION_ID, ASPECT_CODE, ATTRIBUTE_CODE, ATTRIBUTE_VALUE) VALUES (2, 'IDP_JOSE', 'Auth:IDP:JOSE:EndPoint', 'http://localhost:19999/idp/sso');
INSERT INTO APPLICATION_ASPECT_ATTR (APPLICATION_ID, ASPECT_CODE, ATTRIBUTE_CODE, ATTRIBUTE_VALUE) VALUES (2, 'IDP_JOSE', 'Auth:IDP:JOSE:Entity:ID', 'sample-idp');
INSERT INTO APPLICATION_ASPECT_ATTR (APPLICATION_ID, ASPECT_CODE, ATTRIBUTE_CODE, ATTRIBUTE_VALUE) VALUES (2, 'IDP_JOSE', 'Auth:IDP:JOSE:Encryption:Certificate',
        'MIICcjCCAdugAwIBAgIEWjnVPjANBgkqhkiG9w0BAQsFADBsMQswCQYDVQQGEwJG
        UjEWMBQGA1UECBMNSWxlLWRlLWZyYW5jZTEOMAwGA1UEBxMFUGFyaXMxDzANBgNV
        BAoTBlNBTVBMRTEPMA0GA1UECxMGU0FNUExFMRMwEQYDVQQDEwpJZHAgU0FNUExF
        MB4XDTE4MDMxNDE0MzkxM1oXDTQzMTAyOTE0MzkxM1owbDELMAkGA1UEBhMCRlIx
        FjAUBgNVBAgTDUlsZS1kZS1mcmFuY2UxDjAMBgNVBAcTBVBhcmlzMQ8wDQYDVQQK
        EwZTQU1QTEUxDzANBgNVBAsTBlNBTVBMRTETMBEGA1UEAxMKSWRwIFNBTVBMRTCB
        nzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEA1sVzlX38fvxXR5Hl/w3+zUPE1O/j
        gqnFKNIZVk7pq9R7UZDa/MNoHZ08tPwZsWbYF3CZf54yF8I/A1WOXrzLcXYP20hL
        yL4c2K4p+qrg/Y/EMj4l6XnaAaYWXiIvV6DKvEA3AYVM8CNp+yQqShkq4hstCyPz
        uXsCoc4SIweJruUCAwEAAaMhMB8wHQYDVR0OBBYEFPwK0M2fdgRSPmpNsMIx8t+P
        wlYCMA0GCSqGSIb3DQEBCwUAA4GBACbW+QOVOW/rpdxOLZCzTFrqlvcv/Y5RyJDT
        FJLnJAgoE+Qkj91InoBiGILeWVcD5tnyYNeuvtwJg0hukJZvpOmhkn8OBH2iifjc
        LpCx9qf2AnqNn51BTsvV/hmPdjofi5euvf1sU0gjxWr7Jg/4aFaSpKlGIhuK0ojp
        5+lHyfAJ');

INSERT INTO AUTHENTICATION_INFO (ID, LOGIN, PASSWORD, STATUS, EFFECTIVE_AT, EXPIRES_AT) VALUES (3, 'cl.sample-client', '$2a$04$eERhWgIVu3VoBrSf4V1dxegP7GDl5J.TZ8nL0uoHcf2NGnTsNLYfG', 'ACTIVE', '2018-01-14 18:28:39', '2222-02-14 18:28:56');
INSERT INTO APPLICATION (ID, APP_NAME, TITLE, AUTHENTICATION_INFO_ID) VALUES (3, 'SampleClient', 'Sample Client', 3);
INSERT INTO APPLICATION_ASPECT (APPLICATION_ID, ASPECT_CODE) VALUES (3, 'CL_JOSE');
INSERT INTO APPLICATION_ASPECT_ATTR (APPLICATION_ID, ASPECT_CODE, ATTRIBUTE_CODE, ATTRIBUTE_VALUE) VALUES (3, 'CL_JOSE', 'Auth:CL:JOSE:Context', 'http://localhost:17777');
INSERT INTO APPLICATION_ASPECT_ATTR (APPLICATION_ID, ASPECT_CODE, ATTRIBUTE_CODE, ATTRIBUTE_VALUE) VALUES (3, 'CL_JOSE', 'Auth:CL:JOSE:Entity:ID', 'sample-client');
INSERT INTO APPLICATION_ASPECT_ATTR (APPLICATION_ID, ASPECT_CODE, ATTRIBUTE_CODE, ATTRIBUTE_VALUE) VALUES (3, 'CL_JOSE', 'Auth:CL:JOSE:Encryption:Certificate',
        'MIIDfTCCAmWgAwIBAgIEGbQrAjANBgkqhkiG9w0BAQsFADBvMQswCQYDVQQGEwJG
        UjEWMBQGA1UECBMNSWxlLWRlLWZyYW5jZTEOMAwGA1UEBxMFUGFyaXMxDzANBgNV
        BAoTBlNBTVBMRTEPMA0GA1UECxMGU0FNUExFMRYwFAYDVQQDEw1TQU1QTEUgQ2xp
        ZW50MB4XDTE4MDMxNzIyMjMxOVoXDTQzMTEwMTIyMjMxOVowbzELMAkGA1UEBhMC
        RlIxFjAUBgNVBAgTDUlsZS1kZS1mcmFuY2UxDjAMBgNVBAcTBVBhcmlzMQ8wDQYD
        VQQKEwZTQU1QTEUxDzANBgNVBAsTBlNBTVBMRTEWMBQGA1UEAxMNU0FNUExFIENs
        aWVudDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBANHcg9JLd/FOAKJZ
        rAC/EslCZZ2Kc8l+VJbY2abwy7iZEQleYEOnsK3iYWbzFQLDKYqhHY97yt/W4mx/
        mQRrVfdZY9k0UTL7LMgytX+FGPrMzQNX9sVaxpV6Jee/Mpq397zouBuGnoJ/wOSL
        dPGBgO9gdjAwIk6DjvrDP3907sRznq+6rKoWw16KMLhTrWrItxr93RTJiwzfokf7
        44oo2YENql3lsqB/4/7pDJqQ62TmXJFbfyQaARXpYfzsiYaDjpbvcSD/epx4+NlO
        8jfRwOMkvwhmPh6SK+T7w2bQAZwy+d1e2CujjiI2oQe6ZmoPmXizIXKlafen1ymk
        u6rax6cCAwEAAaMhMB8wHQYDVR0OBBYEFG/YittSVZRI9nEz9SOLdx7SgE1NMA0G
        CSqGSIb3DQEBCwUAA4IBAQDCLy118KoMG9HCJk9qltjQnAXpLuUWp6BoHEsOHSrn
        3NaiGQica13m6Qdos7vUr8lmUpILNayMLap1WjtjMxGjiLhM61typttjddcObPN4
        wtMwoefdta+9SyRKEil4nAa5n0y/K/OL5EexInYEQRmdinaobxYiekBBikF+mazA
        HYVL3Xh8XDlyM0kuzJxO1CG4Fqk8U61FXhP70HCoFZ8zsGWXk7Rd1tERtKZ3WhmM
        WE9DIN7Ihv/LxbxZvzALMFmN22lBmyHD15UFMhXkUgf/Y7zllxd/cxXQnLb9/Glc
        uMR/bBUlYWWj6LhEp/wzd5DNfiEfUKlr2NKA5pyyC+vH');

INSERT INTO TOKEN (ID, VALIDATION_MODE, REFRESH_MODE, STAMP, CHECKSUM, TYPE, NORM, PURPOSE, ISSUANCE_TIME, EFFECTIVE_TIME, EXPIRY_TIME, STATUS, APPLICATION_ID, AUTHENTICATION_SESSION_ID) VALUES (3, 'STRICT', 'EACH_TIME', 'cd3a7dcb1130ad864939f1deea915b5e6643a8df9f5ff9da734faa7820c769f4', '954dcc7b498064f143056463136ede8bc6be2f96b029021a62d583b3f306999a', 'REFRESH', 'JOSE', 'DELEGATION', '2018-03-18 14:06:02', '2018-03-18 14:07:27', '2021-03-18 14:07:32', 'READY', 3, null);
INSERT INTO TOKEN_CLAIM (ID, CODE, VALUE, TOKEN_ID) VALUES (3, 'audience', 'sample-client', 3);