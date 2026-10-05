package assistant.main.javaServer.Services;

import assistant.main.javaServer.Entities.Cart;
import assistant.main.javaServer.Repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;
    // Create Cart
    public Cart createCart(Cart cart){
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
