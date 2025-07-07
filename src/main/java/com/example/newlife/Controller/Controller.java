package com.example.newlife.Controller;

import com.example.newlife.Entity.PageContent;
import com.example.newlife.Entity.TestEntity;
import com.example.newlife.Repository.PageContentRepository;
import com.example.newlife.Repository.TestEntityRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/app")
public class Controller {


    private final TestEntityRepository repo;

    private final PageContentRepository repository;

    @Value("${upload.dir:uploads/}")
    private String uploadDir;

    public Controller(PageContentRepository repository, TestEntityRepository repo, TestEntityRepository repo1) {
        this.repository = repository;
        this.repo = repo1;

    }

    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        // Save a test row to the database
        TestEntity test = new TestEntity("hello my people wher you at");
        repo.save(test);

        // Fetch it back
        String result = repo.findById(test.getId())
                .map(TestEntity::getMessage)
                .orElse("not found");

        return ResponseEntity.ok(result);
    }

    // Save content (with image URL - either local or external)
    // ✅ CREATE
    @PostMapping
    public ResponseEntity<PageContent> createContent(
            @RequestParam String page,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam(required = false) MultipartFile file,
            @RequestParam(required = false) String img
    ) {
        try {
            String imageUrl = handleFileOrUrl(file, img);
            PageContent content = PageContent.builder()
                    .page(page)
                    .title(title)
                    .description(description)
                    .img(imageUrl)
                    .build();

            return ResponseEntity.ok(repository.save(content));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ READ ALL
    @GetMapping
    public List<PageContent> getAll() {
        return repository.findAll();
    }

    // ✅ READ BY ID
    @GetMapping("/{id}")
    public ResponseEntity<PageContent> getById(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<PageContent> updateContent(
            @PathVariable Long id,
            @RequestParam String page,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam(required = false) MultipartFile file,
            @RequestParam(required = false) String img
    ) {
        Optional<PageContent> optional = repository.findById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            PageContent existing = optional.get();

            if (file != null && existing.getImg() != null && existing.getImg().contains("/uploads/")) {
                deleteImageFile(existing.getImg());
            }

            String imageUrl = handleFileOrUrl(file, img);

            existing.setPage(page);
            existing.setTitle(title);
            existing.setDescription(description);
            existing.setImg(imageUrl);

            PageContent updated = repository.save(existing);
            return ResponseEntity.ok(updated);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    // ✅ DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteContent(@PathVariable Long id) {
        return repository.findById(id).map(existing -> {
            if (existing.getImg() != null && existing.getImg().contains("/uploads/")) {
                deleteImageFile(existing.getImg());
            }
            repository.delete(existing);
            return ResponseEntity.noContent().build();
        }).orElse(ResponseEntity.notFound().build());
    }

    // 🔧 Utility: Save file or return image URL
    private String handleFileOrUrl(MultipartFile file, String img) throws IOException {
        if (file != null && !file.isEmpty()) {
            String ext = getFileExtension(file.getOriginalFilename());
            String filename = UUID.randomUUID() + "." + ext;

            Path path = Paths.get(uploadDir + filename);
            Files.createDirectories(path.getParent());
            Files.write(path, file.getBytes());

            return "https://newlifecleaning.onrender.com/uploads/" + filename;
        } else {
            return img; // External image URL
        }
    }

    // 🔧 Utility: Extract file extension
    private String getFileExtension(String filename) {
        if (filename == null) return "jpg";
        int dot = filename.lastIndexOf(".");
        return (dot == -1) ? "jpg" : filename.substring(dot + 1);
    }

    // 🔧 Utility: Delete uploaded image
    private void deleteImageFile(String imageUrl) {
        try {
            String filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            Path path = Paths.get(uploadDir + filename);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            System.err.println("Could not delete image file: " + e.getMessage());
        }
    }
}
