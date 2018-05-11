package com.mlyauth.domain;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

@Entity
@Table(name = "AUTO_NAVIGATION")
public class AutoNavigation {

    public static final int ID_INIT_VALUE = 9999;
    public static final int ID_INC_STEP = 1;

    @Id
    @Column(name = "ID", nullable = false)
    @TableGenerator(name = "AUTO_NAVIGATION_ID", table = "IDS_SEQUENCES", pkColumnName = "SEQUENCENAME",
            valueColumnName = "SEQUENCEVALUE", pkColumnValue = "AUTO_NAVIGATION_ID",
            initialValue = ID_INIT_VALUE, allocationSize = ID_INC_STEP)
    @GeneratedValue(generator = "AUTO_NAVIGATION_ID", strategy = GenerationType.TABLE)
    private long id;


    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "ROLE")
    private Role role;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "APPLICATION_TYPE")
    private ApplicationType applicationType;

    public long getId() {
        return id;
    }

    public AutoNavigation setId(final long id) {
        this.id = id;
        return this;
    }

    public Role getRole() {
        return role;
    }

    public AutoNavigation setRole(final Role role) {
        this.role = role;
        return this;
    }

    public ApplicationType getApplicationType() {
        return applicationType;
    }

    public AutoNavigation setApplicationType(final ApplicationType applicationType) {
        this.applicationType = applicationType;
        return this;
    }
}
