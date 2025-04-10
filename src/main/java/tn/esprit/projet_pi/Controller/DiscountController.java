package tn.esprit.projet_pi.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projet_pi.Service.DiscountService;
import tn.esprit.projet_pi.entity.Discount;

@RestController
@RequestMapping("/api/discount")
public class DiscountController {

    private final DiscountService discountService;

    @Autowired
    public DiscountController(DiscountService discountService) {
        this.discountService = discountService;
    }

    @PostMapping("/create/{userId}")
    public Discount createDiscount(@PathVariable Long userId,@RequestBody Discount discount) {
        return discountService.createDiscount(discount, userId);
    }

}
