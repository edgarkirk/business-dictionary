package com.epam.businessdictionary.persistence;

import com.epam.businessdictionary.domain.BusinessDictionaryTerm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BusinessDictionaryTermRepository extends JpaRepository<BusinessDictionaryTerm, UUID> {

    Optional<BusinessDictionaryTerm> findByNormalizedTerm(String normalizedTerm);
}
