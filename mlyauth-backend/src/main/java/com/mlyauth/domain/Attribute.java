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


    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public AttributeCategory getCategory() {
        return category;
    }

    public void setCategory(AttributeCategory category) {
        this.category = category;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
