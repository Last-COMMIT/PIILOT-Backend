package com.lastcommit.piilot.domain.document.repository;

import com.lastcommit.piilot.domain.document.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByUserIdOrderByCreatedAtDesc(Long userId);
}
