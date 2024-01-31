package ru.clevertec.house.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import ru.clevertec.house.constant.FormatConstants;
import ru.clevertec.house.documentation.OpenApiSchema;
import ru.clevertec.house.entity.IdentifiableByUUID;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class HouseResponse implements IdentifiableByUUID {
    @Schema(example = OpenApiSchema.Examples.HouseDTO.UUID_EXAMPLE)
    private UUID uuid;

    @Schema(exclusiveMinimum = true,
            minimum = OpenApiSchema.Minimums.HouseDTO.AREA_EXCLUSIVE_MINIMUM,
            example = OpenApiSchema.Examples.HouseDTO.AREA_EXAMPLE)
    private Double area;

    @Schema(description = OpenApiSchema.Descriptions.NOT_BLANK_DESCRIPTION,
            example = OpenApiSchema.Examples.HouseDTO.COUNTRY_EXAMPLE)
    private String country;

    @Schema(description = OpenApiSchema.Descriptions.NOT_BLANK_DESCRIPTION,
            example = OpenApiSchema.Examples.HouseDTO.CITY_EXAMPLE)
    private String city;

    @Schema(description = OpenApiSchema.Descriptions.NOT_BLANK_DESCRIPTION,
            example = OpenApiSchema.Examples.HouseDTO.STREET_EXAMPLE)
    private String street;

    @Schema(exclusiveMinimum = true,
            minimum = OpenApiSchema.Minimums.HouseDTO.NUMBER_EXCLUSIVE_MINIMUM,
            example = OpenApiSchema.Examples.HouseDTO.NUMBER_EXAMPLE)
    private Integer number;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = FormatConstants.DATE_TIME_FORMAT)
    @Schema(pattern = OpenApiSchema.Patterns.DATE_TIME_FORMAT)
    private LocalDateTime createDate;
}
