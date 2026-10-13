package assistant.main.javaServer.Services;

import assistant.main.javaServer.Dto.CartItemRequest;
import assistant.main.javaServer.Entities.Cart;
import assistant.main.javaServer.Entities.CartItem;
import assistant.main.javaServer.Entities.Product;
import assistant.main.javaServer.Entities.User;
import assistant.main.javaServer.Repository.CartItemRepository;
import assistant.main.javaServer.Repository.CartRepository;
import assistant.main.javaServer.Repository.ProductRepository;
import assistant.main.javaServer.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    // Create Cart
    public Cart itemsCart(Long userId, CartItemRequest request){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Cart cart = cartRepository.findByUserUsername(user.getUsername()).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUser(user);
            return cartRepository.save(newCart);
        });

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Optional<CartItem> existItem = cart.getItems()
                .stream().filter(item ->  item.getProduct().getId().equals(product.getId()))
                .findFirst();

        if (existItem.isPresent()){
            CartItem item = existItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            if (item.getQuantity() > 0){
                cartItemRepository.save(item);
            }else {
                cartItemRepository.delete(item);
                cart.getItems().remove(item);
            }
        }else{
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(request.getQuantity());
            cartItemRepository.save(newItem);
            cart.getItems().add(newItem);
        }
        return cartRepository.save(cart);
    }
    //Get Cart By Username
    public Cart getCartByUsername(String username){
        return cartRepository.findByUserUsername(username).orElseThrow(() -> new RuntimeException("Cart not found"));
    }
    // Delete Cart
    public void deleteCart(Long id){
        cartRepository.deleteById(id);
    }

}
