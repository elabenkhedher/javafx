package test;
import java.sql.*;
import service.DatabaseService;

public class TestDB {
    public static void main(String[] args) {
        try {
            System.out.println("Connecting...");
            DatabaseService.initialize();
            try (Connection conn = DatabaseService.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("DESCRIBE candidate")) {
                
                System.out.println("--- Table candidate schema ---");
                boolean hasCvFilePath = false;
                while (rs.next()) {
                    String field = rs.getString("Field");
                    System.out.println(field + " - " + rs.getString("Type"));
                    if ("cv_file_path".equalsIgnoreCase(field)) {
                        hasCvFilePath = true;
                    }
                }
                
                if (!hasCvFilePath) {
                    System.out.println("\nAdding cv_file_path column...");
                    stmt.executeUpdate("ALTER TABLE candidate ADD COLUMN cv_file_path VARCHAR(500)");
                    System.out.println("Column added successfully.");
                }
                
                System.out.println("\nData in candidate:");
                try (ResultSet rs2 = stmt.executeQuery("SELECT id, nom, cv_file_path FROM candidate LIMIT 5")) {
                    while (rs2.next()) {
                        System.out.println("ID: " + rs2.getInt("id") + ", Nom: " + rs2.getString("nom") + ", CV: " + rs2.getString("cv_file_path"));
                    }
                }
                
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
