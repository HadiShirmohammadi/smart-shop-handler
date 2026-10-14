package assistant.main.javaServer;

import assistant.main.javaServer.Services.ProductImageService;
import assistant.main.javaServer.Controllers.ProductImageController;
import assistant.main.javaServer.Services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.nio.file.Files;
import static org.junit.jupiter.api.Assertions.*;

class ProductImageServiceTests {
    @TempDir Path directory;

    @Test
    void storesRealImageWithGeneratedNameAndReadableContent() throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB), "png", bytes);
        ProductImageService service = new ProductImageService(directory.toString());
        String url = service.store(new MockMultipartFile("file", "../../unsafe.png", "image/png", bytes.toByteArray()));
        assertTrue(url.matches("/product/images/[0-9a-f-]{36}\\.png"));
        Path image = service.resolve(url.substring(url.lastIndexOf('/') + 1));
        assertTrue(Files.isRegularFile(image));
        assertEquals(4, ImageIO.read(image.toFile()).getWidth());
    }

    @Test
    void rejectsFakeImagesAndTraversal() {
        ProductImageService service = new ProductImageService(directory.toString());
        assertThrows(ResponseStatusException.class, () -> service.store(new MockMultipartFile("file", "fake.png", "image/png", "not an image".getBytes())));
        assertThrows(ResponseStatusException.class, () -> service.resolve("../../secret.png"));
        assertThrows(ResponseStatusException.class, () -> service.store(new MockMultipartFile("file", new byte[0])));
    }

    @Test
    void rejectsUnauthenticatedUpload() {
        ProductImageController controller = new ProductImageController(new ProductImageService(directory.toString()), new UserService());
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> controller.upload(new MockMultipartFile("file", new byte[]{1}), null));
        assertEquals(403, error.getStatusCode().value());
    }
}
