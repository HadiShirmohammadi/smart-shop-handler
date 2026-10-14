package assistant.main.javaServer.Dto;

import assistant.main.javaServer.Entities.Cart;
import java.util.List;

/** Public cart representation: no user credentials or recursive entity references. */
public record CartResponse(Long id, List<Item> items) {
    public record Item(Long id, int quantity, int price, ProductResponse product) {}

    public static CartResponse from(Cart cart) {
        return new CartResponse(cart.getId(), cart.getItems().stream()
                .map(item -> new Item(item.getId(), item.getQuantity(), item.getPrice(),
                        new ProductResponse(item.getProduct())))
                .toList());
    }
}
