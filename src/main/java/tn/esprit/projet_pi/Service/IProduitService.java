package tn.esprit.projet_pi.Service;

import org.springframework.web.multipart.MultipartFile;
import tn.esprit.projet_pi.entity.Produit;
import java.util.List;

public interface IProduitService {

    public Produit addProduit(Produit produit);
    public List<Produit> getAllProduits();
    public Produit modifyProduit(Produit Produit);
    public Produit deleteProduit(Integer id);
    public Produit getProduitById(Integer produitId);
    public Produit getProductByBarcode(String barcode);
    public String decodeBarcode(MultipartFile file);
    public long getLowStockCount(int threshold);
    public long getExpiredProductCount();
    public double getAverageStockLevel();
    public long getTotalProductCount();
    public long getOutOfStockCount();
    public List<Produit> getProductsNearExpiry(int daysBeforeExpiry);


}
