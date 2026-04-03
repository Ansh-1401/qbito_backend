package com.scan2dine.scan2dine.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/media")
@CrossOrigin(origins = "${app.frontend.url}")
public class MediaController {

    private final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
        }

        try {
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            // Generate unique filename
            String newFilename = UUID.randomUUID().toString() + extension;
            Path filePath = Paths.get(UPLOAD_DIR + newFilename);

            // Save file natively
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Construct URI matching WebMvcConfigurer
            String fileDownloadUri = "http://localhost:8080/media/" + newFilename;

            return ResponseEntity.ok(Map.of("url", fileDownloadUri));
        } catch (IOException ex) {
            ex.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to store file"));
        }
    }
}
