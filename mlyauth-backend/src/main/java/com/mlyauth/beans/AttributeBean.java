package com.mlyauth.beans;

import com.mlyauth.constants.AttributeCategory;

import static com.mlyauth.constants.AttributeCategory.AUTHENTICATION;

public class AttributeBean implements Cloneable{

    public final static AttributeBean BASIC_AUTH_USERNAME = new AttributeBean("Auth:Basic:Username", AUTHENTICATION);
    public final static AttributeBean BASIC_AUTH_PASSWORD = new AttributeBean("Auth:Basic:Password", AUTHENTICATION);
    public final static AttributeBean BASIC_AUTH_ENDPOINT = new AttributeBean("Auth:Basic:EndPoint", AUTHENTICATION);
    public final static AttributeBean SAML_RESPONSE_CLIENT_ID = new AttributeBean("idClient", AUTHENTICATION);
    public final static AttributeBean SAML_RESPONSE_PROFILE = new AttributeBean("profilUtilisateur", AUTHENTICATION);
    public final static AttributeBean SAML_RESPONSE_PRESTATION_ID = new AttributeBean("idPrestation", AUTHENTICATION);
    public final static AttributeBean SAML_RESPONSE_ACTION = new AttributeBean("action", AUTHENTICATION);
    public final static AttributeBean SAML_RESPONSE_APP = new AttributeBean("application", AUTHENTICATION);
    public final static AttributeBean SAML_RESPONSE = new AttributeBean("SAMLResponse", AUTHENTICATION);

    private String code;
    private String alias;
    private AttributeCategory category;
    private String defaultValue;
    private String value;
    private boolean mandatory;

    public AttributeBean(){

    }

    public AttributeBean(String code, AttributeCategory category) {
        this.code = this.alias = code;
        this.category = category;
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

    public AttributeBean setCode(String code) {
        this.code = code;
        return this;
    }

    public String getAlias() {
        return alias;
    }

    public AttributeBean setAlias(String alias) {
        this.alias = alias;
        return this;
    }

    public AttributeCategory getCategory() {
        return category;
    }

    public AttributeBean setCategory(AttributeCategory category) {
        this.category = category;
        return this;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public AttributeBean setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public String getValue() {
        return value;
    }

    public AttributeBean setValue(String value) {
        this.value = value;
        return this;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public AttributeBean setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
        return this;
    }

    public static AttributeBean createAuthAttr(String code){
        return new AttributeBean(code, AUTHENTICATION);
    }

    public AttributeBean clone() {
        try {
            return (AttributeBean) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException();
        }
    }

}
