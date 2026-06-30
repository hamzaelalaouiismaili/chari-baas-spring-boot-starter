package com.github.hamzaelalaouiismaili.chari.model.webhook;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;

/**
 * Lenient {@link Instant} deserializer for Chari webhook timestamps.
 * <p>
 * Chari is inconsistent about timezone information: {@code ExecutedAt} arrives
 * as a UTC instant ({@code 2026-06-30T23:02:13.3321866Z}) while {@code CreatedAt}
 * arrives with no offset at all ({@code 2026-06-30T22:02:01.127087}). The default
 * Jackson {@code Instant} deserializer rejects the offset-less form, which fails
 * the whole payload. This deserializer accepts both, treating an offset-less
 * value as UTC, and yields {@code null} for blank/unparseable input rather than
 * throwing.
 */
public class LenientInstantDeserializer extends JsonDeserializer<Instant> {

    @Override
    public Instant deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String value = parser.getValueAsString();
        if (value == null || value.isBlank()) {
            return null;
        }
        value = value.trim();
        try {
            return OffsetDateTime.parse(value).toInstant();
        } catch (DateTimeParseException ignored) {
            // not an offset/Z form
        }
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException ignored) {
            // not an ISO-8601 instant
        }
        try {
            return LocalDateTime.parse(value).toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException ignored) {
            // not a local date-time either
        }
        return null;
    }
}
