package com.miaumigo.app.models;

public class Product {
    private String id;
    private String vendorId;
    private String name;
    private String description;
    private double price;
    private String imageUrl; // URL da imagem principal (para compatibilidade)
    private java.util.List<String> imageUrls; // Lista de URLs de imagens
    private double rating;
    private boolean inStock;
    private boolean visibleToCustomers; // Se o produto aparece na vitrine pública
    private String category;
    private int quantity;
    private String condition; // "new", "used", "refurbished"
    private long createdAt;
    private long updatedAt;
    private String brand;
    private String vendorName;
    private long salesCount;

    public Product() {
        // Construtor vazio necessário para Firebase
        this.visibleToCustomers = true;
        this.inStock = true;
        this.quantity = 0;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.brand = "";
        this.vendorName = "";
        this.salesCount = 0L;
    }

    public Product(String id, String name, String description, double price, String imageUrl, double rating, boolean inStock) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.rating = rating;
        this.inStock = inStock;
        this.visibleToCustomers = true;
        this.quantity = 0;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.brand = "";
        this.vendorName = "";
        this.salesCount = 0L;
    }

    // Getters e Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        // Se não houver lista de imagens, cria uma com a imagem principal
        if (imageUrls == null || imageUrls.isEmpty()) {
            imageUrls = new java.util.ArrayList<>();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                imageUrls.add(imageUrl);
            }
        }
    }
    
    public java.util.List<String> getImageUrls() {
        if (imageUrls == null) {
            imageUrls = new java.util.ArrayList<>();
            // Se houver imageUrl antiga, adiciona à lista
            if (imageUrl != null && !imageUrl.isEmpty()) {
                imageUrls.add(imageUrl);
            }
        }
        return imageUrls;
    }
    
    public void setImageUrls(java.util.List<String> imageUrls) {
        this.imageUrls = imageUrls;
        // Atualiza imageUrl principal se a lista não estiver vazia
        if (imageUrls != null && !imageUrls.isEmpty()) {
            this.imageUrl = imageUrls.get(0);
        }
    }
    
    public void addImageUrl(String url) {
        if (imageUrls == null) {
            imageUrls = new java.util.ArrayList<>();
        }
        if (!imageUrls.contains(url)) {
            imageUrls.add(url);
            // Se for a primeira imagem, define como principal
            if (imageUrl == null || imageUrl.isEmpty()) {
                imageUrl = url;
            }
        }
    }
    
    public void removeImageUrl(String url) {
        if (imageUrls != null) {
            imageUrls.remove(url);
            // Se a imagem removida for a principal, atualiza
            if (url.equals(imageUrl) && !imageUrls.isEmpty()) {
                imageUrl = imageUrls.get(0);
            }
        }
    }
    
    public void setMainImage(int index) {
        if (imageUrls != null && index >= 0 && index < imageUrls.size()) {
            String mainImage = imageUrls.get(index);
            // Move para o início da lista
            imageUrls.remove(index);
            imageUrls.add(0, mainImage);
            imageUrl = mainImage;
        }
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public boolean isInStock() {
        return inStock;
    }

    public void setInStock(boolean inStock) {
        this.inStock = inStock;
    }

    public String getVendorId() {
        return vendorId;
    }

    public void setVendorId(String vendorId) {
        this.vendorId = vendorId;
    }

    public boolean isVisibleToCustomers() {
        return visibleToCustomers;
    }

    public void setVisibleToCustomers(boolean visibleToCustomers) {
        this.visibleToCustomers = visibleToCustomers;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public long getSalesCount() {
        return salesCount;
    }

    public void setSalesCount(long salesCount) {
        this.salesCount = salesCount;
    }
}
