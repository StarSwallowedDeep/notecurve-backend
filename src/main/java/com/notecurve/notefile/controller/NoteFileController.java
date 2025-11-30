package com.notecurve.notefile.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

import com.notecurve.auth.security.UserDetailsImpl;
import com.notecurve.notefile.dto.NoteFileDTO;
import com.notecurve.notefile.domain.NoteFile;
import com.notecurve.notefile.service.NoteFileService;

@RestController
@RequestMapping("/api/files")
public class NoteFileController {

    @Value("${note.upload-dir}")
    private String uploadDir;

    private final NoteFileService fileService;

    public NoteFileController(NoteFileService fileService) {
        this.fileService = fileService;
    }

    private UserDetailsImpl getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserDetailsImpl)) return null;
        return (UserDetailsImpl) principal;
    }

    private ResponseEntity<String> checkAuthorization() {
        UserDetailsImpl userDetails = getCurrentUser();
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증되지 않은 사용자입니다.");
        }
        return null;
    }

    @PostMapping("/upload/{noteId}")
    public ResponseEntity<String> uploadFiles(@PathVariable Long noteId,
                                              @RequestParam("files") MultipartFile[] files) {
        ResponseEntity<String> authResponse = checkAuthorization();
        if (authResponse != null) return authResponse;

        Long userId = getCurrentUser().getUser().getId();
        try {
            for (MultipartFile file : files) {
                fileService.uploadFile(noteId, file, userId);
            }
            return ResponseEntity.ok("파일 여러 개 업로드 성공");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("업로드 실패: " + e.getMessage());
        }
    }

    @GetMapping("/note/{noteId}")
    public ResponseEntity<List<NoteFileDTO>> getFilesByNote(@PathVariable Long noteId) {
        if (checkAuthorization() != null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Long userId = getCurrentUser().getUser().getId();
        return ResponseEntity.ok(fileService.getFilesByNoteId(noteId, userId));
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) {
        ResponseEntity<String> authResponse = checkAuthorization();
        if (authResponse != null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Long userId = getCurrentUser().getUser().getId();

        try {
            NoteFile file = fileService.downloadFile(fileId, userId);
            Path path = Paths.get(uploadDir).resolve(file.getFilePath()).resolve(file.getStoredName());
            Resource resource = new FileSystemResource(path);

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            ContentDisposition contentDisposition = ContentDisposition.attachment()
                    .filename(file.getOriginalName(), StandardCharsets.UTF_8)
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteFiles(@RequestBody List<Long> fileIds) {
        ResponseEntity<String> authResponse = checkAuthorization();
        if (authResponse != null) return authResponse;

        Long userId = getCurrentUser().getUser().getId();
        try {
            for (Long fileId : fileIds) {
                fileService.deleteFile(fileId, userId);
            }
            return ResponseEntity.ok("여러 파일 삭제 성공");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("삭제 실패: " + e.getMessage());
        }
    }

    @GetMapping("/images/{fileName}")
    public ResponseEntity<Resource> serveImage(@PathVariable String fileName) {
        if (checkAuthorization() != null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Long userId = getCurrentUser().getUser().getId();

        try {
            Resource imageResource = fileService.serveImage(userId, fileName);
            return ResponseEntity.ok().body(imageResource);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
