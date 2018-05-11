package com.mlyauth.domain;

import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

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

    public static PersonByEmail newInstance() {
        return new PersonByEmail();
    }

    public long getId() {
        return id;
    }

    public PersonByEmail setId(long id) {
        this.id = id;
        return this;
    }

    public String getPersonId() {
        return personId;
    }

    public PersonByEmail setPersonId(String personId) {
        this.personId = personId;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public PersonByEmail setEmail(String email) {
        this.email = email;
        return this;
    }
}
