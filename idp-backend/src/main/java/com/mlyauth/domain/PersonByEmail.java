package com.mlyauth.domain;

import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="PERSON_BY_EMAIL")
public class PersonByEmail implements TokenizedDomain, EncryptedDomain {

    @Id
    @Column(name = "PERSON_ID", nullable = false)
    @Type(type = ENCRYPTED_STRING)
    private String id;

    @Column(name = "EMAIL", nullable = false)
    @Type(type = TOKENIZED_EMAIL)
    private String email;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
