package ru.clevertec.house.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.github.sakujj.cache.IdentifiableByUUID;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import ru.clevertec.house.constant.FormatConstants;
import ru.clevertec.house.documentation.OpenApiSchema;
import ru.clevertec.house.enumeration.Sex;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PersonResponse implements IdentifiableByUUID {
    @Schema(pattern = OpenApiSchema.Examples.PersonDTO.UUID_EXAMPLE)
    private UUID uuid;

    @Schema(description = OpenApiSchema.Descriptions.NOT_BLANK_DESCRIPTION,
            example = OpenApiSchema.Examples.PersonDTO.NAME_EXAMPLE)
    private String name;

    @Schema(description = OpenApiSchema.Descriptions.NOT_BLANK_DESCRIPTION,
            example = OpenApiSchema.Examples.PersonDTO.SURNAME_EXAMPLE)
    private String surname;

    @Schema(example = OpenApiSchema.Examples.PersonDTO.SEX_EXAMPLE)
    private Sex sex;

    @Schema(pattern = OpenApiSchema.Patterns.PersonDTO.PASSPORT_SERIES_PATTERN,
            example = OpenApiSchema.Examples.PersonDTO.PASSPORT_SERIES_EXAMPLE)
    private String passportSeries;

    @Schema(pattern = OpenApiSchema.Patterns.PersonDTO.PASSPORT_NUMBER_PATTERN,
            example = OpenApiSchema.Examples.PersonDTO.PASSPORT_NUMBER_EXAMPLE)
    private String passportNumber;

    @Schema(description = "must exist",
            example = OpenApiSchema.Examples.PersonDTO.HOUSE_OF_RESIDENCE_UUID_EXAMPLE)
    private UUID houseOfResidenceUUID;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = FormatConstants.DATE_TIME_FORMAT)
    @Schema(pattern = OpenApiSchema.Patterns.DATE_TIME_FORMAT)
    private LocalDateTime createDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = FormatConstants.DATE_TIME_FORMAT)
    @Schema(pattern = OpenApiSchema.Patterns.DATE_TIME_FORMAT)
    private LocalDateTime updateDate;
}

