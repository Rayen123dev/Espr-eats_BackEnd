package tn.esprit.projet_pi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JobApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long jobAppID;

    private String motivationAttachment; // Motivation Letter PDF filename
    private String cvAttachment;          // CV PDF filename

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "jobOfferId")

    private JobOffer jobOffer;

    public Long getJobAppID() {
        return jobAppID;
    }

    public void setJobAppID(Long jobAppID) {
        this.jobAppID = jobAppID;
    }

    public String getMotivationAttachment() {
        return motivationAttachment;
    }

    public void setMotivationAttachment(String motivationAttachment) {
        this.motivationAttachment = motivationAttachment;
    }

    public String getCvAttachment() {
        return cvAttachment;
    }

    public void setCvAttachment(String cvAttachment) {
        this.cvAttachment = cvAttachment;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public JobOffer getJobOffer() {
        return jobOffer;
    }

    public void setJobOffer(JobOffer jobOffer) {
        this.jobOffer = jobOffer;
    }
}
