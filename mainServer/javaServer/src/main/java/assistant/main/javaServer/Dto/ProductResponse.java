package assistant.main.javaServer.Dto;

import assistant.main.javaServer.Entities.Product;
import assistant.main.javaServer.Entities.Tag;

import java.util.List;

public class ProductResponse {
    private Long id;
    private String name;
    private String shortDesc;
    private String longDesc;
    private int price;
    private int discount;
    private String images;
    private List<String> tags;

    //Constructor
    public ProductResponse(String name, String shortDesc, String longDesc, int price, int discount, String images, List<String> tags) {
        this.name = name;
        this.shortDesc = shortDesc;
        this.longDesc = longDesc;
        this.price = price;
        this.discount = discount;
        this.images = images;
        this.tags = tags;
    }

    public ProductResponse(Product p) {
        this.id = p.getId();
        this.name = p.getName();
        this.shortDesc = p.getShortDesc();
        this.longDesc = p.getLongDesc();
        this.price = p.getPrice();
        this.discount = p.getDiscount();
        this.images = p.getImages();
        this.tags = p.getTags().stream().map(Tag::getName).toList();
    }

    // Getter / Setter

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortDesc() {
        return shortDesc;
    }

    public void setShortDesc(String shortDesc) {
        this.shortDesc = shortDesc;
    }

    public String getLongDesc() {
        return longDesc;
    }

    public void setLongDesc(String longDesc) {
        this.longDesc = longDesc;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getDiscount() {
        return discount;
    }

    public void setDiscount(int discount) {
        this.discount = discount;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
