package assistant.main.javaServer.Services;

import assistant.main.javaServer.Entities.CartItem;
import assistant.main.javaServer.Repository.CartItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartItemService {
    @Autowired
    private CartItemRepository cartItemRepository;

    // Add Cart Item
    public CartItem addCartItem(CartItem cartItem){
        return cartItemRepository.save(cartItem);
    }
    // Get CartItem By CartId
    public List<CartItem> getCartItemByCart(Long id){
        return cartItemRepository.findByCartId(id);
    }
    // Add or Reduce quantity
    public void addOrReduceQuantityCard(Integer number, Long id){
        CartItem cartItem = cartItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CartItem not found"));
        if (number == 0) {
            throw new RuntimeException("Number must be greater than zero");
        }
        int newQuantity = cartItem.getQuantity() + number;
        if (newQuantity < 0) {
            throw new RuntimeException("Quantity cannot be negative");
        } else if (newQuantity == 0) {
            cartItemRepository.deleteById(id);
            return;
        }
        cartItem.setQuantity(newQuantity);
        cartItemRepository.save(cartItem);
    }


}
