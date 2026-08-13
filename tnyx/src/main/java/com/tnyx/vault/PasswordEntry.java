package com.tnyx.vault;

public class PasswordEntry {
    private String name;
    private String username;
    private String password;
    private String url;


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

    public String getPasswordEntryData(){
        return (this.name + "||" + this.username + "||" + this.password + "||" + this.url);
    }
}
