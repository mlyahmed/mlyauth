package com.primasolutions.idp.domain;

import com.primasolutions.idp.security.sensitive.domain.TokenizedEmail;
import com.primasolutions.idp.security.sensitive.domain.TokenizedLogin;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.MappedSuperclass;
import java.io.Serializable;

@TypeDefs(value = {
        @TypeDef(name = TokenizedDomain.TOKENIZED_EMAIL, typeClass = TokenizedEmail.class),
        @TypeDef(name = TokenizedDomain.TOKENIZED_LOGIN, typeClass = TokenizedLogin.class)
})
@MappedSuperclass
public interface TokenizedDomain  extends Serializable {
    String TOKENIZED_EMAIL = "tokenizedEmail";
    String TOKENIZED_LOGIN = "tokenizedLogin";
}
