package com.epam.businessdictionary.persistence;

import com.epam.businessdictionary.domain.BusinessDictionaryEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BusinessDictionaryRepository extends JpaRepository<BusinessDictionaryEntry, UUID> {

    Optional<BusinessDictionaryEntry> findByNormalizedTerm(String normalizedTerm);
}
