package com.mlyauth.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

@Entity
@Table(name = "NAVIGATION_ATTRIBUTE")
public class NavigationAttribute {

    @Id
    @Column(name = "ID", nullable = false)
    @TableGenerator(name = "NAVIGATION_ATTRIBUTE_ID", table = "IDS_SEQUENCES", pkColumnName = "SEQUENCENAME", pkColumnValue = "NAVIGATION_ATTRIBUTE_ID", valueColumnName = "SEQUENCEVALUE", initialValue = 9999, allocationSize = 1)
    @GeneratedValue(generator = "NAVIGATION_ATTRIBUTE_ID", strategy = GenerationType.TABLE)
    private long id;

    public static NavigationAttribute newInstance() {
        return new NavigationAttribute();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Column(name = "CODE", nullable = false)
    private String code;

    @Column(name = "ALIAS", nullable = false)
    private String alias;

    @Column(name = "VALUE", nullable = false)
    private String value;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "NAVIGATION_ID")
    private Navigation navigation;

    public String getCode() {
        return code;
    }

    public NavigationAttribute setCode(String code) {
        this.code = code;
        return this;
    }

    public String getAlias() {
        return alias;
    }

    public NavigationAttribute setAlias(String alias) {
        this.alias = alias;
        return this;
    }

    public String getValue() {
        return value;
    }

    public NavigationAttribute setValue(String value) {
        this.value = value;
        return this;
    }

    public Navigation getNavigation() {
        return navigation;
    }

    public void setNavigation(Navigation navigation) {
        this.navigation = navigation;
    }
}
