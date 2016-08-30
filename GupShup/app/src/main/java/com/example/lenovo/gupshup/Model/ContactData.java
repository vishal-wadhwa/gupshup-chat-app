package com.example.lenovo.gupshup.Model;

/**
 * Created by Lenovo on 12-Aug-16.
 */
public class ContactData {
    private String name;
    private String phone;

    public ContactData(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }
}
