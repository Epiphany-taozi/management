package com.ryan.property.model;

import java.sql.Timestamp;

public class Owner {
    private long id;
    private String name;
    private String phone;
    private String building;
    private String room;
    private String idCard;
    private Timestamp createdAt;

    public Owner() {}

    public Owner(long id, String name, String phone, String building, String room, String idCard, Timestamp createdAt) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.building = building;
        this.room = room;
        this.idCard = idCard;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getBuilding() { return building; }
    public void setBuilding(String building) { this.building = building; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    // Java 字段叫 idCard，对应数据库列 id_card
    public String getIdCard() { return idCard; }
    public void setIdCard(String idCard) { this.idCard = idCard; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
