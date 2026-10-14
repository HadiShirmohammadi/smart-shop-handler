package assistant.main.javaServer.Services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.UUID;

@Service
public class ProductImageService {
    private final Path directory;

    public ProductImageService(@Value("${shop.upload-directory:uploads/products}") String directory) {
        this.directory = Path.of(directory).toAbsolutePath().normalize();
    }

    public String store(MultipartFile file) throws IOException {
        if (file.isEmpty() || file.getSize() > 5L * 1024 * 1024) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose an image up to 5 MB");
        }
        // Decode and re-encode raster images instead of trusting filenames or MIME headers.
        BufferedImage image;
        String extension;
        try (ImageInputStream input = ImageIO.createImageInputStream(file.getInputStream())) {
            if (input == null) throw invalidImage();
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) throw invalidImage();
            ImageReader reader = readers.next();
            try {
                reader.setInput(input);
                String format = reader.getFormatName().toLowerCase(java.util.Locale.ROOT);
                extension = switch (format) { case "jpeg", "jpg" -> "jpg"; case "png" -> "png"; default -> throw invalidImage(); };
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width <= 0 || height <= 0 || (long) width * height > 20_000_000) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image must not exceed 20 megapixels");
                }
                image = reader.read(0);
            } finally { reader.dispose(); }
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image could not be decoded", e);
        }
        Files.createDirectories(directory);
        String filename = UUID.randomUUID() + "." + extension;
        Path destination = directory.resolve(filename);
        try {
            if (!ImageIO.write(image, extension, destination.toFile())) throw new IOException("Image encoder unavailable");
        } catch (IOException e) { Files.deleteIfExists(destination); throw e; }
        return "/product/images/" + filename;
    }

    public Path resolve(String filename) {
        if (!filename.matches("[0-9a-f-]{36}\\.(jpg|png)")) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        Path path = directory.resolve(filename).normalize();
        if (!path.startsWith(directory) || !Files.isRegularFile(path)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return path;
    }

    private ResponseStatusException invalidImage() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only valid JPEG and PNG images are supported");
    }
}
