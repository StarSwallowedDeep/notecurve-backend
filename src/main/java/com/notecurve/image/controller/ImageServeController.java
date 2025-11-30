package com.notecurve.image.controller;

import java.io.IOException;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.notecurve.image.service.ImageServeService;

@RestController
@RequestMapping("/api/images")
public class ImageServeController {

    private static final Logger LOGGER = Logger.getLogger(ImageServeController.class.getName());

    private final ImageServeService ImageServeService;

    @Value("${file.upload-dir}")
    private String uploadDirStr;

    public ImageServeController(ImageServeService ImageServeService) {
        this.ImageServeService = ImageServeService;
    }

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> serveImage(@PathVariable("filename") String filename) {
        try {
            // 이미지 파일 제공
            Resource resource = ImageServeService.serveImage(filename);

            // MIME 타입 결정
            String contentType = ImageServeService.determineContentType(resource.getFile().toPath());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);

        } catch (IOException | IllegalArgumentException e) {
            LOGGER.severe("Error: " + e.getMessage());
            return ResponseEntity.status(400).body(null);
        }
    }
}
