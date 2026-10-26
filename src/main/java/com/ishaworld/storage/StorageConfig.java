package com.ishaworld.storage;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageConfig {

    @Bean
    ObjectStore objectStore(
            @Value("${app.storage.type:local}") String type,
            @Value("${app.uploads.dir:uploads}") String uploadsDir,
            @Value("${app.storage.s3.bucket:}") String bucket,
            @Value("${app.storage.s3.region:}") String region,
            @Value("${app.storage.s3.endpoint:}") String endpoint,
            @Value("${app.storage.s3.access-key:}") String accessKey,
            @Value("${app.storage.s3.secret-key:}") String secretKey
    ) throws IOException {
        return switch (type.toLowerCase()) {
            case "s3" -> new S3ObjectStore(bucket, region, endpoint, accessKey, secretKey);
            case "local" -> new LocalObjectStore(uploadsDir);
            default -> throw new IllegalStateException("app.storage.type must be 'local' or 's3', not '" + type + "'");
        };
    }
}
