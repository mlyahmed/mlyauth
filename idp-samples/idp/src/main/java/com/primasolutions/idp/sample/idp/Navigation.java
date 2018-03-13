package com.primasolutions.idp.sample.idp;

import java.util.Set;

public class Navigation {

    private String target;
    private Set<NavigationAttribute> attributes;

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public Set<NavigationAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(Set<NavigationAttribute> attributes) {
        this.attributes = attributes;
    }


}
