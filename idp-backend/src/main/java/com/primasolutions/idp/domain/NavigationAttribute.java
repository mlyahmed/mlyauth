package com.primasolutions.idp.domain;

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

    public static final int ID_INIT_VALUE = 9999;
    public static final int ID_INC_STEP = 1;

    @Id
    @Column(name = "ID", nullable = false)
    @TableGenerator(name = "NAVIGATION_ATTRIBUTE_ID", table = "IDS_SEQUENCES", pkColumnName = "SEQUENCENAME",
            pkColumnValue = "NAVIGATION_ATTRIBUTE_ID", valueColumnName = "SEQUENCEVALUE",
            initialValue = ID_INIT_VALUE, allocationSize = ID_INC_STEP)
    @GeneratedValue(generator = "NAVIGATION_ATTRIBUTE_ID", strategy = GenerationType.TABLE)
    private long id;

    @Column(name = "CODE", nullable = false)
    private String code;

    @Column(name = "ALIAS", nullable = false)
    private String alias;

    @Column(name = "VALUE", nullable = false)
    private String value;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "NAVIGATION_ID")
    private Navigation navigation;

    public static NavigationAttribute newInstance() {
        return new NavigationAttribute();
    }

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public NavigationAttribute setCode(final String code) {
        this.code = code;
        return this;
    }

    public String getAlias() {
        return alias;
    }

    public NavigationAttribute setAlias(final String alias) {
        this.alias = alias;
        return this;
    }

    public String getValue() {
        return value;
    }

    public NavigationAttribute setValue(final String value) {
        this.value = value;
        return this;
    }

    public Navigation getNavigation() {
        return navigation;
    }

    public void setNavigation(final Navigation navigation) {
        this.navigation = navigation;
    }
}
