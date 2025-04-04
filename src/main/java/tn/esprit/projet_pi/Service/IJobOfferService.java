package tn.esprit.projet_pi.Service;

import tn.esprit.projet_pi.entity.JobOffer;

import java.util.List;

public interface IJobOfferService {
    JobOffer addNewJoffer(JobOffer offer);
    List<JobOffer> findAllJoffer();
    JobOffer findByIdJoffer(Long idJobOffer);
    JobOffer ModifyJoffer(JobOffer reclamation);
    void deleteById(Long idJobOffer);



}
