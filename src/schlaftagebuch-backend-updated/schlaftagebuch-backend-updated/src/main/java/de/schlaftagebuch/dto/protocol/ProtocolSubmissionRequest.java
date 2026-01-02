package de.schlaftagebuch.dto.protocol;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.time.Instant;
import java.util.List;

public class ProtocolSubmissionRequest {

    @NotBlank
    public String templateKey;   // "prototype_1_v1"

    @NotBlank
    public String locale;        // "de" | "en"

    public Instant filledAt;     // optional, если null — ставим now()

    @NotEmpty
    public List<AnswerDto> answers;

    public static class AnswerDto {
        public int id;
        public String result;    // как в вашем фронте
    }
}
