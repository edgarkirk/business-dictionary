package com.epam.businessdictionary.infrastructure;

import com.epam.businessdictionary.domain.DictionaryEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DictionaryRepository extends JpaRepository<DictionaryEntry, UUID> {

    Optional<DictionaryEntry> findByNormalizedTerm(String normalizedTerm);
}
