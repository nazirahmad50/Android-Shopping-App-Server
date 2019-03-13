package com.nazir.shoppingserver.Model;

public class Hookah {

    private String name;
    private String description;
    private String image;
    private String menuid;
    private String price;
    private String discount;

    public Hookah() {
    }

    public Hookah(String name, String description, String image, String menuid, String price, String discount) {
        this.name = name;
        this.description = description;
        this.image = image;
        this.menuid = menuid;
        this.price = price;
        this.discount = discount;
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getMenuid() {
        return menuid;
    }

    public void setMenuid(String menuid) {
        this.menuid = menuid;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }
}
