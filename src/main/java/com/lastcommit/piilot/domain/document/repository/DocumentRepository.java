package com.lastcommit.piilot.domain.document.repository;

import com.lastcommit.piilot.domain.document.entity.Document;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    @Query("SELECT d FROM Document d JOIN FETCH d.user ORDER BY d.createdAt DESC")
    Slice<Document> findAllWithUserOrderByCreatedAtDesc(Pageable pageable);
}
