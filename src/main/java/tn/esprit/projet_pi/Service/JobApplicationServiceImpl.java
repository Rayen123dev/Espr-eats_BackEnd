package tn.esprit.projet_pi.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.projet_pi.Repository.JobApplicationRepository;
import tn.esprit.projet_pi.entity.JobApplication;

import java.util.List;

@Service
public class JobApplicationServiceImpl implements IJobApplicationService {

    private final JobApplicationRepository jobApplicationRepository;
    @Autowired
    public JobApplicationServiceImpl(JobApplicationRepository jobApplicationRepository) {
        this.jobApplicationRepository = jobApplicationRepository;
    }
    public JobApplication addNewJapplication(JobApplication application) {
        return jobApplicationRepository.save(application);
    }
    public List<JobApplication> findAllJapplication() {
        return jobApplicationRepository.findAll();
    }
    public JobApplication findByIdJapplication(Long idJobApplication) {
        return jobApplicationRepository.findById(idJobApplication).orElse(null);
    }
    public JobApplication ModifyJapplication(JobApplication application) {
        return jobApplicationRepository.save(application);
    }
    public void deleteById(Long idJobApplication) {
        jobApplicationRepository.deleteById(idJobApplication);
    }




}
