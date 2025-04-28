package tn.esprit.projet_pi.Service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.projet_pi.Repository.JobApplicationRepository;
import tn.esprit.projet_pi.Repository.JobOfferRepository;
import tn.esprit.projet_pi.entity.JobApplication;
import tn.esprit.projet_pi.entity.JobOffer;
import tn.esprit.projet_pi.entity.User;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class JobApplicationServiceImpl implements IJobApplicationService {

    private final JobApplicationRepository jobApplicationRepository;
    private final JobOfferRepository jobOfferRepository;

    @Autowired
    public JobApplicationServiceImpl(JobApplicationRepository jobApplicationRepository, JobOfferRepository jobOfferRepository) {
        this.jobApplicationRepository = jobApplicationRepository;
        this.jobOfferRepository = jobOfferRepository;
    }

    @Override
    public List<JobApplication> findAllJapplication() {
        return jobApplicationRepository.findAll();
    }

    @Override
    public JobApplication findByIdJapplication(Long idJobApplication) {
        return jobApplicationRepository.findById(idJobApplication).orElse(null);
    }

    @Override
    public JobApplication ModifyJapplication(JobApplication application) {
        return jobApplicationRepository.save(application);
    }

    @Override
    public void deleteById(Long idJobApplication) {
        jobApplicationRepository.deleteById(idJobApplication);
    }

    @Override
    public String processCV(Long id) {
        JobApplication application = jobApplicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        try {
            String filePath = "uploads/cv/" + application.getCvAttachment();
            PDDocument document = PDDocument.load(new File(filePath));
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String text = pdfStripper.getText(document);
            document.close();

            String cvText = text.toLowerCase();
            JobOffer offer = application.getJobOffer();
            String[] requiredSkills = offer.getSkills().toLowerCase().split(",");

            int matchedSkills = 0;
            for (String skill : requiredSkills) {
                if (cvText.contains(skill.trim())) {
                    matchedSkills++;
                }
            }

            int totalSkills = requiredSkills.length;
            int score = (int) (((double) matchedSkills / totalSkills) * 100);

            return "CV Match Score: " + score + "% (" + matchedSkills + "/" + totalSkills + " skills matched)";

        } catch (Exception e) {
            throw new RuntimeException("Failed to process CV", e);
        }
    }

    @Override
    public JobApplication addNewApplication(MultipartFile motivationAttachment, MultipartFile cvAttachment, Long jobOfferId, User connectedUser) {
        try {
            JobOffer offer = jobOfferRepository.findById(jobOfferId)
                    .orElseThrow(() -> new RuntimeException("Offer not found."));

            if (jobApplicationRepository.existsByUserAndJobOffer(connectedUser, offer)) {
                throw new RuntimeException("You have already applied for this job offer.");
            }

            String motivationDir = "uploads/motivation_letters/";
            String cvDir = "uploads/cv/";
            Path motivationPath = Paths.get(motivationDir);
            Path cvPath = Paths.get(cvDir);

            if (!Files.exists(motivationPath)) Files.createDirectories(motivationPath);
            if (!Files.exists(cvPath)) Files.createDirectories(cvPath);

            String motivationFilename = UUID.randomUUID().toString() + "_" + motivationAttachment.getOriginalFilename();
            String cvFilename = UUID.randomUUID().toString() + "_" + cvAttachment.getOriginalFilename();

            Files.copy(motivationAttachment.getInputStream(), motivationPath.resolve(motivationFilename));
            Files.copy(cvAttachment.getInputStream(), cvPath.resolve(cvFilename));

            JobApplication application = new JobApplication();
            application.setMotivationAttachment(motivationFilename);
            application.setCvAttachment(cvFilename);
            application.setJobOffer(offer);
            application.setUser(connectedUser);

            return jobApplicationRepository.save(application);

        } catch (Exception e) {
            throw new RuntimeException("Failed to save application", e);
        }
    }
}
