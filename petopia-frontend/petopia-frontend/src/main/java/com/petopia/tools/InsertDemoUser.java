package com.petopia.tools;

import com.petopia.db.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Utility kecil: insert akun demo ke tabel users.
 * Jalankan sekali dari IDE atau Maven, lalu hapus/komentari jika tidak lagi diperlukan.
 */
public class InsertDemoUser {
    public static void main(String[] args) {
        // ubah sesuai keinginan
        String username = "demo";          // username yang akan dibuat
        String password = "demo123";       // password plaintext (akan di-hash)
        String displayName = "Demo User";

        try {
            // pastikan DB sudah diinisialisasi (initH2 membuat tabel)
            DatabaseUtil.initH2();

            try (Connection conn = DatabaseUtil.getConnection()) {
                String sql = "INSERT INTO users (username, display_name, password_hash) VALUES (?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, username);
                    ps.setString(2, displayName);
                    ps.setString(3, DatabaseUtil.hashPassword(password));
                    ps.executeUpdate();
                    System.out.println("Inserted demo user: " + username + " / " + password);
                }
            }
        } catch (SQLException sqe) {
            System.err.println("SQL error while inserting demo user: " + sqe.getMessage());
            sqe.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}