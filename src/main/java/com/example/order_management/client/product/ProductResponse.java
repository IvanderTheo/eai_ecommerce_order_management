package com.example.order_management.client.product;

public class ProductResponse {
    private Long id;
    private String description;
    private String name;
    private Double price;
    private String sku;
    private float unitSize;
    private float weight;   

    public Long getId() { return id; }
    public String getDescription() { return description; }
    public String getName() { return name; }
    public Double getPrice() { return price; }
    public String getSku() { return sku; }
    public float unitSize() { return unitSize; }
    public float getWeight() { return weight; }

    public void setId(Long id) { this.id = id; }
    public void setdescription(String description) { this.description = description; }
    public void setname(String name) { this.name = name; }
    public void setPrice(Double price) { this.price = price; }
    public void setSku(String sku) { this.sku=sku; }
    public void setUnitSize(float unitSize) { this.unitSize=unitSize; }
    public void setWeight(float weight) { this.weight=weight; }
}
