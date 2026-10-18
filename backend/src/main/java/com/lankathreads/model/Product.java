package com.lankathreads.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // e.g. "Men's Polo T-shirt"

    @Column(nullable = false)
    private String category; // Men, Women, Children

    @Column(nullable = false)
    private String size; // XS, S, M, L, XL, 2XL

    @Column(nullable = false)
    private String colour;
    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stockQuantity;

    @Column(nullable = false)
    private BigDecimal fabricCost = new BigDecimal("400.00"); // BOM fabric cost

    @Column(nullable = false)
    private BigDecimal laborCost = new BigDecimal("250.00");  // BOM labor cost

    @Column(nullable = false)
    private BigDecimal overheadCost = new BigDecimal("150.00"); // BOM overhead cost

    @Column(name = "main_image_url")
    private String mainImageUrl; // Primary product image path

    @Column(name = "sub_image_1_url")
    private String subImage1Url; // Supporting product image path

    @Column(name = "sub_image_2_url")
    private String subImage2Url; // Supporting product image path

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public String getColour() { return colour; }
    public void setColour(String colour) { this.colour = colour; }

    public BigDecimal getFabricCost() { 
        return fabricCost != null ? fabricCost : new BigDecimal("400.00"); 
    }
    public void setFabricCost(BigDecimal fabricCost) { this.fabricCost = fabricCost; }

    public BigDecimal getLaborCost() { 
        return laborCost != null ? laborCost : new BigDecimal("250.00"); 
    }
    public void setLaborCost(BigDecimal laborCost) { this.laborCost = laborCost; }

    public BigDecimal getOverheadCost() { 
        return overheadCost != null ? overheadCost : new BigDecimal("150.00"); 
    }
    public void setOverheadCost(BigDecimal overheadCost) { this.overheadCost = overheadCost; }

    public BigDecimal getPrice() {
        return price != null ? price : BigDecimal.ZERO;
    }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getStockQuantity() {
        return stockQuantity != null ? stockQuantity : 0;
    }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }

    @Transient
    public BigDecimal getTotalBOMCost() {
        return getFabricCost().add(getLaborCost()).add(getOverheadCost());
    }

    @Transient
    public BigDecimal getNetMargin() {
        return getPrice().subtract(getTotalBOMCost());
    }

    public String getMainImageUrl() { return mainImageUrl; }
    public void setMainImageUrl(String mainImageUrl) {
        this.mainImageUrl = (mainImageUrl != null && !mainImageUrl.trim().isEmpty()) ? mainImageUrl.trim() : null;
    }

    public String getSubImage1Url() { return subImage1Url; }
    public void setSubImage1Url(String subImage1Url) {
        this.subImage1Url = (subImage1Url != null && !subImage1Url.trim().isEmpty()) ? subImage1Url.trim() : null;
    }

    public String getSubImage2Url() { return subImage2Url; }
    public void setSubImage2Url(String subImage2Url) {
        this.subImage2Url = (subImage2Url != null && !subImage2Url.trim().isEmpty()) ? subImage2Url.trim() : null;
    }

    // Backward-compatible helpers for existing templates and code paths.
    public String getImageUrl() { return getDisplayImage(); }
    public void setImageUrl(String imageUrl) { setMainImageUrl(imageUrl); }

    @Transient
    public String getDisplayImage() {
        if (mainImageUrl != null && !mainImageUrl.trim().isEmpty()) {
            return mainImageUrl.trim();
        }
        if (subImage1Url != null && !subImage1Url.trim().isEmpty()) {
            return subImage1Url.trim();
        }
        if (subImage2Url != null && !subImage2Url.trim().isEmpty()) {
            return subImage2Url.trim();
        }
        // Return colour-specific polo image if available
        if (colour != null && !colour.trim().isEmpty()) {
            String colourKey = colour.trim().toLowerCase();
            switch (colourKey) {
                case "red": return "/images/products/polo_red.jpg";
                case "blue": return "/images/products/polo_blue.jpg";
                case "green": return "/images/products/polo_green.jpg";
                case "black": return "/images/products/polo_black.jpg";
                case "white": return "/images/products/polo_white.jpg";
                case "yellow": return "/images/products/polo_yellow.jpg";
                case "navy blue": return "/images/products/polo_men.png";
                case "pink": return "/images/products/polo_pink.jpg";
                case "heather grey": return "/images/products/polo_heather_grey.jpg";
                case "orange": return "/images/products/polo_orange.jpg";
                default: break;
            }
        }
        String cat = (category != null && !category.trim().isEmpty()) ? category.trim().toLowerCase() : "men";
        return "/images/products/polo_" + cat + ".png";
    }

    @Transient
    public String getMainImageLocalPath() {
        return toLocalPath(mainImageUrl);
    }

    @Transient
    public String getSubImage1LocalPath() {
        return toLocalPath(subImage1Url);
    }

    @Transient
    public String getSubImage2LocalPath() {
        return toLocalPath(subImage2Url);
    }

    private String toLocalPath(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return null;
        }
        if (imageUrl.startsWith("/images/products/")) {
            return "uploads/products/" + imageUrl.substring("/images/products/".length());
        }
        if (imageUrl.startsWith("/uploads/")) {
            return imageUrl.substring(1);
        }
        return imageUrl;
    }
}

