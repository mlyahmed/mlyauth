package com.primasolutions.idp.domain;

import com.primasolutions.idp.security.sensitive.domain.EncryptedDate;
import com.primasolutions.idp.security.sensitive.domain.EncryptedLong;
import com.primasolutions.idp.security.sensitive.domain.EncryptedString;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.jasypt.hibernate4.type.ParameterNaming;

import javax.persistence.MappedSuperclass;
import java.io.Serializable;

@TypeDefs(value = {
        @TypeDef(name = EncryptedDomain.ENCRYPTED_STRING, typeClass = EncryptedString.class,
                parameters = { @Parameter(name = ParameterNaming.ENCRYPTOR_NAME, value = "hibernateStringEncryptor")}),
        @TypeDef(name = EncryptedDomain.ENCRYPTED_LONG, typeClass = EncryptedLong.class,
                parameters = { @Parameter(name = ParameterNaming.ENCRYPTOR_NAME, value = "hibernateStringEncryptor")}),
        @TypeDef(name = EncryptedDomain.ENCRYPTED_DATE, typeClass = EncryptedDate.class,
                parameters = { @Parameter(name = ParameterNaming.ENCRYPTOR_NAME, value = "hibernateStringEncryptor")})
})
@MappedSuperclass
public interface EncryptedDomain extends Serializable {
    String ENCRYPTED_STRING = "encryptedString";
    String ENCRYPTED_LONG = "encryptedLong";
    String ENCRYPTED_DATE = "encryptedDate";
}
