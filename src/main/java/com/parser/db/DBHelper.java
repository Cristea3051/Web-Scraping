package com.parser.db;


import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import io.github.cdimascio.dotenv.Dotenv;

public class DBHelper {


    public static Connection getConnection() throws SQLException {
        Dotenv dotenv = Dotenv.load();
        String url = dotenv.get("DB_URL");

        return DriverManager.getConnection(url);
    }

    public static void insertArticle(Connection conn, String title, String url) {
        String insertSQL = "INSERT INTO articles (title, url, inserted_at) VALUES (?, ?, ?)";

        LocalDateTime now = LocalDateTime.now();

        try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setString(1, title);
            pstmt.setString(2, url);
            pstmt.setTimestamp(3, Timestamp.valueOf(now));

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Articolul a fost inserat cu succes!");
            } else {
                System.out.println("❌ Nu s-a inserat niciun articol.");
            }
        } catch (SQLException e) {
            System.out.println("❌ Eroare la inserarea articolului: " + e.getMessage());
        }
    }

    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                System.out.println("Conexiunea la baza de date a fost închisă.");
            } catch (SQLException e) {
                System.out.println("❌ Eroare la închiderea conexiunii: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            insertArticle(conn, "Articol de Test", "https://exemplu.com/articol");
        } catch (SQLException e) {
            System.out.println("❌ Eroare la obținerea conexiunii: " + e.getMessage());
        }
    }

    public static boolean articleExists(Connection conn, String title, String link) throws SQLException {
        String sql = "SELECT COUNT(*) FROM articles WHERE title = ? AND url = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, title);
            stmt.setString(2, link);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

}
