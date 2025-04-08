package tn.esprit.projet_pi.Controller;

import jakarta.transaction.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projet_pi.Service.EmailService;
import tn.esprit.projet_pi.Service.ReclamationService;
import tn.esprit.projet_pi.entity.Reclamation;
import tn.esprit.projet_pi.entity.ReclamationStatus;
import tn.esprit.projet_pi.entity.TransactionStatus;
import tn.esprit.projet_pi.entity.User;

import java.util.List;

@RestController
@RequestMapping("/api/reclamations")
public class ReclamationController {

    @Autowired
    private ReclamationService reclamationService;

    @PostMapping
    public Reclamation createReclamation(@RequestBody Reclamation reclamation) {
        return reclamationService.createReclamation(reclamation);
    }

    @GetMapping
    public List<Reclamation> getAllReclamations() {
        return reclamationService.getAllReclamations();
    }

    @PutMapping("/updateStatusRes/{id}")
    public void updateStatusR(@PathVariable Long id) {
        Reclamation r=reclamationService.getReclamationById(id);
        r.setStatus(ReclamationStatus.RESOLVED);
        reclamationService.saveReclamation(r);
    }

    @PutMapping("/updateStatusClo/{id}")
    public void updateStatusC(@PathVariable Long id) {
        Reclamation r=reclamationService.getReclamationById(id);
        r.setStatus(ReclamationStatus.CLOSED);
        reclamationService.saveReclamation(r);
    }

    @GetMapping("/rec/{id}")
    public Reclamation getReclamation(@PathVariable Long id) {
        return reclamationService.getReclamationById(id);
    }
    /*
    @GetMapping("/{id}")
    public Reclamation getReclamationById(@PathVariable Long id) {
        return reclamationService.getReclamationById(id);
    }

    @PutMapping("/{id}")
    public Reclamation updateReclamation(@PathVariable Long id, @RequestBody Reclamation reclamation) {
        return reclamationService.updateReclamation(id, reclamation);
    }

     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReclamation(@PathVariable Long id) {
        reclamationService.deleteReclamation(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    public List<Reclamation> getReclamationsByUserId(@PathVariable Long userId) {
        return reclamationService.getReclamationsByUserId(userId);
    }

}

