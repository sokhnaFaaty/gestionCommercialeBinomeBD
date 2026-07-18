package repository;

import entities.Paiement;
import config.DatabaseConfig;
import enums.StatutPaiement;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaiementRepository {
    
    private DatabaseConfig dbConfig;

    public PaiementRepository() {
        this.dbConfig = new DatabaseConfig();
        initTable();
    }

    private void initTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS paiements (
                id INT PRIMARY KEY AUTO_INCREMENT,
                numero VARCHAR(50) UNIQUE NOT NULL,
                montant_verse DECIMAL(10,2) NOT NULL,
                date_paiement DATETIME DEFAULT CURRENT_TIMESTAMP,
                facture_id INT,
                statut VARCHAR(20) DEFAULT 'non_payee',
                FOREIGN KEY (facture_id) REFERENCES factures(id)
            )
        """;
        
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Table paiements prete");
        } catch (SQLException e) {
            System.err.println("Erreur creation table paiements: " + e.getMessage());
        }
    }

    public void save(Paiement paiement) {
        String sql = "INSERT INTO paiements (numero, montant_verse, date_paiement, facture_id, statut) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, paiement.getNumero());
            pstmt.setDouble(2, paiement.getMontantVerse());
            pstmt.setTimestamp(3, new Timestamp(paiement.getDate().getTime()));
            pstmt.setInt(4, paiement.getFacture().getId());
            pstmt.setString(5, paiement.getStatut().toString());
            pstmt.executeUpdate();
            
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                paiement.setId(rs.getInt(1));
            }
            
            System.out.println("Paiement enregistre (ID: " + paiement.getId() + ")");
            
        } catch (SQLException e) {
            System.err.println("Erreur sauvegarde paiement: " + e.getMessage());
        }
    }

    public List<Paiement> findAll() {
        List<Paiement> paiements = new ArrayList<>();
        String sql = "SELECT * FROM paiements ORDER BY id";
        
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Paiement p = new Paiement();
                p.setId(rs.getInt("id"));
                p.setNumero(rs.getString("numero"));
                p.setMontantVerse(rs.getDouble("montant_verse"));
                p.setDate(rs.getTimestamp("date_paiement"));
                p.setStatut(StatutPaiement.valueOf(rs.getString("statut")));
                paiements.add(p);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur liste paiements: " + e.getMessage());
        }
        
        return paiements;
    }
}