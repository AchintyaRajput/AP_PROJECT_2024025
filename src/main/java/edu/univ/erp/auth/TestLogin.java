package edu.univ.erp.auth;

import edu.univ.erp.domain.User;

public class TestLogin {
    public static void main(String[] args) {
        LoginService loginService = new LoginService();

        // === STEP 1: Register a new test user (run ONCE) ===
        // ⚠️ Uncomment this line for one run only, then comment again
//         loginService.registerUser("testuser1", "testpass123", "Instructor");

        // === STEP 2: Try logging in ===
        User user = loginService.login("testuser1", "testpass123");

        if (user != null) {
            System.out.println("Logged in as " + user.getUsername() + " (" + user.getRole() + ")");
        } else {
            System.out.println("Login failed");
        }
    }
}
