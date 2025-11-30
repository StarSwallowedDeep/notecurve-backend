package com.notecurve.image.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.notecurve.image.service.ImageUploadService;

@RestController
@RequestMapping("/api/images")
public class ImageUploadController {

    private final ImageUploadService imageUploadService;

    public ImageUploadController(ImageUploadService imageUploadService) {
        this.imageUploadService = imageUploadService;
    }

    @PostMapping("/uploadMultiple")
    public ResponseEntity<?> uploadImages(@RequestParam("images") List<MultipartFile> files) {
        // 파일 확장자 검증
        List<String> invalidFiles = imageUploadService.validateFiles(files);
        if (!invalidFiles.isEmpty()) {
            return ResponseEntity.badRequest().body("허용되지 않는 파일 형식: " + String.join(", ", invalidFiles));
        }

        // 저장할 파일 경로 리스트
        List<String> savedUrls = new ArrayList<>();
        for (MultipartFile file : files) {
            try {
                String filename = imageUploadService.saveFile(file);
                savedUrls.add("/images/" + filename);
            } catch (IOException e) {
                return ResponseEntity.status(500).body("파일 저장 중 오류 발생");
            }
        }

        return ResponseEntity.ok(savedUrls);
    }
}
