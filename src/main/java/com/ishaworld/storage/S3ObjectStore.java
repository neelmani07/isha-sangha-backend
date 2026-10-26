package com.ishaworld.storage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * Files in a private S3-compatible bucket. Works with AWS S3, Cloudflare R2 and DigitalOcean Spaces:
 * for R2 and Spaces set the endpoint; for AWS leave it blank. Nothing in the bucket is public;
 * images are served through the API (and can be cached by a CDN in front of it).
 */
public class S3ObjectStore implements ObjectStore {

    private final S3Client client;
    private final String bucket;

    public S3ObjectStore(String bucket, String region, String endpoint, String accessKey, String secretKey) {
        if (bucket == null || bucket.isBlank()) {
            throw new IllegalStateException("app.storage.s3.bucket (STORAGE_BUCKET) is required when app.storage.type=s3");
        }
        S3ClientBuilder builder = S3Client.builder().region(Region.of(region == null || region.isBlank() ? "auto" : region));
        if (endpoint != null && !endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint)).forcePathStyle(true);
        }
        if (accessKey != null && !accessKey.isBlank()) {
            builder.credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)));
        }
        this.client = builder.build();
        this.bucket = bucket;
    }

    @Override
    public void put(String path, InputStream content, long size, String contentType) throws IOException {
        try {
            client.putObject(PutObjectRequest.builder().bucket(bucket).key(path).contentType(contentType).build(),
                    RequestBody.fromInputStream(content, size));
        } catch (S3Exception ex) {
            throw new IOException("Upload to storage failed", ex);
        }
    }

    @Override
    public boolean exists(String path) {
        try {
            client.headObject(HeadObjectRequest.builder().bucket(bucket).key(path).build());
            return true;
        } catch (NoSuchKeyException ex) {
            return false;
        } catch (S3Exception ex) {
            if (ex.statusCode() == 404) {
                return false;
            }
            throw ex;
        }
    }

    @Override
    public Resource get(String path) {
        try {
            return new InputStreamResource(client.getObject(GetObjectRequest.builder().bucket(bucket).key(path).build()));
        } catch (NoSuchKeyException ex) {
            return null;
        } catch (S3Exception ex) {
            if (ex.statusCode() == 404) {
                return null;
            }
            throw ex;
        }
    }
}
