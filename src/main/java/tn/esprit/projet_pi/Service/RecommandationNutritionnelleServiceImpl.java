package tn.esprit.projet_pi.Service;

import org.springframework.stereotype.Service;
import tn.esprit.projet_pi.Repository.ConsultationRepository;
import tn.esprit.projet_pi.Repository.RecommandationNutritionnelleRepository;
import tn.esprit.projet_pi.Repository.UserRepo;
import tn.esprit.projet_pi.entity.Consultation;
import tn.esprit.projet_pi.entity.RecommandationNutritionnelle;
import tn.esprit.projet_pi.entity.User;

import java.time.LocalDate;
import java.util.List;

@Service
public class RecommandationNutritionnelleServiceImpl implements IRecommandationNutritionnelleService {

    private final RecommandationNutritionnelleRepository recommandationRepo;
    private final UserRepo userRepo;
    private final ConsultationRepository consultationRepo;

    public RecommandationNutritionnelleServiceImpl(
            RecommandationNutritionnelleRepository recommandationRepo,
            UserRepo userRepo,
            ConsultationRepository consultationRepo
    ) {
        this.recommandationRepo = recommandationRepo;
        this.userRepo = userRepo;
        this.consultationRepo = consultationRepo;
    }

    @Override
    public RecommandationNutritionnelle add(RecommandationNutritionnelle r) {
        // Sécurité contre les NullPointer
        if (r.getMedecin() == null || r.getMedecin().getId_user() == null) {
            throw new RuntimeException("ID du médecin manquant.");
        }
        if (r.getConsultation() == null || r.getConsultation().getId() == null) {
            throw new RuntimeException("ID de la consultation manquant.");
        }

        // Charger les entités persistées
        User medecin = userRepo.findByidUser(r.getMedecin().getId_user())
                .orElseThrow(() -> new RuntimeException("Médecin non trouvé"));

        Consultation consultation = consultationRepo.findById(r.getConsultation().getId())
                .orElseThrow(() -> new RuntimeException("Consultation non trouvée"));

        // Remplacer les objets détachés
        r.setMedecin(medecin);
        r.setConsultation(consultation);

        // Ajouter la date du jour
        r.setDateRecommandation(LocalDate.now());

        return recommandationRepo.save(r);
    }

    @Override
    public void delete(Long id) {
        recommandationRepo.deleteById(id);
    }

    @Override
    public List<RecommandationNutritionnelle> getByConsultation(Long consultationId) {
        return recommandationRepo.findByConsultationId(consultationId);
    }

    @Override
    public List<RecommandationNutritionnelle> getByMedecin(Long medecinId) {
        return recommandationRepo.findByMedecin_IdUser(medecinId);
    }
}
