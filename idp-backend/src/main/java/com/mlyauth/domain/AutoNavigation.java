package com.mlyauth.domain;

import javax.persistence.*;

@Entity
@Table(name="AUTO_NAVIGATION")
public class AutoNavigation {

    @Id
    @Column(name = "ID", nullable = false)
    @TableGenerator(name = "AUTO_NAVIGATION_ID", table = "IDS_SEQUENCES", pkColumnName = "SEQUENCENAME", valueColumnName = "SEQUENCEVALUE", pkColumnValue = "AUTO_NAVIGATION_ID", initialValue = 9999, allocationSize=1)
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

    public AutoNavigation setId(long id) {
        this.id = id;
        return this;
    }

    public Role getRole() {
        return role;
    }

    public AutoNavigation setRole(Role role) {
        this.role = role;
        return this;
    }

    public ApplicationType getApplicationType() {
        return applicationType;
    }

    public AutoNavigation setApplicationType(ApplicationType applicationType) {
        this.applicationType = applicationType;
        return this;
    }
}
