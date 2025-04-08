package tn.esprit.projet_pi.Controller;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projet_pi.Service.IJobApplicationService;
import tn.esprit.projet_pi.Service.IJobOfferService;
import tn.esprit.projet_pi.entity.JobOffer;

import java.util.List;

@RestController
@RequestMapping("/api/offer")
public class JobOfferController {
    private final IJobOfferService jobOfferService;
@Autowired
    public JobOfferController(IJobOfferService jobOfferService) {
        this.jobOfferService = jobOfferService;
    }
@GetMapping("/all")
    public List<JobOffer> getAllJobOffers() {
        return jobOfferService.findAllJoffer();
    }
@GetMapping("/{id}")
    public JobOffer getJobOfferById(@PathVariable Long id) {
        return jobOfferService.findByIdJoffer(id);
    }
@PostMapping("/add")
    public JobOffer createJobOffer(@RequestBody JobOffer jobOffer) {
        return jobOfferService.addNewJoffer(jobOffer);
    }
@PutMapping("/{id}")
    public JobOffer updateJobOffer(@PathVariable Long id, @RequestBody JobOffer jobOffer) {
        return jobOfferService.ModifyJoffer(jobOffer);
    }
@DeleteMapping("/{id}")
    public void  deleteJobOffer(@PathVariable Long id) {
    jobOfferService.deleteById(id);
}



}
