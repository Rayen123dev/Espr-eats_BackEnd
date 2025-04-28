package tn.esprit.projet_pi.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.projet_pi.entity.AllergieType;
import tn.esprit.projet_pi.entity.ProfilNutritionnel;
import tn.esprit.projet_pi.utils.MultipartInputStreamFileResource;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ImageDetectionService {

    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> detecterDepuisFlask(MultipartFile imageFile) throws IOException {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image", new MultipartInputStreamFileResource(imageFile.getInputStream(), imageFile.getOriginalFilename()));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        String flaskUrl = "http://172.20.10.12:5002/detect"; // <--- Vérifie bien que Flask tourne à cette URL

        ResponseEntity<Map> response = restTemplate.postForEntity(flaskUrl, requestEntity, Map.class);
        return response.getBody();
    }


    public boolean estAdapteAuProfil(List<String> aliments, ProfilNutritionnel profil) {
        if (profil.getAllergies() != null) {
            for (AllergieType allergie : profil.getAllergies()) {
                if (aliments.stream().anyMatch(a -> a.equalsIgnoreCase(allergie.name()))) {
                    return false;
                }
            }
        }

        if (profil.getRegimeAlimentaire() != null &&
                profil.getRegimeAlimentaire().name().equalsIgnoreCase("VEGETARIEN")) {
            if (aliments.stream().anyMatch(a ->
                    a.toLowerCase().contains("poulet") ||
                            a.toLowerCase().contains("viande") ||
                            a.toLowerCase().contains("thon") ||
                            a.toLowerCase().contains("poisson"))) {
                return false;
            }
        }

        return true;
    }
}
