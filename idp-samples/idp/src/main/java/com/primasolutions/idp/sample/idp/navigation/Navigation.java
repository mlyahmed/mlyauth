package com.primasolutions.idp.sample.idp.navigation;

import java.util.Set;

public class Navigation {

    private String target;
    private String bearer;
    private Set<NavigationAttribute> attributes;

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getBearer() {
        return bearer;
    }

    public void setBearer(String bearer) {
        this.bearer = bearer;
    }

    public Set<NavigationAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(Set<NavigationAttribute> attributes) {
        this.attributes = attributes;
    }


}
