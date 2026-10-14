package assistant.main.javaServer.Controllers;

import assistant.main.javaServer.Services.ProductImageService;
import assistant.main.javaServer.Services.UserService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

@RestController
@RequestMapping("/product")
public class ProductImageController {
    private final ProductImageService images;
    private final UserService users;

    public ProductImageController(ProductImageService images, UserService users) {
        this.images = images;
        this.users = users;
    }

    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, String> upload(@RequestPart("file") MultipartFile file, Authentication authentication) throws IOException {
        if (authentication == null || !users.findUserByUsername(authentication.getName())
                .map(user -> Boolean.TRUE.equals(user.getAdmin())).orElse(false)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only administrators can upload product images");
        }
        return Map.of("url", images.store(file));
    }

    @GetMapping("/images/{filename}")
    public ResponseEntity<FileSystemResource> image(@PathVariable String filename) {
        Path path = images.resolve(filename);
        return ResponseEntity.ok()
                .header("X-Content-Type-Options", "nosniff")
                .header("Cache-Control", "public, max-age=31536000, immutable")
                .contentType(filename.endsWith(".png") ? MediaType.IMAGE_PNG : MediaType.IMAGE_JPEG)
                .body(new FileSystemResource(path));
    }
}
