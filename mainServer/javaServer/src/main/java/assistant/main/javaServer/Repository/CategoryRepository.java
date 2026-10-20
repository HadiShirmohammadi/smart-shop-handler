package assistant.main.javaServer.Repository;

import assistant.main.javaServer.Entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
