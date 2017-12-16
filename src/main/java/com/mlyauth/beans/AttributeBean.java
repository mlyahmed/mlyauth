package com.mlyauth.beans;

import com.mlyauth.constants.AttributeCategory;

import java.util.Objects;

import static com.mlyauth.constants.AttributeCategory.AUTHENTICATION;

public class AttributeBean {

    public final static AttributeBean BASIC_AUTH_USERNAME = new AttributeBean("Auth:Basic:Username", AUTHENTICATION);
    public final static AttributeBean BASIC_AUTH_PASSWORD = new AttributeBean("Auth:Basic:Password", AUTHENTICATION);
    public final static AttributeBean BASIC_AUTH_ENDPOINT = new AttributeBean("Auth:Basic:EndPoint", AUTHENTICATION);

    private String code;
    private AttributeCategory category;
    private String defaultValue;
    private boolean mandatory;

    public AttributeBean(){

    }

    public AttributeBean(String code, AttributeCategory category) {
        this.code = code;
        this.category = category;
        this.defaultValue = "";
        this.mandatory = false;
    }

    public AttributeBean(String code, AttributeCategory category, String defaultValue, boolean mandatory) {
        this.code = code;
        this.defaultValue = defaultValue;
        this.mandatory = mandatory;
        this.category = category;
    }


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

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public static AttributeBean createAuthAttr(String code){
        return new AttributeBean(code, AUTHENTICATION);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.equals(code, ((AttributeBean) o).code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }
}
