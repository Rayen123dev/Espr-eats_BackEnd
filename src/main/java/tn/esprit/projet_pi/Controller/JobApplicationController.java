package tn.esprit.projet_pi.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.projet_pi.Service.IJobApplicationService;
import tn.esprit.projet_pi.entity.JobApplication;
import tn.esprit.projet_pi.entity.User;
import tn.esprit.projet_pi.Repository.UserRepo;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/application")
public class JobApplicationController {

    private final IJobApplicationService jobApplicationService;
    private final UserRepo userRepo;

    @Autowired
    public JobApplicationController(IJobApplicationService jobApplicationService, UserRepo userRepo) {
        this.jobApplicationService = jobApplicationService;
        this.userRepo = userRepo;
    }

    @PostMapping("/add")
    public JobApplication createJobApplication(
            @RequestParam("motivationAttachment") MultipartFile motivationAttachment,
            @RequestParam("cvAttachment") MultipartFile cvAttachment,
            @RequestParam("jobOfferId") Long jobOfferId,
            Principal principal
    ) {
        if (principal == null) {
            throw new RuntimeException("User must be logged in to apply.");
        }

        User user = userRepo.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found."));

        return jobApplicationService.addNewApplication(motivationAttachment, cvAttachment, jobOfferId, user);
    }

    @PostMapping("/process-cv/{id}")
    public String processCV(@PathVariable Long id) {
        return jobApplicationService.processCV(id);
    }

    @GetMapping("/all")
    public List<JobApplication> getAllJobApplications() {
        return jobApplicationService.findAllJapplication();
    }

    @GetMapping("/{id}")
    public JobApplication getJobApplicationById(@PathVariable Long id) {
        return jobApplicationService.findByIdJapplication(id);
    }

    @PutMapping("/{id}")
    public JobApplication updateJobApplication(@PathVariable Long id, @RequestBody JobApplication jobApplication) {
        jobApplication.setJobAppID(id);
        return jobApplicationService.ModifyJapplication(jobApplication);
    }

    @DeleteMapping("/{id}")
    public void deleteJobApplication(@PathVariable Long id) {
        jobApplicationService.deleteById(id);
    }
}
