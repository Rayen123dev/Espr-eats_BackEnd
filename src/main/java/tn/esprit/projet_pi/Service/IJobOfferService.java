package tn.esprit.projet_pi.Service;

import tn.esprit.projet_pi.entity.JobOffer;

import java.util.List;

public interface IJobOfferService {
    JobOffer addNewJoffer(JobOffer reclamation);
    List<JobOffer> findAllJoffer();
    JobOffer findByIdJoffer(Long idJobOffer);
    JobOffer ModifyJoffer(Long idJobOffer);
    void deleteById(Long idJobOffer);


}
