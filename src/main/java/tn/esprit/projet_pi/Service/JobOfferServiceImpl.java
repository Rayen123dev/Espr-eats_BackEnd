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

    @Override
    public JobOffer addNewJoffer(JobOffer offer) {
        return jobOfferRepository.save(offer);
    }

    @Override
    public List<JobOffer> findAllJoffer() {
        return jobOfferRepository.findAll();
    }

    @Override
    public JobOffer findByIdJoffer(Long idJobOffer) {
        return jobOfferRepository.findById(idJobOffer).orElse(null);
    }

    @Override
    public JobOffer ModifyJoffer(JobOffer jobOffer) {
        JobOffer existingOffer = jobOfferRepository.findById(jobOffer.getJobOfferId())
                .orElseThrow(() -> new RuntimeException("Offer not found"));

        if (existingOffer.getApplications() != null && !existingOffer.getApplications().isEmpty()) {
            throw new RuntimeException("Cannot modify offer with existing applications");
        }

        return jobOfferRepository.save(jobOffer);
    }

    @Override
    public void deleteById(Long idJobOffer) {
        jobOfferRepository.deleteById(idJobOffer);
    }
}
