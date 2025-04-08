package tn.esprit.projet_pi.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
public class Plat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private CategoriePlat categorie;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "added_by")
    private User addedBy;

    @ManyToMany
    private String description;
    private String nom;

    // New calorie attribute
    private Integer calories;

    // Enlever la sérialisation de 'menus' et les relations inutiles ici
    @ManyToMany(mappedBy = "plats")
    @JsonIgnore
    @JsonBackReference
    private List<Menu> menus= new ArrayList<>();

    @ManyToMany(cascade = {CascadeType.MERGE})
    @JoinTable(
            name = "regime_alimentaire_plats_recommandes",
            joinColumns = @JoinColumn(name = "plat_id"),
            inverseJoinColumns = @JoinColumn(name = "regime_id")
    )
    private List<Produit> produits;
    @JsonIgnore
    private List<RegimeAlimentaire> regimes = new ArrayList<>();
    private String imagePath;

    public List<Produit> getProduits() {
        return produits;
    // Add getters and setters for the new field
    public String getImagePath() {
        return imagePath;
    }

    public void setProduits(List<Produit> produits) {
        this.produits = produits;
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    // Getters and setters
    public User getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(User addedBy) {
        this.addedBy = addedBy;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CategoriePlat getCategorie() {
        return categorie;
    }

    public void setCategorie(CategoriePlat categorie) {
        this.categorie = categorie;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public List<Menu> getMenus() {
        return menus;
    }

    public void setMenus(List<Menu> menus) {
        this.menus = menus;
    }

    public List<RegimeAlimentaire> getRegimes() {
        return regimes;
    }

    public void setRegimes(List<RegimeAlimentaire> regimes) {
        this.regimes = regimes;
    }

    private String description;
    private String nom;

    // Enlever la sérialisation de 'menus' et les relations inutiles ici
    @ManyToMany(mappedBy = "plats")
    @JsonIgnore
    private List<Menu> menus= new ArrayList<>();;

    @ManyToMany(cascade = {CascadeType.MERGE})
    @JoinTable(
            name = "regime_alimentaire_plats_recommandes",
            joinColumns = @JoinColumn(name = "plat_id"),
            inverseJoinColumns = @JoinColumn(name = "regime_id")
    )
    private List<RegimeAlimentaire> regimes = new ArrayList<>();

}
    public Integer getCalories() {
        return calories;
    }

    public void setCalories(Integer calories) {
        this.calories = calories;
    }
}