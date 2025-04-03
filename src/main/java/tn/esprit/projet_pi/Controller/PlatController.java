package tn.esprit.projet_pi.Controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.projet_pi.Repository.PlatRepository;
import tn.esprit.projet_pi.Repository.UserRepo;
import tn.esprit.projet_pi.Service.FileStorageService;
import tn.esprit.projet_pi.entity.CategoriePlat;
import tn.esprit.projet_pi.entity.Plat;
import tn.esprit.projet_pi.entity.Role;
import tn.esprit.projet_pi.entity.User;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/plats")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")

public class PlatController {
    private final Cloudinary cloudinary;
    private static final Logger logger = LoggerFactory.getLogger(PlatController.class);

    public PlatController() {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dibnjxoh3",
                "api_key", "548318794989785",
                "api_secret", "Rg3CbX8QvikSZscUq_zNqytFBbs"));
    }

    @Autowired
    private PlatRepository platRepository;

    @Autowired
    private UserRepo userRepository;
    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping
    public List<Plat> getAllPlats() {
        return platRepository.findAll();
    }

    @GetMapping("/categorie/{categorie}")
    public List<Plat> getPlatsByCategorie(@PathVariable CategoriePlat categorie) {
        return platRepository.findByCategorie(categorie);
    }

    @PostMapping("/addplat")
    public ResponseEntity<?> addPlat(@RequestBody Plat plat, @RequestParam(required = false) Long userId) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Le paramètre userId est requis");
        }

        User user = userRepository.findById(Math.toIntExact(userId))
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!user.getRole().equals(Role.Staff)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Accès refusé");
        }

        // Vérification des calories
        if (plat.getCalories() == null || plat.getCalories() < 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Les calories doivent être spécifiées et positives");
        }

        plat.setAddedBy(user);
        Plat savedPlat = platRepository.save(plat);
        return ResponseEntity.ok(savedPlat);
    }

    @GetMapping("/{id}")
    public Optional<Plat> getPlatsById(@PathVariable Long id) {
        return platRepository.findById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePlat(@PathVariable Long id, @RequestBody Plat platDetails,
                                        @RequestParam(required = false) Long userId) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Le paramètre userId est requis");
        }

        User user = userRepository.findById(Math.toIntExact(userId))
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!user.getRole().equals(Role.Staff)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Accès refusé");
        }

        // Vérification des calories
        if (platDetails.getCalories() != null && platDetails.getCalories() < 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Les calories doivent être positives");
        }

        Optional<Plat> optionalPlat = platRepository.findById(id);
        if (optionalPlat.isPresent()) {
            Plat plat = optionalPlat.get();
            plat.setNom(platDetails.getNom());
            plat.setDescription(platDetails.getDescription());
            plat.setCategorie(platDetails.getCategorie());

            // Mise à jour des calories
            if (platDetails.getCalories() != null) {
                plat.setCalories(platDetails.getCalories());
            }

            return ResponseEntity.ok(platRepository.save(plat));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePlat(@PathVariable Long id, @RequestParam(required = false) Long userId) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Le paramètre userId est requis");
        }

        User user = userRepository.findById(Math.toIntExact(userId))
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!user.getRole().equals(Role.Staff)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Accès refusé");
        }

        if (platRepository.existsById(id)) {
            platRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Modified uploadImage method to accept image URL instead of file upload
    @PostMapping("/{id}/uploadImage")
    public ResponseEntity<?> uploadImage(@PathVariable Long id,
                                         @RequestBody Map<String, String> payload,
                                         @RequestParam(required = false) Long userId) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Le paramètre userId est requis");
        }

        User user = userRepository.findById(Math.toIntExact(userId))
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!user.getRole().equals(Role.Staff)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Accès refusé");
        }

        Optional<Plat> optionalPlat = platRepository.findById(id);
        if (!optionalPlat.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        String imageUrl = payload.get("image");
        if (imageUrl == null || imageUrl.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("L'URL de l'image est requise");
        }

        // Update the plat with the image URL
        Plat plat = optionalPlat.get();
        plat.setImagePath(imageUrl);
        platRepository.save(plat);

        return ResponseEntity.ok().body("Image URL ajoutée avec succès");
    }

    // Modified addplatWithImage method to accept JSON instead of multipart form
    @PostMapping(value = "/addplatWithImage", consumes = {"multipart/form-data"})
    public ResponseEntity<?> addPlatWithImage(
            @RequestParam("nom") String nom,
            @RequestParam("description") String description,
            @RequestParam("categorie") String categorie,
            @RequestParam("calories") Integer calories,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(required = false) Long userId) {

        logger.info("Début de addPlatWithImage - nom: {}, userId: {}", nom, userId);

        if (userId == null) {
            logger.warn("userId est null");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Le paramètre userId est requis");
        }

        User user = userRepository.findById(Math.toIntExact(userId))
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        logger.info("Utilisateur trouvé: {}", user.getId_user());

        if (!user.getRole().equals(Role.Staff)) {
            logger.warn("Accès refusé pour userId: {}", userId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Accès refusé");
        }

        if (calories == null || calories < 0) {
            logger.warn("Calories invalides: {}", calories);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Les calories doivent être spécifiées et positives");
        }

        Plat plat = new Plat();
        plat.setNom(nom);
        plat.setDescription(description);
        try {
            plat.setCategorie(CategoriePlat.valueOf(categorie));
        } catch (IllegalArgumentException e) {
            logger.error("Catégorie invalide: {}", categorie);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Catégorie invalide: " + categorie);
        }
        plat.setCalories(calories);
        plat.setAddedBy(user);

        if (image != null && !image.isEmpty()) {
            try {
                logger.info("Upload de l'image vers Cloudinary, taille: {}", image.getSize());
                Map uploadResult = cloudinary.uploader().upload(image.getBytes(),
                        ObjectUtils.asMap("resource_type", "image"));
                String imageUrl = (String) uploadResult.get("secure_url");
                plat.setImagePath(imageUrl);
                logger.info("Image uploadée avec succès: {}", imageUrl);
            } catch (Exception e) {
                logger.error("Erreur lors de l'upload vers Cloudinary", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Erreur lors de l'upload vers Cloudinary : " + e.getMessage());
            }
        }

        Plat savedPlat = platRepository.save(plat);
        logger.info("Plat sauvegardé avec succès: {}", savedPlat.getId());
        return ResponseEntity.ok(savedPlat);
    }

    @PutMapping(value = "/{id}/updateWithImage", consumes = {"multipart/form-data"})
    public ResponseEntity<?> updatePlatWithImage(
            @PathVariable Long id,
            @RequestParam(value = "nom", required = false) String nom,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "categorie", required = false) String categorie,
            @RequestParam(value = "calories", required = false) Integer calories,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(required = false) Long userId) {

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Le paramètre userId est requis");
        }

        User user = userRepository.findById(Math.toIntExact(userId))
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!user.getRole().equals(Role.Staff)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Accès refusé");
        }

        Optional<Plat> optionalPlat = platRepository.findById(id);
        if (!optionalPlat.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Plat plat = optionalPlat.get();

        if (nom != null) plat.setNom(nom);
        if (description != null) plat.setDescription(description);
        if (categorie != null) plat.setCategorie(CategoriePlat.valueOf(categorie));
        if (calories != null) {
            if (calories < 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Les calories doivent être positives");
            }
            plat.setCalories(calories);
        }

        if (image != null && !image.isEmpty()) {
            try {
                Map uploadResult = cloudinary.uploader().upload(image.getBytes(),
                        ObjectUtils.asMap("resource_type", "image"));
                String imageUrl = (String) uploadResult.get("secure_url");
                plat.setImagePath(imageUrl);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Erreur lors de l'upload vers Cloudinary : " + e.getMessage());
            }
        }

        return ResponseEntity.ok(platRepository.save(plat));
    }
}
