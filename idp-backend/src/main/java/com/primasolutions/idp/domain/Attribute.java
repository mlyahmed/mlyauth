package com.primasolutions.idp.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "ATTRIBUTE_CATALOG")
public class Attribute implements Serializable {

    @Id
    @Column(name = "ID", nullable = false)
    private String code;

    @Column(name = "TITLE", nullable = false)
    private String title;

    @Column(name = "DESCRIPTION", nullable = false)
    private String description;

    public static Attribute newInstance() {
        return new Attribute();
    }

    public String getCode() {
        return code;
    }

    public Attribute setCode(final String code) {
        this.code = code;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Attribute setTitle(final String title) {
        this.title = title;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Attribute setDescription(final String description) {
        this.description = description;
        return this;
    }
}
