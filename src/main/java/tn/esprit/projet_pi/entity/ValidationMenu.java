package tn.esprit.projet_pi.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ValidationMenu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "menu_id")
    private Menu menu;

    @ManyToOne
    @JoinColumn(name = "medecin_id") // ✅ le champ en base sera medecin_id
    private User medecin;

    private LocalDate dateValidation;

    @Enumerated(EnumType.STRING)
    private DecisionMenu decision;

    private String motifRejet;
    private String commentaire;
    private Integer scoreQualite;

    // Pas obligatoire avec Lombok, mais tu peux les garder si tu veux
    public Long getId() {
        return id;
    }

    public Menu getMenu() {
        return menu;
    }

    public User getMedecin() {
        return medecin;
    }

    public LocalDate getDateValidation() {
        return dateValidation;
    }

    public DecisionMenu getDecision() {
        return decision;
    }

    public String getMotifRejet() {
        return motifRejet;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public Integer getScoreQualite() {
        return scoreQualite;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
    }

    public void setMedecin(User medecin) {
        this.medecin = medecin;
    }

    public void setDateValidation(LocalDate dateValidation) {
        this.dateValidation = dateValidation;
    }

    public void setDecision(DecisionMenu decision) {
        this.decision = decision;
    }

    public void setMotifRejet(String motifRejet) {
        this.motifRejet = motifRejet;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public void setScoreQualite(Integer scoreQualite) {
        this.scoreQualite = scoreQualite;
    }
}
