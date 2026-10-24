package com.ishaworld.storage;

import com.ishaworld.exception.BusinessException;
import com.ishaworld.exception.NotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * Public images members upload with their listings (e.g. an initiative photo). Unlike proofs these are
 * shown to everyone. Stored on local disk for now; move to cloud storage + CDN when deploying.
 */
@Component
public class ImageStorage {

    public static final String PUBLIC_PATH = "/api/v1/images/";

    private static final long MAX_BYTES = 5L * 1024 * 1024;
    private static final Map<String, String> ALLOWED = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp");
    private static final Pattern KEY = Pattern.compile("^\\d+-[0-9a-f\\-]{36}\\.(jpg|png|webp)$");

    private final Path root;

    public ImageStorage(@Value("${app.uploads.dir:uploads}") String dir) throws IOException {
        this.root = Path.of(dir, "images").toAbsolutePath().normalize();
        Files.createDirectories(root);
    }

    public String store(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Please choose a photo to upload.");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new BusinessException("The photo is too large. The limit is 5 MB.");
        }
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        String extension = ALLOWED.get(contentType);
        if (extension == null) {
            throw new BusinessException("Please upload a JPG, PNG or WebP photo.");
        }
        String key = userId + "-" + UUID.randomUUID() + "." + extension;
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, root.resolve(key), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new BusinessException("Could not save the photo. Please try again.");
        }
        return key;
    }

    public boolean isOwnedBy(String key, Long userId) {
        return key != null && KEY.matcher(key).matches() && key.startsWith(userId + "-") && Files.exists(root.resolve(key));
    }

    public Resource load(String key) {
        if (key == null || !KEY.matcher(key).matches()) {
            throw new NotFoundException("Image not found");
        }
        Path path = root.resolve(key).normalize();
        if (!path.startsWith(root) || !Files.exists(path)) {
            throw new NotFoundException("Image not found");
        }
        return new FileSystemResource(path);
    }

    public static String contentTypeOf(String key) {
        if (key.endsWith(".png")) return "image/png";
        if (key.endsWith(".webp")) return "image/webp";
        return "image/jpeg";
    }

    public static String publicUrl(String key) {
        return key == null ? null : PUBLIC_PATH + key;
    }
}
