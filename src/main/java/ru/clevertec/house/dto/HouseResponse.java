package ru.clevertec.house.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import ru.clevertec.house.constant.FormatConstants;
import ru.clevertec.house.entity.IdentifiableByUUID;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class HouseResponse implements IdentifiableByUUID {
    private UUID uuid;
    private Double area;
    private String country;
    private String city;
    private String street;
    private Integer number;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = FormatConstants.DATE_TIME_FORMAT)
    private LocalDateTime createDate;
}
