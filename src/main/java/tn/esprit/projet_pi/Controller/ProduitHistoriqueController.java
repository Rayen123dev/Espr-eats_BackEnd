package tn.esprit.projet_pi.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.projet_pi.Service.ProduitHistoriqueService;
import tn.esprit.projet_pi.entity.ProduitHistorique;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
;
@RestController
@RequestMapping("/historique")
public class ProduitHistoriqueController {
    @Autowired
    private final ProduitHistoriqueService produitHistoriqueService;



    public ProduitHistoriqueController(ProduitHistoriqueService produitHistoriqueService) {
        this.produitHistoriqueService = produitHistoriqueService;

    }

    @GetMapping("/retrieve-all-historiques")
    public ResponseEntity<List<ProduitHistorique>> getAllHistory() {
        try {
            List<ProduitHistorique> historyList = produitHistoriqueService.getAllHistory();
            return ResponseEntity.ok(historyList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }



}
