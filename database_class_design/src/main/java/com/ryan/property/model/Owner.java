package com.ryan.property.model;

public class Owner {
    private long id;
    private String name;
    private String phone;
    private String building;
    private String unit;

    public Owner() {}

    public Owner(long id, String name, String phone, String building, String unit) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.building = building;
        this.unit = unit;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
