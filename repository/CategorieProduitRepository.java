package repository;

import entities.CategorieProduit;
import config.DatabaseConfig;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategorieProduitRepository {
    
    private DatabaseConfig dbConfig;

    public CategorieProduitRepository() {
        this.dbConfig = new DatabaseConfig();
        initTable();
    }

    private void initTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS categories (
                id INT PRIMARY KEY AUTO_INCREMENT,
                libelle VARCHAR(100) NOT NULL UNIQUE
            )
        """;
        
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Table categories prete");
        } catch (SQLException e) {
            System.err.println("Erreur creation table categories: " + e.getMessage());
        }
    }

    public void save(CategorieProduit categorie) {
        String sql = "INSERT INTO categories (libelle) VALUES (?)";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, categorie.getLibelle());
            pstmt.executeUpdate();
            
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                categorie.setId(rs.getInt(1));
            }
            
            System.out.println("Categorie ajoutee (ID: " + categorie.getId() + ")");
            
        } catch (SQLException e) {
            System.err.println("Erreur ajout categorie: " + e.getMessage());
        }
    }

    public List<CategorieProduit> findAll() {
        List<CategorieProduit> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories ORDER BY id";
        
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                CategorieProduit cat = new CategorieProduit(
                    rs.getInt("id"),
                    rs.getString("libelle")
                );
                categories.add(cat);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur liste categories: " + e.getMessage());
        }
        
        return categories;
    }

    public CategorieProduit findById(int id) {
        String sql = "SELECT * FROM categories WHERE id = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new CategorieProduit(
                    rs.getInt("id"),
                    rs.getString("libelle")
                );
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur recherche categorie: " + e.getMessage());
        }
        return null;
    }
}