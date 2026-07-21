package repository;

import entities.Produit;
import entities.CategorieProduit;
import config.DatabaseConfig;
import enums.StatutProduit;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProduitRepository {
    
    private DatabaseConfig dbConfig;

    public ProduitRepository() {
        this.dbConfig = new DatabaseConfig();
        initTable();
    }

    private void initTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS produits (
                id INT PRIMARY KEY AUTO_INCREMENT,
                libelle VARCHAR(100) NOT NULL,
                qte_stock INT DEFAULT 0,
                prix_unitaire DECIMAL(10,2) NOT NULL,
                statut VARCHAR(20) DEFAULT 'disponible',
                categorie_id INT,
                FOREIGN KEY (categorie_id) REFERENCES categories(id)
            )
        """;
        
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Table produits prete");
        } catch (SQLException e) {
            System.err.println("Erreur creation table produits: " + e.getMessage());
        }
    }

    public void save(Produit produit) {
        String sql = "INSERT INTO produits (libelle, qte_stock, prix_unitaire, statut, categorie_id) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, produit.getLibelle());
            pstmt.setInt(2, produit.getQteStock());
            pstmt.setDouble(3, produit.getPrixUnitaire());
            pstmt.setString(4, produit.getStatut().toString());
            pstmt.setInt(5, produit.getCategorie().getId());
            pstmt.executeUpdate();
            
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                produit.setId(rs.getInt(1));
            }
            
            System.out.println("Produit ajoute (ID: " + produit.getId() + ")");
            
        } catch (SQLException e) {
            System.err.println("Erreur ajout produit: " + e.getMessage());
        }
    }

    public List<Produit> findAll() {
        List<Produit> produits = new ArrayList<>();
        String sql = "SELECT p.*, c.libelle as categorie_libelle FROM produits p JOIN categories c ON p.categorie_id = c.id ORDER BY p.id";
        
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                CategorieProduit cat = new CategorieProduit(
                    rs.getInt("categorie_id"),
                    rs.getString("categorie_libelle")
                );
                
                Produit produit = new Produit(
                    rs.getInt("id"),
                    rs.getString("libelle"),
                    rs.getInt("qte_stock"),
                    rs.getDouble("prix_unitaire"),
                    cat
                );
                produit.setStatut(StatutProduit.valueOf(rs.getString("statut")));
                produits.add(produit);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur liste produits: " + e.getMessage());
        }
        
        return produits;
    }

    public Produit findById(int id) {
        String sql = "SELECT p.*, c.libelle as categorie_libelle FROM produits p JOIN categories c ON p.categorie_id = c.id WHERE p.id = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                CategorieProduit cat = new CategorieProduit(
                    rs.getInt("categorie_id"),
                    rs.getString("categorie_libelle")
                );
                
                Produit produit = new Produit(
                    rs.getInt("id"),
                    rs.getString("libelle"),
                    rs.getInt("qte_stock"),
                    rs.getDouble("prix_unitaire"),
                    cat
                );
                produit.setStatut(StatutProduit.valueOf(rs.getString("statut")));
                return produit;
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur recherche produit: " + e.getMessage());
        }
        return null;
    }

    public Produit findByLibelle(String libelle) {
        String sql = "SELECT p.*, c.libelle as categorie_libelle FROM produits p JOIN categories c ON p.categorie_id = c.id WHERE p.libelle = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, libelle);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                CategorieProduit cat = new CategorieProduit(
                    rs.getInt("categorie_id"),
                    rs.getString("categorie_libelle")
                );
                
                Produit produit = new Produit(
                    rs.getInt("id"),
                    rs.getString("libelle"),
                    rs.getInt("qte_stock"),
                    rs.getDouble("prix_unitaire"),
                    cat
                );
                produit.setStatut(StatutProduit.valueOf(rs.getString("statut")));
                return produit;
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur recherche par libelle: " + e.getMessage());
        }
        return null;
    }

    public void update(Produit produit) {
        String sql = "UPDATE produits SET libelle = ?, qte_stock = ?, prix_unitaire = ?, statut = ?, categorie_id = ? WHERE id = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, produit.getLibelle());
            pstmt.setInt(2, produit.getQteStock());
            pstmt.setDouble(3, produit.getPrixUnitaire());
            pstmt.setString(4, produit.getStatut().toString());
            pstmt.setInt(5, produit.getCategorie().getId());
            pstmt.setInt(6, produit.getId());
            pstmt.executeUpdate();
            
            System.out.println("Produit mis a jour (ID: " + produit.getId() + ")");
            
        } catch (SQLException e) {
            System.err.println("Erreur mise a jour produit: " + e.getMessage());
        }
    }
}