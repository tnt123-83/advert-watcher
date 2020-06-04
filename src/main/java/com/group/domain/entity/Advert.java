package com.group.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ADVERT")
public class Advert {

    @Id
    @GeneratedValue
    @Column(name = "ID", unique = true, nullable = false, length = 20)
    private Integer id;

    //@Column(name = "SITE_ID", unique = true, length = 30)
    @Column(name = "SITE_ID", length = 30)
    private String siteId;

    @Column(name = "URL", unique = true)
    private String url;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "TEXT")
    private String text;

    @Column(name = "PRICE")
    private String price;

    @Column(name = "DATE")
    private Date date;

    @Column(name = "LOCATION")
    private String location;

    @Column(name = "FROM_AGENT")
    private String fromAgent;

    @Column(name = "VIEWED")
    private boolean viewed;

    @Column(name = "SAVE")
    private boolean save;

    @Column(name = "GROUP_NAME")
    private String groupName;

    public Advert(Integer id) {
        this.id = id;
    }
}
