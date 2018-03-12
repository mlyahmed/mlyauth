package com.mlyauth.domain;

import com.mlyauth.constants.AspectType;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

@Entity
@Table(name="APPLICATION")
public class Application  implements Serializable {

    @Id
    @Column(name = "ID", nullable = false)
    @TableGenerator(name = "APPLICATION_ID", table = "IDS_SEQUENCES", pkColumnName = "SEQUENCENAME", valueColumnName = "SEQUENCEVALUE", pkColumnValue = "APPLICATION_ID", initialValue = 9999, allocationSize=1)
    @GeneratedValue(generator = "APPLICATION_ID", strategy = GenerationType.TABLE)
    private long id;


    @Column(name = "APP_NAME", nullable = false, unique = true)
    private String appname;

    @Column(name = "TITLE", nullable = false)
    private String title;

    @ElementCollection(targetClass = AspectType.class, fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "APPLICATION_ASPECT", joinColumns = @JoinColumn(name = "APPLICATION_ID"))
    @Column(name = "ASPECT_CODE")
    private Set<AspectType> aspects;

    public static Application newInstance(){
        return new Application();
    }

    public long getId() {
        return id;
    }

    public Application setId(long id) {
        this.id = id;
        return this;
    }

    public String getAppname() {
        return appname;
    }

    public Application setAppname(String appname) {
        this.appname = appname;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Application setTitle(String title) {
        this.title = title;
        return this;
    }

    public Set<AspectType> getAspects() {
        return aspects;
    }

    public Application setAspects(Set<AspectType> aspects) {
        this.aspects = aspects;
        return this;
    }

}
