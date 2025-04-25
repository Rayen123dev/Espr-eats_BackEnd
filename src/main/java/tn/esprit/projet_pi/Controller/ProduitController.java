package tn.esprit.projet_pi.Controller;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.projet_pi.Repository.ProduitHistoriqueRepository;
import tn.esprit.projet_pi.Service.IAlertService;
import tn.esprit.projet_pi.Service.IProduitService;
import tn.esprit.projet_pi.Service.ProduitHistoriqueService;
import tn.esprit.projet_pi.entity.Produit;
import tn.esprit.projet_pi.entity.TypeTransaction;
import java.util.List;
import java.util.Map;


@RestController
@CrossOrigin(origins = "http://localhost:4200")
@AllArgsConstructor
@RequestMapping("/produit")
public class ProduitController {
    @Autowired
    IProduitService produitService;
    @Autowired
    private ProduitHistoriqueRepository produitHistoriqueRepository;
    @Autowired
    private ProduitHistoriqueService produitHistoriqueService;

    @Autowired
    private IAlertService alertService;


    /// /////////CRUD///////////////
    // http://localhost:8081/retrieve-all-produits
    @GetMapping("/retrieve-all-produits")
    public ResponseEntity<List<Produit>> getAllProduits() {
        try {
            List<Produit> listproduits = produitService.getAllProduits();
            return ResponseEntity.ok(listproduits);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Transactional
    @PostMapping("/add-produit")
    public ResponseEntity<Produit> addProduit(@RequestBody Produit produit) {
        try {
            Produit savedProduit = produitService.addProduit(produit);
            produitHistoriqueService.createHistory(savedProduit, TypeTransaction.CREATED, produit.getQuantite());

            return ResponseEntity.ok(savedProduit);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    @Transactional
    @DeleteMapping("/remove-produit/{produit-id}")
    public ResponseEntity<String> removeProduit(@PathVariable("produit-id") Integer produitId) {
//        try {
            // Fetch the product
            Produit produit = produitService.getProduitById(produitId);

            if (produit == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Produit not found");
            }
//            try {
//                produitHistoriqueService.createHistory(produit, TypeTransaction.DELETED, produit.getQuantite());
//                System.out.println("History created successfully for produit: " + produit.getNomProduit());
//            } catch (Exception e) {
//                System.out.println("Error while creating history: " + e.getMessage());

            //}
           // produitHistoriqueRepository.flush();

            produitService.deleteProduit(produitId);

            return ResponseEntity.ok("Produit deleted successfully and history created");
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting produit");
//        }
    }

    @Transactional
    @PutMapping("/modify-produit/{produit-id}")
    public ResponseEntity<Produit> modifyProduit(@PathVariable("produit-id") Integer produitId, @RequestBody Produit produit) {
        try {
            // Fetch the existing product by ID
            Produit existingProduit = produitService.getProduitById(produitId);
            if (existingProduit == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // If product not found
            }
            produitHistoriqueService.createHistory(existingProduit, TypeTransaction.UPDATED, produit.getQuantite());
            // Set the ID of the updated product to the existing ID
            produit.setProduitID(produitId);

            // Update the produit
            Produit updatedProduit = produitService.modifyProduit(produit);
            return ResponseEntity.ok(updatedProduit);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

    }

    /// ////////////// alerts /////////////////
    @GetMapping("/alerts/low-stock")
    public ResponseEntity<List<Produit>> getLowStockAlerts() {
        List<Produit> alerts = alertService.getLowStockAlerts();
        return ResponseEntity.ok(alerts);
    }

    // Get expiry date alerts
    @GetMapping("/alerts/expiry")
    public ResponseEntity<List<Produit>> getExpiryAlerts() {
        List<Produit> alerts = alertService.getExpiryAlerts();
        return ResponseEntity.ok(alerts);
    }

    // Get all alerts (low stock + expiry)
    @GetMapping("/alerts/all")
    public ResponseEntity<List<Produit>> getAllAlerts() {
        List<Produit> alerts = alertService.getAllAlerts();
        return ResponseEntity.ok(alerts);
    }

    /// ///////////////// barcode ///////////////////////
    @GetMapping("/barcode/{barcode}")
    public ResponseEntity<Produit> getProductByBarcode(@PathVariable String barcode) {
        Produit product = produitService.getProductByBarcode(barcode);
        if (product != null) {
            return ResponseEntity.ok(product);
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    @PostMapping("/barcode/upload-barcode")
    public ResponseEntity<String> uploadBarcode(@RequestParam("barcode") MultipartFile file) {
        try {

            String barcode = produitService.decodeBarcode(file);
            Produit product = produitService.getProductByBarcode(barcode);

            if (product != null) {
                return ResponseEntity.ok("Product found: " + product.toString());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found for barcode");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing barcode");
        }
    }

    /// ///////////////////statt///////////////
    @GetMapping("/low-stock-count")
    public long getLowStockCount(@RequestParam(defaultValue = "5") int threshold) {
        return produitService.getLowStockCount(threshold);
    }

    // Endpoint to get count of expired products
    @GetMapping("expired-count")
    public long getExpiredProductCount() {
        return produitService.getExpiredProductCount();
    }

    // Endpoint to get average stock level
    @GetMapping("average-stock-level")
    public double getAverageStockLevel() {
        return produitService.getAverageStockLevel();
    }

    // Endpoint to get total product count
    @GetMapping("total-count")
    public long getTotalProductCount() {
        return produitService.getTotalProductCount();
    }

    @GetMapping("out-of-stock-count")
    public long getOutOfStockCount() {
        return produitService.getOutOfStockCount();
    }

    @GetMapping("/near-expiry/{daysBeforeExpiry}")
    public List<Produit> getProductsNearExpiry(@PathVariable int daysBeforeExpiry) {
        return produitService.getProductsNearExpiry(daysBeforeExpiry);
    }

    //////////ai
    @PostMapping("predict")
    public ResponseEntity<?> getForecast(@RequestBody Map<String, Object> payload) {
        RestTemplate restTemplate = new RestTemplate();
        String flaskUrl = "http://localhost:5000/forecast";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(flaskUrl, request, String.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error while communicating with Flask service: " + e.getMessage());
        }
    }


}
