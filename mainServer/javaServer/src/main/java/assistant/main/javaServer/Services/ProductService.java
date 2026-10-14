package assistant.main.javaServer.Services;

import assistant.main.javaServer.Dto.ProductResponse;
import assistant.main.javaServer.Entities.Product;
import assistant.main.javaServer.Repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;
    // Add Product
    public Product addProduct(Product product){
        return productRepository.save(product);
    }
    // Delete Product
    public void deleteProduct(Long id){
        productRepository.deleteById(id);
    }
    // Get All Product
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProduct() {
        return productRepository.findAll()
                .stream()
                .map(ProductResponse::new)
                .toList();
    }
    // Get Product
    public Product getProduct(Long id){
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found!"));
    }
    // Search Products
    public List<Product> searchProducts(String q) {
        if (q == null || q.isBlank()) return productRepository.findAll();
        String key = q.trim();
        return productRepository
                .findDistinctByNameContainingIgnoreCaseOrTags_NameContainingIgnoreCase(key, key);
    }

}
