package tn.esprit.projet_pi.Service;

import tn.esprit.projet_pi.entity.Produit;

import java.util.List;

public interface IAlertService {

    List<Produit> getLowStockAlerts();
    List<Produit> getExpiryAlerts();
    List<Produit> getAllAlerts();

}
