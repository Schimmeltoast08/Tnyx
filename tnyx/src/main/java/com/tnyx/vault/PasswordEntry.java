package com.tnyx.vault;

import java.util.UUID;

public class PasswordEntry {

    private String name = "";
    private String username = "";
    private String password = "";
    private String url = "";
    private final UUID id;

    public PasswordEntry() {
        this.id = UUID.randomUUID();
    }

    public PasswordEntry(UUID id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPasswordEntryData() {
        return (this.name + " || " + this.username + " || " + this.password + " || " + this.url + " || " + this.id);
    }

    public UUID getId() {
        return this.id;
    }

    public void editEntry(String name, String username, String url, String password) {

        if (!(name.isEmpty())) {
            this.name = name;
        }

        if (!(username.isEmpty())) {
            this.username = username;
        }

        if (!(url.isEmpty())) {
            this.url = url;
        }

        if (!(password.isEmpty())) {
            this.password = password;
        }

    }

    public void printEntry(){

        StringBuilder sb = new StringBuilder();
        sb.append("*".repeat(this.password.length()));

        System.out.printf("Name           : %-10s%n", this.name);
        System.out.printf("Username       : %-10s%n", this.username);
        System.out.printf("Password       : %-10s%n", sb.toString());
        System.out.printf("Url            : %-10s%n", this.url);

    }

}
