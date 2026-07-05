package com.github.hamzaelalaouiismaili.chari.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Common Fatourati business result returned independently of HTTP status. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChariFatouratiResponse {
    private String codeRetour;
    private String msg;

    @JsonIgnore
    public boolean isSuccessful() {
        return "000".equals(codeRetour);
    }

    @JsonIgnore
    public boolean hasCode(String code) {
        return code != null && code.equals(codeRetour);
    }

    @JsonIgnore
    public boolean isTechnicalError() {
        return hasCode("902") || hasCode("908") || hasCode("909")
                || hasCode("910") || hasCode("911");
    }
}
