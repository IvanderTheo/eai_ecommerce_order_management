package com.example.order_management.client.user;

public class UserResponse {
    private Long id;
    private String username;
    private String email;

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }

    public void setId(Long id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
}