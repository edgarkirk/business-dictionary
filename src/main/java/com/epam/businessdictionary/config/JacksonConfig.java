package com.epam.businessdictionary.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Configuration
class JacksonConfig {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC);

    /**
     * Serializes {@link Instant} truncated to milliseconds so that responses are consistent
     * with the precision stored by the database (PostgreSQL/H2 both store TIMESTAMP with at most
     * microsecond precision, but may round vs. truncate differently). Millisecond precision ensures
     * the in-memory value set by JPA auditing and the DB-read value round-trip identically,
     * avoiding off-by-one-microsecond discrepancies between create and subsequent read responses.
     */
    @Bean
    Jackson2ObjectMapperBuilderCustomizer instantMillisecondPrecisionCustomizer() {
        return builder -> builder.serializerByType(Instant.class, new JsonSerializer<Instant>() {
            @Override
            public void serialize(Instant value, JsonGenerator gen, SerializerProvider serializers)
                    throws IOException {
                gen.writeString(FORMATTER.format(value.truncatedTo(ChronoUnit.MILLIS)));
            }
        });
    }
}
