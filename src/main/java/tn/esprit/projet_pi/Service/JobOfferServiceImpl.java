package tn.esprit.projet_pi.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.projet_pi.Repository.JobOfferRepository;
import tn.esprit.projet_pi.entity.JobOffer;

import java.util.List;

@Service
public class JobOfferServiceImpl implements IJobOfferService {
    private final JobOfferRepository jobOfferRepository;
@Autowired
    public JobOfferServiceImpl(JobOfferRepository jobOfferRepository) {
        this.jobOfferRepository = jobOfferRepository;
    }
    public JobOffer addNewJoffer(JobOffer offer) {
        return jobOfferRepository.save(offer);
    }
    public List<JobOffer> findAllJoffer() {
        return jobOfferRepository.findAll();
    }
    public JobOffer findByIdJoffer(Long idJobOffer) {
        return jobOfferRepository.findById(idJobOffer).orElse(null);
    }
    public JobOffer ModifyJoffer(JobOffer offer) {
        return jobOfferRepository.save(offer);
    }
    public void deleteById(Long idJobOffer) {
        jobOfferRepository.deleteById(idJobOffer);
    }






}
