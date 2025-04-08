package tn.esprit.projet_pi.Service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.projet_pi.Repository.ProduitHistoriqueRepository;
import tn.esprit.projet_pi.Repository.ProduitRepository;
import tn.esprit.projet_pi.entity.Produit;
import tn.esprit.projet_pi.entity.ProduitHistorique;
import tn.esprit.projet_pi.entity.TypeTransaction;

import java.util.Date;
import java.util.List;

@Service
public class ProduitHistoriqueService  {

    @Autowired
    private ProduitHistoriqueRepository produitHistoriqueRepository;
    // Method to create history

    @Autowired
    private ProduitRepository produitRepository;


    @Transactional
    public void createHistory(Produit produit, TypeTransaction typeTransaction, int quantite) {
        // Explicitly load the Produit entity to ensure all fields are populated
        Produit loadedProduit = produitRepository.findById(produit.getProduitID()).orElse(null);

        if (loadedProduit == null) {
            // Handle case where the product does not exist
            throw new RuntimeException("Produit not found with ID: " + produit.getProduitID());
        }

        // Manually initialize the fields to ensure they are loaded
//        String nomProduit = loadedProduit.getNomProduit();
//        Date datePeremption = loadedProduit.getDate_peremption();

        // Ensure that produit fields are not null
        if (loadedProduit.getNomProduit() == null) {
            loadedProduit.setNomProduit("No Product");
        }
        if (loadedProduit.getDate_peremption() == null) {
            loadedProduit.setDate_peremption(new Date());  // Set a default value if expiration date is null
        }
        ProduitHistorique historique = new ProduitHistorique();
        historique.setProduit(loadedProduit);
        historique.setType(typeTransaction);
        historique.setQuantite(quantite);
        historique.setDate(new Date());  // Set the current date and time

        produitHistoriqueRepository.save(historique);

    }
    public List<ProduitHistorique> getAllHistory() {
        return produitHistoriqueRepository.findAll(); // Fetch all history from DB
    }
}
