package com.mlyauth.domain;

import com.mlyauth.security.sensitive.domain.EncryptedString;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.jasypt.hibernate4.type.ParameterNaming;

import javax.persistence.MappedSuperclass;
import java.io.Serializable;

@TypeDefs(value = {
        @TypeDef(name = EncryptedDomain.ENCRYPTED_STRING,
                typeClass = EncryptedString.class,
                parameters = { @Parameter(name = ParameterNaming.ENCRYPTOR_NAME, value = "hibernateStringEncryptor")})
})
@MappedSuperclass
public interface EncryptedDomain extends Serializable {
    String ENCRYPTED_STRING = "encryptedString";
}
