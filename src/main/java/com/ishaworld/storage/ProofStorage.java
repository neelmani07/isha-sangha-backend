package com.ishaworld.storage;

import com.ishaworld.exception.BusinessException;
import com.ishaworld.exception.NotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * Stores proof documents (certificates, letters) for teacher and center submissions through {@link ObjectStore}.
 * Proofs are private: only admins can read them back.
 *
 * Keys look like "{userId}-{uuid}.pdf" so a member can only attach proofs they uploaded themselves.
 */
@Component
public class ProofStorage {

    private static final long MAX_BYTES = 5L * 1024 * 1024;
    private static final Map<String, String> ALLOWED = Map.of(
            "application/pdf", "pdf",
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp");
    private static final Pattern KEY = Pattern.compile("^\\d+-[0-9a-f\\-]{36}\\.(pdf|jpg|png|webp)$");

    private static final String FOLDER = "proofs/";

    private final ObjectStore store;

    public ProofStorage(ObjectStore store) {
        this.store = store;
    }

    public String store(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Please choose a file to upload.");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new BusinessException("The file is too large. The limit is 5 MB.");
        }
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        String extension = ALLOWED.get(contentType);
        if (extension == null) {
            throw new BusinessException("Please upload a PDF, JPG, PNG or WebP file.");
        }

        String key = userId + "-" + UUID.randomUUID() + "." + extension;
        try (InputStream in = file.getInputStream()) {
            store.put(FOLDER + key, in, file.getSize(), contentType);
        } catch (IOException ex) {
            throw new BusinessException("Could not save the file. Please try again.");
        }
        return key;
    }

    /** True when the key is well-formed, belongs to this member and the file exists. */
    public boolean isOwnedBy(String key, Long userId) {
        return key != null && KEY.matcher(key).matches() && key.startsWith(userId + "-") && store.exists(FOLDER + key);
    }

    public Resource load(String key) {
        if (key == null || !KEY.matcher(key).matches()) {
            throw new NotFoundException("Proof not found");
        }
        Resource resource = store.get(FOLDER + key);
        if (resource == null) {
            throw new NotFoundException("Proof not found");
        }
        return resource;
    }

    public static String contentTypeOf(String key) {
        String ext = key.substring(key.lastIndexOf('.') + 1);
        return switch (ext) {
            case "pdf" -> "application/pdf";
            case "png" -> "image/png";
            case "webp" -> "image/webp";
            default -> "image/jpeg";
        };
    }
}
