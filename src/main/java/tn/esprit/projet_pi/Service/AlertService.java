package tn.esprit.projet_pi.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.projet_pi.Repository.ProduitRepository;
import tn.esprit.projet_pi.entity.Produit;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AlertService implements IAlertService {

    @Autowired
    private ProduitRepository produitRepository;

    @Override
    public List<Produit> getLowStockAlerts() {
        return produitRepository.findAll().stream()
                .filter(produit -> produit.getQuantite() <= produit.getSeuil_alerte())
                .collect(Collectors.toList());
    }
    @Override
    public List<Produit> getExpiryAlerts() {
        Date today = new Date();
        return produitRepository.findAll().stream()
                .filter(produit -> produit.getDate_peremption() != null && produit.getDate_peremption().before(today))
                .collect(Collectors.toList());
    }

    @Override
    public List<Produit> getAllAlerts() {
        List<Produit> lowStock = getLowStockAlerts();
        List<Produit> expired = getExpiryAlerts();
        lowStock.addAll(expired);
        return lowStock;
    }
}