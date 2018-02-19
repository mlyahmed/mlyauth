package com.mlyauth.domain;

import javax.persistence.*;

//@Entity
//@Table(name = "AUTHENTICATION_SESSION")
public abstract class AuthenticationSession {

    @Id
    @Column(name = "ID", nullable = false)
    @TableGenerator(name = "AUTHENTICATION_SESSION_ID", table = "IDS_SEQUENCES", pkColumnName = "SEQUENCENAME", valueColumnName = "SEQUENCEVALUE", pkColumnValue = "AUTHENTICATION_SESSION_ID", initialValue = 9999, allocationSize = 1)
    @GeneratedValue(generator = "AUTHENTICATION_SESSION_ID", strategy = GenerationType.TABLE)
    private long id;

    @Column(name = "CREATED_AT", nullable = false)
    private java.util.Date createdAt;
}
