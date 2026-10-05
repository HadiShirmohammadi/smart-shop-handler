package assistant.main.javaServer.Services;

import assistant.main.javaServer.Entities.Product;
import assistant.main.javaServer.Repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public List<Product> getAllProduct(){
        return productRepository.findAll();
    }

}
