package ru.clevertec.house.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.clevertec.house.documentation.OpenApiSchema;
import ru.clevertec.house.enumeration.Sex;
import ru.clevertec.house.validator.person.HouseOfResidenceIsPresent;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonRequest {

    @NotBlank
    @Schema(description = OpenApiSchema.Descriptions.NOT_BLANK_DESCRIPTION,
            example = OpenApiSchema.Examples.PersonDTO.NAME_EXAMPLE)
    private String name;

    @NotBlank
    @Schema(description = OpenApiSchema.Descriptions.NOT_BLANK_DESCRIPTION,
            example = OpenApiSchema.Examples.PersonDTO.SURNAME_EXAMPLE)
    private String surname;

    @NotNull
    @Schema(example = OpenApiSchema.Examples.PersonDTO.SEX_EXAMPLE)
    private Sex sex;

    @NotNull
    @Pattern(regexp = Constraints.Patterns.PersonDTO.PASSPORT_SERIES_PATTERN)
    @Schema(pattern = OpenApiSchema.Patterns.PersonDTO.PASSPORT_SERIES_PATTERN,
            example = OpenApiSchema.Examples.PersonDTO.PASSPORT_SERIES_EXAMPLE)
    private String passportSeries;

    @NotBlank
    @Pattern(regexp = Constraints.Patterns.PersonDTO.PASSPORT_NUMBER_PATTERN)
    @Schema(pattern = OpenApiSchema.Patterns.PersonDTO.PASSPORT_NUMBER_PATTERN,
            example = OpenApiSchema.Examples.PersonDTO.PASSPORT_NUMBER_EXAMPLE)
    private String passportNumber;

    @NotNull
    @HouseOfResidenceIsPresent
    @Schema(description = "must exist",
            example = OpenApiSchema.Examples.PersonDTO.HOUSE_OF_RESIDENCE_UUID_EXAMPLE)
    private UUID houseOfResidenceUUID;
}
