package assistant.main.javaServer.Controllers;

import assistant.main.javaServer.Dto.CartItemRequest;
import assistant.main.javaServer.Entities.Cart;
import assistant.main.javaServer.Entities.User;
import assistant.main.javaServer.Services.CartService;
import assistant.main.javaServer.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/cart")
public class CartController {
    @Autowired
    private CartService cartService;
    @Autowired
    private UserService userService;
    // Add Or Delete Item
    @PostMapping("/items")
    public ResponseEntity<Cart> addOrDeleteItemToCart(@RequestBody CartItemRequest request, Authentication authentication){
        String username = authentication.getName();
        User user = userService.findUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not Found"));
        Cart updateCart = cartService.itemsCart(user.getId(),request);
        return ResponseEntity.ok(updateCart);
    }
    //Get Cart By user
    @GetMapping("/get")
    public ResponseEntity<Cart> getCartByUser(Authentication authentication){
        String username = authentication.getName();
        return ResponseEntity.ok(cartService.getCartByUsername(username));
    }
    //Delete All Cart
    @DeleteMapping("/delete")
    public void deleteCart(Authentication authentication){
        String username = authentication.getName();
        Cart cart = cartService.getCartByUsername(username);
        cartService.deleteCart(cart.getId());
    }
}
