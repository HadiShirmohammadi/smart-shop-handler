package assistant.main.javaServer.Services;

import assistant.main.javaServer.Dto.CartItemRequest;
import assistant.main.javaServer.Dto.CartResponse;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@Transactional
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
    public CartResponse itemsCart(Long userId, CartItemRequest request){
        if (request.getProductId() == null || request.getQuantity() == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product ID and a non-zero quantity are required");
        }
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
            if (request.getQuantity() <= 0) {
                throw new RuntimeException("Quantity must be greater than zero");
            }
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(request.getQuantity());
            newItem.setPrice(product.getPrice());
            cartItemRepository.save(newItem);
            cart.getItems().add(newItem);
        }
        return CartResponse.from(cartRepository.save(cart));
    }
    //Get Cart By Username
    @Transactional(readOnly = true)
    public CartResponse getCartByUsername(String username){
        return cartRepository.findByUserUsername(username)
                .map(CartResponse::from)
                .orElseGet(() -> new CartResponse(null, java.util.List.of()));
    }

    public void deleteCartByUsername(String username) {
        cartRepository.findByUserUsername(username).ifPresent(cartRepository::delete);
    }
    // Delete Cart
    public void deleteCart(Long id){
        cartRepository.deleteById(id);
    }

}
