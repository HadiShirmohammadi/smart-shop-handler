package assistant.main.javaServer.Controllers;

import assistant.main.javaServer.Dto.CategoryNode;
import assistant.main.javaServer.Dto.CategoryRequest;
import assistant.main.javaServer.Entities.Category;
import assistant.main.javaServer.Entities.Product;
import assistant.main.javaServer.Services.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    // Add Category
    @PostMapping("/add")
    public ResponseEntity<CategoryNode> addCategory(@RequestBody CategoryRequest request){
        Category category = categoryService.create(request);
        CategoryNode node = new CategoryNode(category.getName(), category.getId(), List.of());
        return ResponseEntity.status(HttpStatus.CREATED).body(node);
    }

    // Get Tree
    @GetMapping("/tree")
    public List<CategoryNode> tree(){
        return categoryService.getTree();
    }

    // Get Products
    @GetMapping("/{id}/products")
    public List<Product> products(@PathVariable Long id){
        return categoryService.getProducts(id);
    }
    // Delete Category
    @DeleteMapping("/{id}/delete")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id){
        categoryService.deleteCategory(id);
        return ResponseEntity.ok().build();
    }

}
