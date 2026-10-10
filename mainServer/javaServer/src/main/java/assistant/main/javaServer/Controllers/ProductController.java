package assistant.main.javaServer.Controllers;


import assistant.main.javaServer.Dto.ProductRequest;
import assistant.main.javaServer.Dto.ProductResponse;
import assistant.main.javaServer.Entities.Product;
import assistant.main.javaServer.Entities.Tag;
import assistant.main.javaServer.Entities.User;
import assistant.main.javaServer.Repository.ProductRepository;
import assistant.main.javaServer.Services.ProductService;
import assistant.main.javaServer.Services.TagService;
import assistant.main.javaServer.Services.UserService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @Autowired
    private TagService tagService;

    // Add Product
    @PostMapping("/add")
    public ResponseEntity<?> addProduct(@RequestBody ProductRequest req, Authentication authentication){
        String username = authentication.getName();
        Optional<User> userOptional = userService.findUserByUsername(username);
        if (userOptional.isPresent()){
            User user = userOptional.get();
            if (Boolean.TRUE.equals(user.getAdmin())){
                Product product = new Product();
                product.setName(req.getName());
                product.setShortDesc(req.getShortDesc());
                product.setLongDesc(req.getLongDesc());
                product.setPrice(req.getPrice());
                product.setDiscount(req.getDiscount());
                product.setImages(req.getImages());
                Set<Tag> tagSet = new HashSet<>();
                if (req.getTags() != null) {
                    for (String tagName : req.getTags()) {
                        tagSet.add(tagService.getOrCreateTag(tagName));
                    }
                }
                product.setTags(tagSet);

                Product addProduct = productService.addProduct(product);
                return ResponseEntity.ok(addProduct);
            }
            return ResponseEntity.status(403).body("user doesn't have access");
        }
        return ResponseEntity.status(404).body("User not found");

    }

    //Delete Product
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id, Authentication authentication){
        String username = authentication.getName();
        Optional<User> userOptional = userService.findUserByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (Boolean.TRUE.equals(user.getAdmin())) {
                productService.deleteProduct(id);
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.status(403).body("user doesn't have access");
        }
        return ResponseEntity.status(404).body("User not found");
    }

    // Get All Product
    @GetMapping("/products")
    public List<ProductResponse> getAllProducts(){
        return productService.getAllProduct();
    }

    //Search Product
    @GetMapping("/search/{key}")
    public List<Product> searchProduct(@PathVariable String key){
        return productService.searchProducts(key);
    }
}
