package assistant.main.javaServer.Repository;

import assistant.main.javaServer.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByUsername(String username);
    @Modifying
    void deleteByUsername(String username);
    Boolean existsByUsername(String username);
}
