package tn.esprit.projet_pi.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Discount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDiscount;
    private Double percentage; // e.g., 20 for 20%

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private Boolean isActive = true;
    @Enumerated(EnumType.STRING)
    private TypeAbonnement type;

    @ManyToOne
    private Abonnement abonnement;


    public Long getIdDiscount() {
        return idDiscount;
    }

    public void setIdDiscount(Long idDiscount) {
        this.idDiscount = idDiscount;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }
    public TypeAbonnement getType() {
        return type;
    }
    public void setType(TypeAbonnement type) {
        this.type = type;
    }
    public Abonnement getAbonnement() {
        return abonnement;
    }
    public void setAbonnement(Abonnement abonnement) {
        this.abonnement = abonnement;
    }

}
