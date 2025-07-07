package com.example.newlife.Entity;



import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String page; // e.g., "Blog Posts", "Laundry Services"

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String img;
}

