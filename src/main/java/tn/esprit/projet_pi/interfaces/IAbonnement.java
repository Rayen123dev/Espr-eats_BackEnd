package tn.esprit.projet_pi.interfaces;

import tn.esprit.projet_pi.entity.Abonnement;


public interface IAbonnement {
    public Abonnement createAbonnementByUser(Abonnement abonnement,Long userId, String checkoutUrl);
    public void deleteAbonnement(Long userId, Long idAbonnement);
    public Abonnement updateAbonnement(Long userId, Abonnement abonnement);
    public Abonnement getAbonnementById(Long userId, Long idAbonnement);



}
