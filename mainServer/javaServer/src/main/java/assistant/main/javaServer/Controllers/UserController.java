package assistant.main.javaServer.Controllers;

import assistant.main.javaServer.Dto.AuthResponse;
import assistant.main.javaServer.Dto.UserprofileResponse;
import assistant.main.javaServer.Entities.User;
import assistant.main.javaServer.Services.JwtService;
import assistant.main.javaServer.Services.UserService;
import org.apache.logging.log4j.message.StringFormattedMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "/auth")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private JwtService jwtService;


    // Register User
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user){
        try {
            User saveUser = userService.addUser(user);
            String token = jwtService.generateToken(saveUser.getUsername(), saveUser.getAdmin());
            AuthResponse response = new AuthResponse(token, saveUser.getUsername(), saveUser.getAdmin());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    // Login User
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User request){
        Optional<User> userOpt = userService.findUserByUsername(request.getUsername());
        if (userOpt.isPresent()){
            User user = userOpt.get();
            if (userService.checkPassword(request.getPassword(), user.getPassword())){
                String token = jwtService.generateToken(user.getUsername(), user.getAdmin());
                AuthResponse authResponse = new AuthResponse(token, user.getUsername(), user.getAdmin());
                return ResponseEntity.ok(authResponse);
            }
        }
        return ResponseEntity.status(401).body("User password or username incorrect!!");
    }
    // Get User Profile
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication authentication){
        String username = authentication.getName();
        Optional<User> userOpt = userService.findUserByUsername(username);
        if (userOpt.isPresent()){
            User user = userOpt.get();
            UserprofileResponse response = new UserprofileResponse(
                    user.getFirstname(),
                    user.getLastname(),
                    user.getUsername(),
                    user.getPhone(),
                    user.getAddress(),
                    user.getAdmin()
            );
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(404).build();
    }
    // Delete User
    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteUser(Authentication authentication){
        String username = authentication.getName();
        Boolean response = userService.deleteUser(username);
        if (response){
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(404).build();
    }
    // Get All User
    @GetMapping("/users")
    public ResponseEntity<?> getAllUser(Authentication authentication){
        String username = authentication.getName();
        Optional<User> userOpt = userService.findUserByUsername(username);
        if (userOpt.isPresent()){
            User user = userOpt.get();
            if (Boolean.TRUE.equals(user.getAdmin())){
                List<User> userList = userService.getAllUser();
                List<UserprofileResponse> responseList = userList.stream()
                        .map(userL -> new UserprofileResponse(
                                userL.getFirstname(),
                                userL.getLastname(),
                                userL.getUsername(),
                                userL.getPhone(),
                                userL.getAddress(),
                                userL.getAdmin()
                        ))
                        .toList();
                return ResponseEntity.ok(responseList);
            }
            return ResponseEntity.status(403).body("This user is not an admin.");
        }
        return ResponseEntity.status(404).body("User not found");
    }
}
