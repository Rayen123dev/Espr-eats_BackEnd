package tn.esprit.projet_pi.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.common.HybridBinarizer;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.projet_pi.Repository.ProduitRepository;
import tn.esprit.projet_pi.entity.Produit;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.Result;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class ProduitService implements IProduitService {
    @Autowired
    private  ProduitRepository produitRepository;

    @Override
    public Produit addProduit(Produit produit) {
        if (produit.getBarcode() == null || produit.getBarcode().isEmpty()) {
            produit.setBarcode(generateBarcode());
        }
        return produitRepository.save(produit);
    }
    @Override
    public List<Produit> getAllProduits() {
        return produitRepository.findAll();
    }
    @Override
    public  Produit modifyProduit(Produit Produit) {
        Produit existingProduit = produitRepository.findById(Produit.getProduitID())
                .orElseThrow(() -> new RuntimeException("Produit not found"));

        if (Produit.getBarcode() != null && !Produit.getBarcode().isEmpty()) {
            Produit.setBarcode(existingProduit.getBarcode()); // Ensure barcode remains unchanged
        }

        existingProduit.setNomProduit(Produit.getNomProduit());
        existingProduit.setDescription(Produit.getDescription());
        existingProduit.setQuantite(Produit.getQuantite());
        existingProduit.setSeuil_alerte(Produit.getSeuil_alerte());
        existingProduit.setDate_peremption(Produit.getDate_peremption());
        return produitRepository.save(existingProduit);
    }
    @Override
    public Produit deleteProduit(Integer id) {
        Produit produit = produitRepository.findById(id).orElseThrow(() -> new RuntimeException("Produit not found"));
        produitRepository.deleteById(id);
        return produit;
    }

    @Override
    public Produit getProduitById(Integer produitId) {
        return produitRepository.findById(produitId).orElse(null); // Returns the product if found, else null
    }

    private String generateBarcode() {
        Random random = new Random();
        StringBuilder barcode = new StringBuilder();

        // Generate a 12-digit barcode
        for (int i = 0; i < 12; i++) {
            barcode.append(random.nextInt(10));
        }

        return barcode.toString();
    }
    @Override
    public Produit getProductByBarcode(String barcode) {
        return produitRepository.findByBarcode(barcode);
    }

    @Override
    public String decodeBarcode(MultipartFile file) {
        try {
            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
            LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            Result result = new MultiFormatReader().decode(bitmap);
            return result.getText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to decode barcode: " + e.getMessage());
        }
    }
 /// stat
 public long getLowStockCount(int threshold) {
     List<Produit> produits = produitRepository.findAll();
     return produits.stream()
             .filter(produit -> produit.getQuantite() <= threshold)
             .count();
 }
    // Get count of expired products
    public long getExpiredProductCount() {
        List<Produit> produits = produitRepository.findAll();
        Date currentDate = new Date();
        return produits.stream()
                .filter(produit -> produit.getDate_peremption().before(currentDate))
                .count();
    }

    // Get average stock level (Average quantity across all products)
    public double getAverageStockLevel() {
        List<Produit> produits = produitRepository.findAll();
        if (produits.isEmpty()) {
            return 0;
        }
        double totalQuantity = produits.stream()
                .mapToInt(Produit::getQuantite)
                .sum();
        return totalQuantity / produits.size();
    }

    // Get total number of products
    public long getTotalProductCount() {
        return produitRepository.count();
    }

    public long getOutOfStockCount() {
        List<Produit> produits = produitRepository.findAll();
        return produits.stream()
                .filter(produit -> produit.getQuantite() == 0)
                .count();
    }

    public List<Produit> getProductsNearExpiry(int daysBeforeExpiry) {
        List<Produit> produits = produitRepository.findAll();
        Date currentDate = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, daysBeforeExpiry); // Get products expiring within the next `daysBeforeExpiry` days
        Date expiryThreshold = calendar.getTime();

        // Debugging: Print the current and threshold dates
        System.out.println("Current Date: " + currentDate);
        System.out.println("Expiry Threshold: " + expiryThreshold);

        return produits.stream()
                .filter(produit -> produit.getDate_peremption().before(expiryThreshold) && produit.getDate_peremption().after(currentDate))
                .collect(Collectors.toList());
    }

    /// /////////////  AI //////////////////


}
