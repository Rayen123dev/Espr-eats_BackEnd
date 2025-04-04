package tn.esprit.projet_pi.Service;

import tn.esprit.projet_pi.entity.Menu;
import tn.esprit.projet_pi.entity.RegimeAlimentaire;

import java.util.List;

public interface MenuService {
    void generateWeeklyMenus(Long userId);

    List<Menu> getAllMenus();

    // New methods
    List<Menu> getValidatedMenus();

    void validateMenus(Long doctorId, List<Long> menuIds);

    void regenerateWeeklyMenus(Long staffId);

    // Method to schedule automatic menu generation
    void scheduleMenuGeneration();
    public List<RegimeAlimentaire> returnregime ();
    public void rejectMenus(Long doctorId, List<Long> menuIds, String rejectionReason);
}