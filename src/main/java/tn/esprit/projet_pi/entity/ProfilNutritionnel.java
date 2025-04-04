package tn.esprit.projet_pi.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfilNutritionnel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Le sexe est obligatoire")
    private Sexe sexe;

    @NotNull(message = "Le poids est requis")
    @DecimalMin(value = "1.0", message = "Le poids doit être supérieur à 0")
    private Double poidsActuel;

    @NotNull(message = "La taille est requise")
    @DecimalMin(value = "1.0", message = "La taille doit être supérieure à 0")
    private Double taille;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Le niveau d'activité est requis")
    private NiveauActivite niveauActivite;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "profil_objectifs", joinColumns = @JoinColumn(name = "profil_id"))
    @Column(name = "objectif")
    private Set<ObjectifType> objectif;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "profil_allergies", joinColumns = @JoinColumn(name = "profil_id"))
    @Column(name = "allergie")
    private Set<AllergieType> allergies;


    @Enumerated(EnumType.STRING)
    @NotNull(message = "Le régime alimentaire est requis")
    private RegimeAlimentaireType regimeAlimentaire;

    private Double imc;

    private Integer besoinCalorique;

    private String derniereEvolution;

    private LocalDateTime derniereMiseAJour;

    private Boolean fumeur;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Le groupe sanguin est requis")
    private GroupeSanguin groupeSanguin;




    // --- Getters et Setters explicites (optionnels avec Lombok) ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Sexe getSexe() {
        return sexe;
    }

    public void setSexe(Sexe sexe) {
        this.sexe = sexe;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Double getPoidsActuel() {
        return poidsActuel;
    }

    public void setPoidsActuel(Double poidsActuel) {
        this.poidsActuel = poidsActuel;
    }

    public Double getTaille() {
        return taille;
    }

    public void setTaille(Double taille) {
        this.taille = taille;
    }


    public NiveauActivite getNiveauActivite() {
        return niveauActivite;
    }

    public void setNiveauActivite(NiveauActivite niveauActivite) {
        this.niveauActivite = niveauActivite;
    }


    public RegimeAlimentaireType getRegimeAlimentaire() {
        return regimeAlimentaire;
    }

    public void setRegimeAlimentaire(RegimeAlimentaireType regimeAlimentaire) {
        this.regimeAlimentaire = regimeAlimentaire;
    }

    public Double getImc() {
        return imc;
    }

    public void setImc(Double imc) {
        this.imc = imc;
    }

    public Integer getBesoinCalorique() {
        return besoinCalorique;
    }

    public void setBesoinCalorique(Integer besoinCalorique) {
        this.besoinCalorique = besoinCalorique;
    }

    public String getDerniereEvolution() {
        return derniereEvolution;
    }

    public void setDerniereEvolution(String derniereEvolution) {
        this.derniereEvolution = derniereEvolution;
    }

    public LocalDateTime getDerniereMiseAJour() {
        return derniereMiseAJour;
    }

    public void setDerniereMiseAJour(LocalDateTime derniereMiseAJour) {
        this.derniereMiseAJour = derniereMiseAJour;
    }

    public Boolean getFumeur() {
        return fumeur;
    }

    public void setFumeur(Boolean fumeur) {
        this.fumeur = fumeur;
    }

    public GroupeSanguin getGroupeSanguin() {
        return groupeSanguin;
    }

    public void setGroupeSanguin(GroupeSanguin groupeSanguin) {
        this.groupeSanguin = groupeSanguin;
    }

    public Set<ObjectifType> getObjectif() {
        return objectif;
    }
    public void setObjectif(Set<ObjectifType> objectif) {
        this.objectif = objectif;
    }
    public Set<AllergieType> getAllergies() {
        return allergies;
    }
    public void setAllergies(Set<AllergieType> allergies) {
        this.allergies = allergies;
    }

}
