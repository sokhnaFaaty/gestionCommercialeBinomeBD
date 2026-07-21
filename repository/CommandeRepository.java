package repository;

import entities.Commande;
import entities.Client;
import entities.ProduitCommande;
import config.DatabaseConfig;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommandeRepository {
    
    private DatabaseConfig dbConfig;

    public CommandeRepository() {
        this.dbConfig = new DatabaseConfig();
        initTable();
    }

    private void initTable() {
        String sql1 = """
            CREATE TABLE IF NOT EXISTS commandes (
                id INT PRIMARY KEY AUTO_INCREMENT,
                numero VARCHAR(50) UNIQUE NOT NULL,
                date_commande DATETIME DEFAULT CURRENT_TIMESTAMP,
                montant_total DECIMAL(10,2) DEFAULT 0,
                client_id INT,
                FOREIGN KEY (client_id) REFERENCES clients(id)
            )
        """;
        
        String sql2 = """
            CREATE TABLE IF NOT EXISTS produit_commandes (
                id INT PRIMARY KEY AUTO_INCREMENT,
                quantite INT NOT NULL,
                prix_saisie DECIMAL(10,2) NOT NULL,
                produit_id INT,
                commande_id INT,
                FOREIGN KEY (produit_id) REFERENCES produits(id),
                FOREIGN KEY (commande_id) REFERENCES commandes(id)
            )
        """;
        
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql1);
            stmt.execute(sql2);
            System.out.println("Tables commandes prete");
        } catch (SQLException e) {
            System.err.println("Erreur creation tables commandes: " + e.getMessage());
        }
    }

    public void save(Commande commande) {
        String sql = "INSERT INTO commandes (numero, date_commande, montant_total, client_id) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, commande.getNumero());
            pstmt.setTimestamp(2, new Timestamp(commande.getDate().getTime()));
            pstmt.setDouble(3, commande.getMontantTotal());
            pstmt.setInt(4, commande.getClient().getId());
            pstmt.executeUpdate();
            
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                commande.setId(rs.getInt(1));
            }
            
            for (ProduitCommande pc : commande.getProduitCommandes()) {
                saveProduitCommande(conn, pc, commande.getId());
            }
            
            System.out.println("Commande enregistree (ID: " + commande.getId() + ")");
            
        } catch (SQLException e) {
            System.err.println("Erreur sauvegarde commande: " + e.getMessage());
        }
    }

    private void saveProduitCommande(Connection conn, ProduitCommande pc, int commandeId) throws SQLException {
        String sql = "INSERT INTO produit_commandes (quantite, prix_saisie, produit_id, commande_id) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, pc.getQuantite());
            pstmt.setDouble(2, pc.getPrixS());
            pstmt.setInt(3, pc.getProduit().getId());
            pstmt.setInt(4, commandeId);
            pstmt.executeUpdate();
            
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                pc.setId(rs.getInt(1));
            }
        }
    }

    public List<Commande> findAll() {
        List<Commande> commandes = new ArrayList<>();
        String sql = "SELECT * FROM commandes ORDER BY id";
        
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Commande cmd = new Commande();
                cmd.setId(rs.getInt("id"));
                cmd.setNumero(rs.getString("numero"));
                cmd.setDate(rs.getTimestamp("date_commande"));
                cmd.setMontantTotal(rs.getDouble("montant_total"));
                cmd.setClient(loadClient(conn, rs.getInt("client_id")));
                commandes.add(cmd);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur liste commandes: " + e.getMessage());
        }
        
        return commandes;
    }

    private Client loadClient(Connection conn, int clientId) throws SQLException {
        String sql = "SELECT * FROM clients WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, clientId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Client(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getString("prenom"),
                    rs.getString("telephone")
                );
            }
        }
        return null;
    }
}