package tn.esprit.projet_pi.Controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.projet_pi.Repository.UserRepo;
import tn.esprit.projet_pi.Service.IJobOfferService;
import tn.esprit.projet_pi.Service.JobApplicationServiceImpl;
import tn.esprit.projet_pi.entity.JobOffer;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/offer")
public class JobOfferController {

    private final IJobOfferService jobOfferService;
    private final UserRepo userRepo;
    private final JobApplicationServiceImpl jobApplicationServiceImpl;
    private final Cloudinary cloudinary;

    @Autowired
    public JobOfferController(IJobOfferService jobOfferService, UserRepo userRepo,
                              JobApplicationServiceImpl jobApplicationServiceImpl, Cloudinary cloudinary) {
        this.jobOfferService = jobOfferService;
        this.userRepo = userRepo;
        this.jobApplicationServiceImpl = jobApplicationServiceImpl;
        this.cloudinary = cloudinary;
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
    public JobOffer createJobOffer(
            @RequestPart("jobOffer") JobOffer jobOffer,
            @RequestPart(value = "image", required = false) MultipartFile imageFile
    ) {
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                Map uploadResult = cloudinary.uploader().upload(imageFile.getBytes(), ObjectUtils.emptyMap());
                String imageUrl = (String) uploadResult.get("secure_url");
                jobOffer.setImage(imageUrl); // ➡️ Save the URL returned by Cloudinary
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to upload image to Cloudinary.");
            }
        }
        return jobOfferService.addNewJoffer(jobOffer);
    }

    @PutMapping("/{id}")
    public JobOffer updateJobOffer(@PathVariable Long id, @RequestPart("jobOffer") JobOffer jobOffer,
                                   @RequestPart(value = "image", required = false) MultipartFile imageFile) {
        jobOffer.setJobOfferId(id);
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                Map uploadResult = cloudinary.uploader().upload(imageFile.getBytes(), ObjectUtils.emptyMap());
                String imageUrl = (String) uploadResult.get("secure_url");
                jobOffer.setImage(imageUrl);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to upload image to Cloudinary.");
            }
        }
        return jobOfferService.ModifyJoffer(jobOffer);
    }

    @DeleteMapping("/{id}")
    public void deleteJobOffer(@PathVariable Long id) {
        jobOfferService.deleteById(id);
    }
}
