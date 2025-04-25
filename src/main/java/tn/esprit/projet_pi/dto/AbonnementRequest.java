package tn.esprit.projet_pi.dto;

import tn.esprit.projet_pi.entity.TypeAbonnement;

public class AbonnementRequest {
    private TypeAbonnement typeAbonnement;
    private Boolean renouvellementAutomatique;

    // Constructors, Getters, Setters
    public AbonnementRequest() {}

    public AbonnementRequest(TypeAbonnement typeAbonnement, Boolean renouvellementAutomatique) {
        this.typeAbonnement = typeAbonnement;
        this.renouvellementAutomatique = renouvellementAutomatique;
    }

    public TypeAbonnement getTypeAbonnement() { return typeAbonnement; }
    public void setTypeAbonnement(TypeAbonnement typeAbonnement) { this.typeAbonnement = typeAbonnement; }
    public Boolean getRenouvellementAutomatique() { return renouvellementAutomatique; }
    public void setRenouvellementAutomatique(Boolean renouvellementAutomatique) { this.renouvellementAutomatique = renouvellementAutomatique; }
}