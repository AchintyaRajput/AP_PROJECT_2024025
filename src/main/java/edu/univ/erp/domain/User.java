package edu.univ.erp.domain;

public class User {
    private int userId;
    private String username;
    private String role;
    private String status;

    public User(int userId, String username, String role, String status) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.status = status;
    }

    public int getUserId() { return userId; }

    public int getId() { return userId; }

    public String getUsername() { return username; }
    public String getRole() { return role; }
    public String getStatus() { return status; }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
