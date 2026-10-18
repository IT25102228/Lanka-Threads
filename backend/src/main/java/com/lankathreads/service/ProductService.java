package com.lankathreads.service;

import com.lankathreads.model.Product;
import com.lankathreads.pattern.ProductStockEventPublisher;
import com.lankathreads.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service managing Finished Goods (Garment Products) catalog and search filters.
 *
 * Design Pattern: Observer (GoF Behavioural) — integrated via
 * {@link ProductStockEventPublisher}. Whenever a product's stock quantity
 * changes during a save operation, all registered
 * {@link com.lankathreads.pattern.ProductStockObserver} implementations
 * are automatically notified. No frontend changes are required.
 *
 * Registered observers:
 *   - {@link com.lankathreads.pattern.LowStockAlertObserver}
 *   - {@link com.lankathreads.pattern.StockAuditLogObserver}
 */
@SuppressWarnings("null")
@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    // Observer Pattern — Subject injected by Spring.
    @Autowired
    private ProductStockEventPublisher stockEventPublisher;

    /**
     * Retrieves all products in the catalog.
     */
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * Overloaded search helper for keyword and category.
     */
    public List<Product> searchProducts(String search, String category) {
        return searchProducts(search, category, "", "");
    }

    /**
     * Filters products by keyword, category, colour, and size using straightforward matching.
     */
    public List<Product> searchProducts(String search, String category, String colour, String size) {
        String searchStr = search == null ? "" : search.trim().toLowerCase();
        String categoryStr = category == null ? "" : category.trim().toLowerCase();
        String colourStr = colour == null ? "" : colour.trim().toLowerCase();
        String sizeStr = size == null ? "" : size.trim().toLowerCase();

        List<Product> allProducts = productRepository.findAll();
        List<Product> matchedProducts = new ArrayList<>();

        for (Product product : allProducts) {
            // Check 1: Name match
            boolean matchesSearch = searchStr.isEmpty() ||
                    (product.getName() != null && product.getName().toLowerCase().contains(searchStr));

            // Check 2: Category match (Men, Women, Children)
            boolean matchesCategory = categoryStr.isEmpty() ||
                    (product.getCategory() != null && product.getCategory().toLowerCase().contains(categoryStr));

            // Check 3: Colour match
            boolean matchesColour = colourStr.isEmpty() ||
                    (product.getColour() != null && product.getColour().equalsIgnoreCase(colourStr));

            // Check 4: Size match (XS, S, M, L, XL, 2XL)
            boolean matchesSize = sizeStr.isEmpty() ||
                    (product.getSize() != null && product.getSize().equalsIgnoreCase(sizeStr));

            // If all active filters match, include the product
            if (matchesSearch && matchesCategory && matchesColour && matchesSize) {
                matchedProducts.add(product);
            }
        }

        return matchedProducts;
    }

    /**
     * Saves or updates a product.
     *
     * Observer Pattern integration: if the product already exists in the
     * database and its stock quantity has changed, all registered observers
     * are notified via {@link ProductStockEventPublisher#notifyObservers}.
     * New products also trigger an initial stock notification so observers
     * can react (e.g. alert if the product is created with zero stock).
     */
    public Product saveProduct(Product product) {
        // Capture the old stock quantity before saving (Observer Pattern hook).
        int oldQuantity = 0;
        boolean isUpdate = product.getId() != null;

        if (isUpdate) {
            Product existing = productRepository.findById(product.getId()).orElse(null);
            if (existing != null) {
                oldQuantity = existing.getStockQuantity() != null ? existing.getStockQuantity() : 0;
            }
        }

        // Persist the product to the database.
        Product saved = productRepository.save(product);

        // Observer Pattern — notify all observers if stock quantity changed.
        int newQuantity = saved.getStockQuantity() != null ? saved.getStockQuantity() : 0;
        boolean stockChanged = !isUpdate || (newQuantity != oldQuantity);

        if (stockChanged) {
            stockEventPublisher.notifyObservers(saved, oldQuantity, newQuantity);
        }

        return saved;
    }

    /**
     * Finds a product by ID.
     */
    @Nullable
    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    /**
     * Deletes a product by ID.
     */
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}
