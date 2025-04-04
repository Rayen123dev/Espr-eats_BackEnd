package tn.esprit.projet_pi.Service;

import tn.esprit.projet_pi.entity.JobApplication;

import java.util.List;
import java.util.Optional;

public interface IJobApplicationService {
    JobApplication addNewJapplication(JobApplication reclamation);
    List<JobApplication> findAllJapplication();
    JobApplication findByIdJapplication(Long idJobApplication);
    void deleteById(Long idJobApplication);
    JobApplication ModifyJapplication(JobApplication reclamation);
}
