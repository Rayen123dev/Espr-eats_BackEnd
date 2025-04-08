package tn.esprit.projet_pi.Controller;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projet_pi.Service.AbonnementService;
import tn.esprit.projet_pi.entity.Abonnement;
import tn.esprit.projet_pi.entity.AbonnementStatus;
import tn.esprit.projet_pi.entity.TypeAbonnement;

import java.util.Map;

@RestController
@RequestMapping("/api/abonnement")
public class AbonnementController {
    final AbonnementService abonnementService;


    @Autowired
    public AbonnementController(AbonnementService abonnementService) {
        this.abonnementService = abonnementService;
    }

    @PostMapping("/add/{userId}")
    public ResponseEntity<Abonnement> addAbonnement(@RequestBody Abonnement abonnement, @PathVariable("userId") Long userId) {
        try {
            Abonnement createdAbonnement = abonnementService.createAbonnementByUser(abonnement, userId);
            return new ResponseEntity<>(createdAbonnement, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/delete/{userId}/{idAbonnement}")
    public ResponseEntity<Void> deleteAbonnement(@PathVariable Long userId, @PathVariable Long idAbonnement) {
        try {
            abonnementService.deleteAbonnement(userId, idAbonnement);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/update/{userId}")
    public ResponseEntity<Abonnement> updateAbonnement(@PathVariable Long userId, @RequestBody Abonnement abonnement) {
        try {
            Abonnement updatedAbonnement = abonnementService.updateAbonnement(userId, abonnement);
            return new ResponseEntity<>(updatedAbonnement, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/get/{userId}/{idAbonnement}")
    public ResponseEntity<Abonnement> getAbonnementById(@PathVariable Long userId, @PathVariable Long idAbonnement) {
        Abonnement abonnement = abonnementService.getAbonnementById(userId, idAbonnement);
        if (abonnement != null) {
            return new ResponseEntity<>(abonnement, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    //confirm the abonnement
    @PutMapping("/confirm/{userId}/{confirmationCode}")
    public ResponseEntity<?> confirmAbonnement(
            @PathVariable Long userId,
            @PathVariable String confirmationCode,
            HttpServletRequest request) {
        try {
            String clientIp = getClientIp(request); // Extract IP from request
            Abonnement confirmedAbonnement = abonnementService.confirmAbonnement(confirmationCode, userId, clientIp);
            return new ResponseEntity<>(confirmedAbonnement, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    //reports
    @GetMapping("/report")
    public ResponseEntity<Map<String, Object>> getSubscriptionReport() {
        Map<String, Object> report = abonnementService.getSubscriptionReport();
        return ResponseEntity.ok(report);
    }

    @GetMapping("/types-and-costs")
    public ResponseEntity<Map<String, Double>> getSubscriptionTypesAndCosts() {
        Map<String, Double> typesAndCosts = abonnementService.getSubscriptionTypesAndCosts();
        return ResponseEntity.ok(typesAndCosts);
    }

    //get all abonnements by type
    @GetMapping("/getAll-ByType/{type}")
    public ResponseEntity<Iterable<Abonnement>> getAbonnementsByType(@PathVariable TypeAbonnement type) {
        Iterable<Abonnement> abonnements = abonnementService.getAllAbonnementsByType(type);
        return ResponseEntity.ok(abonnements);
    }

    //get all abonnements by status
    @GetMapping("/getAll-ByStatus/{status}")
    public ResponseEntity<Iterable<Abonnement>> getAbonnementsByStatus(@PathVariable AbonnementStatus status) {
        Iterable<Abonnement> abonnements = abonnementService.getAllAbonnementsByStatus(status);
        return ResponseEntity.ok(abonnements);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
