package assistant.main.javaServer.Dto;

import assistant.main.javaServer.Entities.Category;

import java.util.List;

public class ProductRequest {
    private String name;
    private String shortDesc;
    private String longDesc;
    private int price;
    private int discount;
    private String images;
    private Category category;
    private List<String> tags;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getShortDesc() { return shortDesc; }
    public void setShortDesc(String shortDesc) { this.shortDesc = shortDesc; }

    public String getLongDesc() { return longDesc; }
    public void setLongDesc(String longDesc) { this.longDesc = longDesc; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }

    public int getDiscount() { return discount; }
    public void setDiscount(int discount) { this.discount = discount; }

    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
}