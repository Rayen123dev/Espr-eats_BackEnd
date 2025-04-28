package tn.esprit.projet_pi.Service;

import org.springframework.web.multipart.MultipartFile;
import tn.esprit.projet_pi.entity.JobApplication;
import tn.esprit.projet_pi.entity.User;

import java.util.List;

public interface IJobApplicationService {

    List<JobApplication> findAllJapplication();

    JobApplication findByIdJapplication(Long idJobApplication);

    JobApplication ModifyJapplication(JobApplication application);

    void deleteById(Long idJobApplication);

    String processCV(Long id);

    // UPDATED to include User
    JobApplication addNewApplication(MultipartFile motivationAttachment, MultipartFile cvAttachment, Long jobOfferId, User connectedUser);

}
