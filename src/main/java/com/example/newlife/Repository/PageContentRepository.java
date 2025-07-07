package com.example.newlife.Repository;


import com.example.newlife.Entity.PageContent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PageContentRepository extends JpaRepository<PageContent, Long> {
    List<PageContent> findByPage(String page);
}

