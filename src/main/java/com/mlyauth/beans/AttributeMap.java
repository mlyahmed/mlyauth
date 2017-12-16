package com.mlyauth.beans;

import java.util.Objects;

public class AttributeMap {

    private AttributeBean from;
    private AttributeBean to;
    private String value;

    public AttributeMap(){

    }

    public AttributeMap(AttributeBean from, AttributeBean to, String value) {
        this.from = from;
        this.to = to;
        this.value = value;
    }

    public AttributeBean getFrom() {
        return from;
    }

    public void setFrom(AttributeBean from) {
        this.from = from;
    }

    public AttributeBean getTo() {
        return to;
    }

    public void setTo(AttributeBean to) {
        this.to = to;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AttributeMap that = (AttributeMap) o;
        return Objects.equals(from, that.from) && Objects.equals(to, that.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to);
    }
}
