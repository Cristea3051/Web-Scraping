package com.parser.db;


import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import io.github.cdimascio.dotenv.Dotenv;

public class DBHelper {

    // Metodă pentru obținerea unei conexiuni la baza de date
    public static Connection getConnection() throws SQLException {
        Dotenv dotenv = Dotenv.load();
        String url = dotenv.get("DB_URL");
        String user = dotenv.get("DB_USER");
        String password = dotenv.get("DB_PASSWORD");

        return DriverManager.getConnection(url, user, password);
    }

    // Metodă pentru inserarea unui articol în baza de date
    public static void insertArticle(Connection conn, String title, String url) {
        String insertSQL = "INSERT INTO articles (title, url, inserted_at) VALUES (?, ?, ?)";

        // Setăm parametrul 'inserted_at' la data și ora curentă
        LocalDateTime now = LocalDateTime.now();
        String formattedDate = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setString(1, title);  // Setează titlul
            pstmt.setString(2, url);    // Setează URL-ul
            pstmt.setString(3, formattedDate);  // Setează data curentă

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

    // Metodă pentru a închide conexiunea la baza de date (dacă este necesar)
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

    // Exemplu de metodă pentru a insera un articol
    public static void main(String[] args) {
        // Exemplu de folosire a metodei insertArticle
        try (Connection conn = getConnection()) {
            insertArticle(conn, "Articol de Test", "https://exemplu.com/articol");
        } catch (SQLException e) {
            System.out.println("❌ Eroare la obținerea conexiunii: " + e.getMessage());
        }
    }

    public static boolean articleExists(Connection conn, String title, String link) throws SQLException {
        String sql = "SELECT COUNT(*) FROM articles WHERE title = ? AND link = ?";
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
