package com.primasolutions.idp.person.model;

import com.primasolutions.idp.exception.IDPException;
import com.primasolutions.idp.sensitive.EncryptedDomain;
import com.primasolutions.idp.sensitive.TokenizedDomain;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import java.io.Serializable;

@Entity
@Table(name = "PERSON_BY_EMAIL")
public class PersonByEmail implements TokenizedDomain, EncryptedDomain, Cloneable, Serializable {

    public static final int ID_INIT_VALUE = 9999;
    public static final int ID_INC_STEP = 1;

    @Id
    @Column(name = "ID", nullable = false)
    @TableGenerator(name = "PERSON_BY_EMAIL_ID", table = "IDS_SEQUENCES", pkColumnName = "SEQUENCENAME",
            pkColumnValue = "PERSON_BY_EMAIL_ID", valueColumnName = "SEQUENCEVALUE",
            initialValue = ID_INIT_VALUE, allocationSize = ID_INC_STEP)
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

    public PersonByEmail setId(final long id) {
        this.id = id;
        return this;
    }

    public String getPersonId() {
        return personId;
    }

    public PersonByEmail setPersonId(final String personId) {
        this.personId = personId;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public PersonByEmail setEmail(final String email) {
        this.email = email;
        return this;
    }

    public PersonByEmail clone() {
        try {
            return (PersonByEmail) super.clone();
        } catch (CloneNotSupportedException e) {
            throw IDPException.newInstance(e);
        }
    }
}
