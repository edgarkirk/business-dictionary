package com.epam.businessdictionary.infrastructure;

import com.epam.businessdictionary.domain.DictionaryEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DictionaryRepository extends JpaRepository<DictionaryEntry, UUID> {

    Optional<DictionaryEntry> findByNormalizedTerm(String normalizedTerm);
}
