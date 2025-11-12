package com.parser.db;


import java.sql.*;
import java.time.LocalDateTime;
import io.github.cdimascio.dotenv.Dotenv;

public class DBHelper {


    public static Connection getConnection() throws SQLException {
        // Prima dată verifică variabilele de mediu ale sistemului (folosit în GitHub Actions)
        String url = System.getenv("DB_URL");

        // Dacă nu există în environment variables, încearcă să încarci din .env (pentru dezvoltare locală)
        if (url == null || url.isEmpty()) {
            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing()  // Nu aruncă excepție dacă .env lipsește
                    .load();
            url = dotenv.get("DB_URL");
        }

        // Verifică dacă DB_URL a fost găsit
        if (url == null || url.isEmpty()) {
            throw new SQLException("DB_URL nu este configurată! Adaugă-o în variabilele de mediu sau în fișierul .env");
        }

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

    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            insertArticle(conn, "Articol de Test", "https://exemplu.com/articol");
            deleteOldArticles(conn);
        } catch (SQLException e) {
            System.out.println("❌ Eroare la obținerea conexiunii: " + e.getMessage());
        }
    }

    public static void deleteOldArticles(Connection conn) {
        String deleteSQL = "DELETE FROM articles WHERE inserted_at <= ?";

        LocalDateTime thresholdDate = LocalDateTime.now().minusDays(30);

        try (PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(thresholdDate));

            int rowsDeleted = pstmt.executeUpdate();
            System.out.println("🗑️ Au fost șterse " + rowsDeleted + " articole mai vechi de 30 de zile.");
        } catch (SQLException e) {
            System.out.println("❌ Eroare la ștergerea articolelor vechi: " + e.getMessage());
        }
    }


    public static boolean articleExists(Connection conn, String title, String link) throws SQLException {
        String sql = "SELECT COUNT(*) FROM articles WHERE title = ? AND url = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, title);
            stmt.setString(2, link);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1) <= 0;
            }
        }
    }

}
