package tn.esprit.projet_pi.interfaces;

import tn.esprit.projet_pi.entity.Discount;

public interface IDiscount {
    public Discount createDiscount(Discount discount, Long userId);
    public void deleteDiscount(Long userId, Long idDiscount);
    public Discount updateDiscount(Long userId, Discount discount);
    public Discount getDiscountById(Long userId, Long idDiscount);
}
