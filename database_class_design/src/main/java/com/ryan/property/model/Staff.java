package com.ryan.property.model;

public class Staff {
    private long id;
    private String name;
    private String role;
    private String phone;

    public Staff() {}

    public Staff(long id, String name, String role, String phone) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.phone = phone;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
