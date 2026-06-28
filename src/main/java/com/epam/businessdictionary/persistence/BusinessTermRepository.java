package com.epam.businessdictionary.persistence;

import com.epam.businessdictionary.domain.BusinessTerm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BusinessTermRepository extends JpaRepository<BusinessTerm, UUID> {

    Optional<BusinessTerm> findByNormalizedTerm(String normalizedTerm);
}
