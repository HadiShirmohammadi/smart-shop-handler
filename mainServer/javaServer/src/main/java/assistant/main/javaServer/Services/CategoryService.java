package assistant.main.javaServer.Services;

import assistant.main.javaServer.Dto.CategoryNode;
import assistant.main.javaServer.Dto.CategoryRequest;
import assistant.main.javaServer.Entities.Category;
import assistant.main.javaServer.Entities.Product;
import assistant.main.javaServer.Repository.CategoryRepository;
import assistant.main.javaServer.Repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public Category create(CategoryRequest request){
        Category category = new Category();
        category.setName(request.name());
        if (request.parentId() != null){
            Category parent = categoryRepository.findById(request.parentId())
                    .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,"Parent not found!"
            ));
            category.setParent(parent);
        }
        return categoryRepository.save(category);
    }

    @Transactional(readOnly = true)
    public List<CategoryNode> getTree(){
        List<Category> allCategory = categoryRepository.findAll(Sort.by("name"));

        Map<Long,CategoryNode> nodes = new HashMap<>();
        for (Category c : allCategory){
            nodes.put(c.getId(), new CategoryNode(c.getName(), c.getId() , new ArrayList<>()));
        }
        List<CategoryNode> roots = new ArrayList<>();
        for (Category c : allCategory){
            CategoryNode node = nodes.get(c.getId());
            if (c.getParent() == null) {
                roots.add(node);
            } else{
                nodes.get(c.getParent().getId()).children().add(node);
            }
        }
        return roots;
    }

    @Transactional(readOnly = true)
    public List<Product> getProducts(Long categoryId) {
        Set<Long> ids = descendantIds(categoryId);
        return productRepository.findByCategoryIdIn(ids);
    }

    private Set<Long> descendantIds(Long rootId) {
        List<Category> all = categoryRepository.findAll();

        Map<Long, List<Long>> childrenByParent = new HashMap<>();
        for (Category c : all) {
            if (c.getParent() != null) {
                childrenByParent
                        .computeIfAbsent(c.getParent().getId(), k -> new ArrayList<>())
                        .add(c.getId());
            }
        }

        Set<Long> result = new HashSet<>();
        Deque<Long> queue = new ArrayDeque<>();
        queue.add(rootId);
        while (!queue.isEmpty()) {
            Long current = queue.poll();
            if (result.add(current)) {
                queue.addAll(childrenByParent.getOrDefault(current, List.of()));
            }
        }
        return result;
    }
    public Category findCategory(Long id){
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Category Not Found!"));
    }

}
