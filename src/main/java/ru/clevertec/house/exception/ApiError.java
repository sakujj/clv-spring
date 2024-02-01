package ru.clevertec.house.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiError {
    private String errorMessage;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "The field is present only if something is invalid.", nullable = true)
    private List<String> validationErrors;


    public static final class Fields {
        public static final String errorMessage = "errorMessage";
        public static final String validationErrors = "validationErrors";
    }
}
