package com.dailymart.controller;

import com.dailymart.service.ImageUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class ImageController {

    private final ImageUploadService imageUploadService;

    @PostMapping(
            value = "/product-image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> uploadProductImage(
            @RequestParam("file") MultipartFile file) throws IOException {

        String url = imageUploadService.uploadImage(file, "products");

        return ResponseEntity.ok(Map.of(
                "imageUrl", url,
                "message", "Image uploaded successfully"
        ));
    }

    @PostMapping(
            value = "/profile-image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> uploadProfileImage(
            @RequestParam("file") MultipartFile file) throws IOException {

        String url = imageUploadService.uploadImage(file, "profiles");

        return ResponseEntity.ok(Map.of(
                "imageUrl", url,
                "message", "Profile image uploaded successfully"
        ));
    }
}