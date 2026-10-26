package com.ishaworld.storage;

import java.io.IOException;
import java.io.InputStream;
import org.springframework.core.io.Resource;

/**
 * Where uploaded files live. Local disk in development, an S3-compatible bucket in production
 * (chosen with app.storage.type). Paths look like "images/{key}" or "proofs/{key}".
 */
public interface ObjectStore {

    void put(String path, InputStream content, long size, String contentType) throws IOException;

    boolean exists(String path);

    /** The file, or null when it does not exist. */
    Resource get(String path);
}
