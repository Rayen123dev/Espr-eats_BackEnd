package tn.esprit.projet_pi.Service;

import org.springframework.stereotype.Service;
import tn.esprit.projet_pi.Repository.DiscountRepository;
import tn.esprit.projet_pi.entity.Discount;
import tn.esprit.projet_pi.interfaces.IDiscount;

import java.time.LocalDateTime;

@Service
public class DiscountService implements IDiscount {

    private final DiscountRepository discountRepository;

    public DiscountService(DiscountRepository discountRepository) {
        this.discountRepository = discountRepository;
    }
    @Override
    public Discount createDiscount(Discount discount, Long userId) {
        validateDates(discount.getStartDate(), discount.getEndDate());
        return discountRepository.save(discount);
    }

    @Override
    public void deleteDiscount(Long userId, Long idDiscount) {
        discountRepository.deleteById(idDiscount);
    }

    @Override
    public Discount updateDiscount(Long userId, Discount discount) {
        Discount existingDiscount = discountRepository.findById(discount.getIdDiscount()).orElse(null);
        if (existingDiscount != null) {
            return discountRepository.save(existingDiscount);
        }
        return null;
    }

    @Override
    public Discount getDiscountById(Long userId, Long idDiscount) {
        return discountRepository.findById(idDiscount).orElse(null);
    }

    private void validateDates(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date must not be null.");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date cannot be before start date.");
        }
    }
}
