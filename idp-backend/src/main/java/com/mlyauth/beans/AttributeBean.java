package com.mlyauth.beans;

public class AttributeBean implements Cloneable{

    private String code;
    private String alias;
    private String value;

    public AttributeBean(){

    }

    private AttributeBean(String code) {
        this.code = this.alias = code;
    }

    public static AttributeBean newAttribute(String uri) {
        return new AttributeBean(uri);
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


    public String getValue() {
        return value;
    }

    public AttributeBean setValue(String value) {
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
