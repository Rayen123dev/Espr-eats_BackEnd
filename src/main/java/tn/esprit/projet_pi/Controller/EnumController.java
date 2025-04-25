package tn.esprit.projet_pi.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.projet_pi.entity.AllergieType;
import tn.esprit.projet_pi.entity.ObjectifType;

@RestController
@RequestMapping("/api/enums")
public class EnumController {

    @GetMapping("/objectifs")
    public ObjectifType[] getObjectifs() {
        return ObjectifType.values();
    }

    @GetMapping("/allergies")
    public AllergieType[] getAllergies() {
        return AllergieType.values();
    }
}
