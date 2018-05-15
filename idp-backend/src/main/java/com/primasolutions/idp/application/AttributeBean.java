package com.primasolutions.idp.application;

public class AttributeBean implements Cloneable {

    private String code;
    private String alias;
    private String value;

    public AttributeBean() {

    }

    private AttributeBean(final String code) {
        this.code = code;
        this.alias = code;
    }

    public static AttributeBean newAttribute(final String uri) {
        return new AttributeBean(uri);
    }

    public String getCode() {
        return code;
    }

    public AttributeBean setCode(final String code) {
        this.code = code;
        return this;
    }

    public String getAlias() {
        return alias;
    }

    public AttributeBean setAlias(final String alias) {
        this.alias = alias;
        return this;
    }


    public String getValue() {
        return value;
    }

    public AttributeBean setValue(final String value) {
        this.value = value;
        return this;
    }

    public AttributeBean clone() {
        try {
            return (AttributeBean) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException();
        }
    }

}
