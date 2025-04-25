package tn.esprit.projet_pi.Service;

import tn.esprit.projet_pi.entity.Produit;
import tn.esprit.projet_pi.entity.ProduitHistorique;
import tn.esprit.projet_pi.entity.TypeTransaction;

import java.util.List;

public interface IProduitHistoriqueService {

    public void createHistory(Produit produit, TypeTransaction typeTransaction, int quantite);
    public List<ProduitHistorique> getAllHistory();

}
