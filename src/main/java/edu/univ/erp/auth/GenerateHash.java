package edu.univ.erp.auth;

import org.mindrot.jbcrypt.BCrypt;

public class GenerateHash {
    public static void main(String[] args) {
        String password = "teach123";
        // change if you want another password
        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        System.out.println("BCrypt hash for " + password + ":");
        System.out.println(hash);
    }
}
