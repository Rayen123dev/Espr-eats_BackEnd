package tn.esprit.projet_pi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JobApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer jobAppID;
    private String motivation_letter;
    private String attachment;

    @ManyToOne
    @JoinColumn(name = "jobOfferId")
    private JobOffer jobOffer;


    public Integer getJobAppID() {
        return jobAppID;
    }

    public void setJobAppID(Integer jobAppID) {
        this.jobAppID = jobAppID;
    }

    public String getMotivation_letter() {
        return motivation_letter;
    }

    public void setMotivation_letter(String motivation_letter) {
        this.motivation_letter = motivation_letter;
    }

    public String getAttachment() {
        return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }

    public JobOffer getJobOffer() {
        return jobOffer;
    }

    public void setJobOffer(JobOffer jobOffer) {
        this.jobOffer = jobOffer;
    }
}
