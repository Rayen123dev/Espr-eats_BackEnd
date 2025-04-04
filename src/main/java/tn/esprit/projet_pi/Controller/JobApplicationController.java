package tn.esprit.projet_pi.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projet_pi.Service.IJobApplicationService;
import tn.esprit.projet_pi.entity.JobApplication;

import java.util.List;

@RestController
@RequestMapping("/api/application")
public class JobApplicationController {
    private final IJobApplicationService jobApplicationService;
    @Autowired
    public JobApplicationController(IJobApplicationService jobApplicationService) {
        this.jobApplicationService = jobApplicationService;
    }
    @PostMapping("/add")
    public JobApplication createJobApplication(@RequestBody JobApplication jobApplication) {
        return jobApplicationService.addNewJapplication(jobApplication);
    }
    @GetMapping("/all")
    public List<JobApplication> getAllJobApplications() {
        return jobApplicationService.findAllJapplication();
    }
    @GetMapping("/{id}")
    public JobApplication getJobApplicationById(@PathVariable Long id) {
        return jobApplicationService.findByIdJapplication(id);
    }
    @PutMapping("{id}")
    public JobApplication updateJobApplication(@RequestBody JobApplication jobApplication) {
        return jobApplicationService.ModifyJapplication(jobApplication);
    }
    @DeleteMapping("/{id}")
    public void  deleteJobApplication(@PathVariable Long id) {
        jobApplicationService.deleteById(id);
    }

}
