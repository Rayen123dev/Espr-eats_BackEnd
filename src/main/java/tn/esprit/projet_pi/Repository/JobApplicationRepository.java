package tn.esprit.projet_pi.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.projet_pi.entity.JobApplication;
import tn.esprit.projet_pi.entity.JobApplication;
import tn.esprit.projet_pi.entity.JobOffer;
import tn.esprit.projet_pi.entity.User;

import java.util.List;
import java.util.Optional;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {


    boolean existsByUserAndJobOffer(User user, JobOffer jobOffer);

}
