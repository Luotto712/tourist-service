package com.tourist.annotation;

public class CurrentUserInfo {

    private Long userId;
    private String username;
    private String role;
    private String college;

    public CurrentUserInfo() {
    }

    public CurrentUserInfo(Long userId, String username, String role, String college) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.college = college;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getCollege() {
        return college;
    }

    public void setCollege(String college) {
        this.college = college;
    }

}
