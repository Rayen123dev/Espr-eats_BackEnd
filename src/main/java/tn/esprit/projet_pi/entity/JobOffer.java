package tn.esprit.projet_pi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JobOffer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long jobOfferId;

    private String title;
    private String jobDescription;
    private Date date;
    private String skills;

    @OneToMany(mappedBy = "jobOffer", cascade = CascadeType.ALL)
    private List<JobApplication> applications;
}
