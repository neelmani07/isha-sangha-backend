package com.ishaworld.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/** Files on local disk under app.uploads.dir. Fine for development or a server with a persistent disk. */
public class LocalObjectStore implements ObjectStore {

    private final Path root;

    public LocalObjectStore(String dir) throws IOException {
        this.root = Path.of(dir).toAbsolutePath().normalize();
        Files.createDirectories(root);
    }

    @Override
    public void put(String path, InputStream content, long size, String contentType) throws IOException {
        Path target = resolve(path);
        Files.createDirectories(target.getParent());
        Files.copy(content, target, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public boolean exists(String path) {
        return Files.exists(resolve(path));
    }

    @Override
    public Resource get(String path) {
        Path file = resolve(path);
        return Files.exists(file) ? new FileSystemResource(file) : null;
    }

    private Path resolve(String path) {
        Path resolved = root.resolve(path).normalize();
        if (!resolved.startsWith(root)) {
            throw new IllegalArgumentException("Invalid storage path");
        }
        return resolved;
    }
}
