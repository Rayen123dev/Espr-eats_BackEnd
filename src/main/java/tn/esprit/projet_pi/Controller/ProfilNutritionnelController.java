package tn.esprit.projet_pi.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projet_pi.Repository.UserRepo;
import tn.esprit.projet_pi.Service.IProfilNutritionnelService;
import tn.esprit.projet_pi.entity.ProfilNutritionnel;
import tn.esprit.projet_pi.entity.User;

import java.util.List;

@RestController
@RequestMapping("/api/profil")
public class ProfilNutritionnelController {

    private final IProfilNutritionnelService profilService;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    public ProfilNutritionnelController(IProfilNutritionnelService profilService) {
        this.profilService = profilService;
    }

    @PostMapping("/add")
    public ResponseEntity<ProfilNutritionnel> create(@RequestBody ProfilNutritionnel profil) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        String email = auth.getName(); // Récupère l'email de l'utilisateur
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        profil.setUser(user); // Associe le user connecté au profil

        ProfilNutritionnel created = profilService.addProfil(profil);
        return ResponseEntity.ok(created);
    }


    @PutMapping("/{id}")
    public ProfilNutritionnel update(@PathVariable Long id, @RequestBody ProfilNutritionnel profil) {
        return profilService.updateProfil(id, profil);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        profilService.deleteProfil(id);
    }

    @GetMapping("/{id}")
    public ProfilNutritionnel getById(@PathVariable Long id) {
        return profilService.getProfilById(id);
    }

    @GetMapping("/user/{userId}")
    public ProfilNutritionnel getByUserId(@PathVariable Long userId) {
        return profilService.getProfilByUserId(userId);
    }

    @GetMapping
    public List<ProfilNutritionnel> getAll() {
        return profilService.getAllProfils();
    }

    @GetMapping("/me")
    public ResponseEntity<ProfilNutritionnel> getMonProfil() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        String email = auth.getName();
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // 🔁 ICI : on utilise getId_user() au lieu de getIdUser()
        ProfilNutritionnel profil = profilService.getProfilByUserId(user.getId_user());

        if (profil == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(profil);
    }
}
