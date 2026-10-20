package assistant.main.javaServer.Repository;

import assistant.main.javaServer.Entities.Product;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long> {
    @Override
    @EntityGraph(attributePaths = "tags")
    List<Product> findAll();

    List<Product> findDistinctByNameContainingIgnoreCaseOrTags_NameContainingIgnoreCase(
            String name, String tagName);

    List<Product> findByCategoryIdIn(Collection<Long> categoryIds);

}
