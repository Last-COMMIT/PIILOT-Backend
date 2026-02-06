package com.lastcommit.piilot.domain.shared.repository;

import com.lastcommit.piilot.domain.shared.PiiCategory;
import com.lastcommit.piilot.domain.shared.PiiType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PiiTypeRepository extends JpaRepository<PiiType, Integer> {

    Optional<PiiType> findByType(PiiCategory type);
}
