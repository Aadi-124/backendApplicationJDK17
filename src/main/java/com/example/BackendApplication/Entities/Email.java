package com.example.BackendApplication.Entities;

public class Email {
    
    private String email;
    // private String password;
    

    public Email() {
    }
    public Email(String email) {
        this.email = email;
        // this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    
    // public String getPassword() {
    //     return password;
    // }

    // public void setPassword(String password) {
    //     this.password = password;
    // }

}
