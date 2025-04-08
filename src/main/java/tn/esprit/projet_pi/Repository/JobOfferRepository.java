package tn.esprit.projet_pi.Repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projet_pi.entity.JobApplication;
import tn.esprit.projet_pi.entity.JobOffer;
import tn.esprit.projet_pi.entity.Reclamation;
import tn.esprit.projet_pi.entity.User;

import java.util.List;
import java.util.Optional;

public interface JobOfferRepository extends JpaRepository<JobOffer, Long> {
}
