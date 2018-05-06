package com.mlyauth.domain;

import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.jasypt.hibernate4.type.EncryptedStringType;

import javax.persistence.MappedSuperclass;
import java.io.Serializable;

@TypeDefs(value = {
        @TypeDef(name = EncryptedDomain.ENCRYPTED_STRING,
                typeClass = EncryptedStringType.class,
                parameters = { @Parameter(name = "encryptorRegisteredName", value = "hibernateStringEncryptor")})
})
@MappedSuperclass
public interface EncryptedDomain extends Serializable {
    String ENCRYPTED_STRING = "encryptedString";
}
