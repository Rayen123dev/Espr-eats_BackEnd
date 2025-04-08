package tn.esprit.projet_pi.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projet_pi.Repository.UserRepo;
import tn.esprit.projet_pi.Service.IConsultationService;
import tn.esprit.projet_pi.entity.Consultation;
import tn.esprit.projet_pi.entity.User;

import java.util.List;

@RestController
@RequestMapping("/api/consultations")
public class ConsultationController {

    private final IConsultationService consultationService;
    private final UserRepo userRepo;



    @Autowired
    public ConsultationController(IConsultationService consultationService, UserRepo userRepo) {
        this.consultationService = consultationService;
        this.userRepo = userRepo;
    }

    @PostMapping
    public ResponseEntity<Consultation> create(@RequestBody Consultation consultation) {
        Consultation created = consultationService.addConsultation(consultation);
        return ResponseEntity.ok(created);
    }


    @PutMapping("/{id}")
    public Consultation update(@PathVariable Long id, @RequestBody Consultation consultation) {
        return consultationService.updateConsultation(id, consultation);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        consultationService.deleteConsultation(id);
    }

    @GetMapping("/{id}")
    public Consultation get(@PathVariable Long id) {
        return consultationService.getConsultation(id);
    }

    @GetMapping("/etudiant/{userId}")
    public List<Consultation> getByEtudiant(@PathVariable Long userId) {
        return consultationService.getByEtudiant(userId);
    }

    @GetMapping("/medecin/{medecinId}")
    public List<Consultation> getByMedecin(@PathVariable Long medecinId) {
        return consultationService.getByMedecin(medecinId);
    }

    @GetMapping
    public List<Consultation> getAllConsultations() {
        return consultationService.getAllConsultations();
    }

    @GetMapping("/mes-consultations")
    public List<Consultation> getMesConsultations() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByEmail(email).orElseThrow();
        return consultationService.getByEtudiant(user.getIdUser());
    }






}
