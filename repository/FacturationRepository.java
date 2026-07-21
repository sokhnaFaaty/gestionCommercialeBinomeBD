package repository;

import entities.Facturation;
import config.DatabaseConfig;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FacturationRepository {
    
    private DatabaseConfig dbConfig;

    public FacturationRepository() {
        this.dbConfig = new DatabaseConfig();
        initTable();
    }

    private void initTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS factures (
                id INT PRIMARY KEY AUTO_INCREMENT,
                numero VARCHAR(50) UNIQUE NOT NULL,
                date_facture DATETIME DEFAULT CURRENT_TIMESTAMP,
                montant DECIMAL(10,2) NOT NULL,
                commande_id INT UNIQUE,
                FOREIGN KEY (commande_id) REFERENCES commandes(id)
            )
        """;
        
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Table factures prete");
        } catch (SQLException e) {
            System.err.println("Erreur creation table factures: " + e.getMessage());
        }
    }

    public void save(Facturation facture) {
        String sql = "INSERT INTO factures (numero, date_facture, montant, commande_id) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, facture.getNumero());
            pstmt.setTimestamp(2, new Timestamp(facture.getDate().getTime()));
            pstmt.setDouble(3, facture.getMontant());
            pstmt.setInt(4, facture.getCommande().getId());
            pstmt.executeUpdate();
            
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                facture.setId(rs.getInt(1));
            }
            
            System.out.println("Facture ajoutee (ID: " + facture.getId() + ")");
            
        } catch (SQLException e) {
            System.err.println("Erreur sauvegarde facture: " + e.getMessage());
        }
    }

    public List<Facturation> findAll() {
        List<Facturation> factures = new ArrayList<>();
        String sql = "SELECT * FROM factures ORDER BY id";
        
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Facturation facture = new Facturation();
                facture.setId(rs.getInt("id"));
                facture.setNumero(rs.getString("numero"));
                facture.setDate(rs.getTimestamp("date_facture"));
                facture.setMontant(rs.getDouble("montant"));
                factures.add(facture);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur liste factures: " + e.getMessage());
        }
        
        return factures;
    }

    public Facturation findById(int id) {
        String sql = "SELECT * FROM factures WHERE id = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Facturation facture = new Facturation();
                facture.setId(rs.getInt("id"));
                facture.setNumero(rs.getString("numero"));
                facture.setDate(rs.getTimestamp("date_facture"));
                facture.setMontant(rs.getDouble("montant"));
                return facture;
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur recherche facture: " + e.getMessage());
        }
        return null;
    }
}