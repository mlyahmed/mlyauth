package com.mlyauth.domain;

import org.hibernate.annotations.Type;

import javax.persistence.*;

@Entity
@Table(name="PERSON_BY_EMAIL")
public class PersonByEmail implements TokenizedDomain, EncryptedDomain {

    @Id
    @Column(name = "ID", nullable = false)
    @TableGenerator(name = "PERSON_BY_EMAIL_ID", table = "IDS_SEQUENCES", pkColumnName = "SEQUENCENAME", pkColumnValue = "PERSON_BY_EMAIL_ID", valueColumnName = "SEQUENCEVALUE", initialValue = 9999, allocationSize=1)
    @GeneratedValue(generator = "PERSON_BY_EMAIL_ID", strategy = GenerationType.TABLE)
    private long id;

    @Column(name = "PERSON_ID", nullable = false)
    @Type(type = ENCRYPTED_STRING)
    private String personId;

    @Column(name = "EMAIL", nullable = false)
    @Type(type = TOKENIZED_EMAIL)
    private String email;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
