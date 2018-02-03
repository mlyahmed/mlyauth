package com.mlyauth.beans;

import java.util.Collection;

public class AuthNavigation {


    private Collection<AttributeBean> attributes;
    private String posterPage;


    public Collection<AttributeBean> getAttributes() {
        return attributes;
    }

    public void setAttributes(Collection<AttributeBean> attributes) {
        this.attributes = attributes;
    }

    public String getPosterPage() {
        return posterPage;
    }

    public void setPosterPage(String posterPage) {
        this.posterPage = posterPage;
    }

}
