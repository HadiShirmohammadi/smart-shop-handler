package assistant.main.javaServer.Services;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import assistant.main.javaServer.Entities.User;
import assistant.main.javaServer.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Add User
    public User addUser(User user) {

        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);
        user.setAdmin(false);
        return userRepository.save(user);
    }
    // Get User By username
    public Optional<User> findUserByUsername(String username){
        return userRepository.findByUsername(username);
    }
    //Chack User password
    public boolean checkPassword(String rawPassword,String hashedPassword){
        return passwordEncoder.matches(rawPassword,hashedPassword);
    }
    // Delete User
    @Transactional
    public Boolean deleteUser(String username){
        if (!userRepository.existsByUsername(username)){
            return false;
        }
        userRepository.deleteByUsername(username);
        return true;
    }
    // Get All User
    public List<User> getAllUser(){
        return userRepository.findAll();
    }
}
