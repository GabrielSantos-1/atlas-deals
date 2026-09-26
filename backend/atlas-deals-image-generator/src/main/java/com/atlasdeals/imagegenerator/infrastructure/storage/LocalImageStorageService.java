package com.atlasdeals.imagegenerator.infrastructure.storage;

import com.atlasdeals.imagegenerator.infrastructure.config.ImageProperties;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class LocalImageStorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalImageStorageService.class);

    private final ImageProperties imageProperties;
    private Path storagePath;

    public LocalImageStorageService(ImageProperties imageProperties) {
        this.imageProperties = imageProperties;
    }

    @PostConstruct
    public void init() {
        this.storagePath = Paths.get(imageProperties.getStorage().getPath()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(storagePath);
            log.info("Image storage initialized at: {}", storagePath);
        } catch (IOException e) {
            throw new IllegalStateException("Could not create image storage directory: " + storagePath, e);
        }
    }

    public String save(byte[] data, String filename) throws IOException {
        String uniqueName = UUID.randomUUID() + "_" + sanitizeFilename(filename);
        Path targetPath = storagePath.resolve(uniqueName);
        Files.write(targetPath, data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        log.debug("Saved image file: {} ({} bytes)", targetPath, data.length);
        return targetPath.toString();
    }

    public byte[] load(String path) throws IOException {
        Path filePath = resolvePath(path);
        if (!Files.exists(filePath)) {
            throw new IOException("Image file not found: " + filePath);
        }
        return Files.readAllBytes(filePath);
    }

    public void delete(String path) throws IOException {
        Path filePath = resolvePath(path);
        if (Files.exists(filePath)) {
            Files.delete(filePath);
            log.debug("Deleted image file: {}", filePath);
        }
    }

    public String getUrl(String path) {
        Path filePath = resolvePath(path);
        return "/api/images/file/" + filePath.getFileName().toString();
    }

    public void cleanOldImages(Duration olderThan) throws IOException {
        Instant cutoff = Instant.now().minus(olderThan);
        try (var files = Files.list(storagePath)) {
            files.forEach(file -> {
                try {
                    Instant lastModified = Files.getLastModifiedTime(file).toInstant();
                    if (lastModified.isBefore(cutoff)) {
                        Files.delete(file);
                        log.info("Cleaned up old image: {}", file);
                    }
                } catch (IOException e) {
                    log.warn("Failed to clean up old image: {}", file, e);
                }
            });
        }
    }

    private Path resolvePath(String path) {
        Path p = Paths.get(path);
        if (p.isAbsolute()) return p;
        return storagePath.resolve(p).normalize();
    }

    private String sanitizeFilename(String filename) {
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
