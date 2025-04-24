package tn.esprit.projet_pi.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.projet_pi.Repository.UserRepo;
import tn.esprit.projet_pi.Service.ImageDetectionService;
import tn.esprit.projet_pi.Service.ProfilNutritionnelServiceImpl;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/image-analysis")
public class ImageDetectionController {

    private final ImageDetectionService imageDetectionService;
    private final UserRepo userRepo;
    private final ProfilNutritionnelServiceImpl profilService;

    public ImageDetectionController(ImageDetectionService imageDetectionService,
                                    UserRepo userRepo,
                                    ProfilNutritionnelServiceImpl profilService) {
        this.imageDetectionService = imageDetectionService;
        this.userRepo = userRepo;
        this.profilService = profilService;
    }

    @PostMapping("/detect-flask")
    public ResponseEntity<?> detectViaFlask(@RequestParam("image") MultipartFile image) {
        try {
            Map<String, Object> result = imageDetectionService.detecterDepuisFlask(image);

            return ResponseEntity.ok(result);
        } catch (IOException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la détection IA", "details", e.getMessage()));
        }
    }

}
