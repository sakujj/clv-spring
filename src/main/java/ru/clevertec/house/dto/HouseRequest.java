package ru.clevertec.house.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;
import ru.clevertec.house.documentation.OpenApiSchema;

@Data
@Builder
public class HouseRequest {

    @NotNull
    @Positive
    @Schema(exclusiveMinimum = true,
            minimum = OpenApiSchema.Minimums.HouseDTO.AREA_EXCLUSIVE_MINIMUM,
            example = OpenApiSchema.Examples.HouseDTO.AREA_EXAMPLE)
    private Double area;

    @NotBlank
    @Schema(description = OpenApiSchema.Descriptions.NOT_BLANK_DESCRIPTION,
            example = OpenApiSchema.Examples.HouseDTO.COUNTRY_EXAMPLE)
    private String country;

    @NotBlank
    @Schema(description = OpenApiSchema.Descriptions.NOT_BLANK_DESCRIPTION,
            example = OpenApiSchema.Examples.HouseDTO.CITY_EXAMPLE)
    private String city;

    @NotBlank
    @Schema(description = OpenApiSchema.Descriptions.NOT_BLANK_DESCRIPTION,
            example = OpenApiSchema.Examples.HouseDTO.STREET_EXAMPLE)
    private String street;

    @NotNull
    @Positive
    @Schema(exclusiveMinimum = true,
            minimum = OpenApiSchema.Minimums.HouseDTO.NUMBER_EXCLUSIVE_MINIMUM,
            example = OpenApiSchema.Examples.HouseDTO.NUMBER_EXAMPLE)
    private Integer number;
}
