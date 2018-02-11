package com.mlyauth.domain;

import com.mlyauth.constants.AttributeCategory;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "ATTRIBUTE_CATALOG")
public class Attribute implements Serializable {

    @Id
    @Column(name = "ID", nullable = false)
    private String code;

    @Column(name = "CATEGORY", nullable = false)
    @Enumerated(EnumType.STRING)
    private AttributeCategory category;

    @Column(name = "DEFAULT_VALUE", nullable = false)
    private String defaultValue;

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

    public Attribute setCode(String code) {
        this.code = code;
        return this;
    }

    public AttributeCategory getCategory() {
        return category;
    }

    public Attribute setCategory(AttributeCategory category) {
        this.category = category;
        return this;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public Attribute setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Attribute setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Attribute setDescription(String description) {
        this.description = description;
        return this;
    }
}
