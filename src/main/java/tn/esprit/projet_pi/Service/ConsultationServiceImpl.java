package tn.esprit.projet_pi.Service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import tn.esprit.projet_pi.Repository.ConsultationRepository;
import tn.esprit.projet_pi.Repository.UserRepo;
import tn.esprit.projet_pi.entity.*;

import java.util.List;

@Service
public class ConsultationServiceImpl implements IConsultationService {

    private final ConsultationRepository consultationRepository;
    private final UserRepo userRepo;
    private final UserService userService;

    public ConsultationServiceImpl(
            ConsultationRepository consultationRepository,
            UserRepo userRepo,
            UserService userService) {
        this.consultationRepository = consultationRepository;
        this.userRepo = userRepo;
        this.userService = userService;
    }

    @Override
    public Consultation addConsultation(Consultation consultation) {
        // 🔐 Récupère l'utilisateur connecté
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User etudiant = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Étudiant non trouvé"));

        User medecin = userRepo.findByRole(Role.Medcin)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Médecin non trouvé"));

        consultation.setEtudiant(etudiant);
        consultation.setMedecin(medecin);

        // ✅ Valeurs par défaut
        if (consultation.getStatut() == null) {
            consultation.setStatut(StatutConsultation.EN_ATTENTE);
        }

        if (consultation.getTypeConsultation() == null) {
            consultation.setTypeConsultation(TypeConsultation.GENERALE); // 💡 Enum par défaut
        }

        return consultationRepository.save(consultation);
    }

    @Override
    public Consultation updateConsultation(Long id, Consultation c) {
        c.setId(id);
        return consultationRepository.save(c);
    }

    @Override
    public void deleteConsultation(Long id) {
        consultationRepository.deleteById(id);
    }

    @Override
    public Consultation getConsultation(Long id) {
        return consultationRepository.findById(id).orElse(null);
    }

    @Override
    public List<Consultation> getByEtudiant(Long userId) {
        return consultationRepository.findByEtudiant_IdUser(userId);
    }

    @Override
    public List<Consultation> getByMedecin(Long medecinId) {
        return consultationRepository.findByMedecin_IdUser(medecinId);
    }

    @Override
    public List<Consultation> getAllConsultations() {
        return consultationRepository.findAll();
    }
}
