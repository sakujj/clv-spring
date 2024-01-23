package ru.clevertec.house.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import ru.clevertec.house.constant.ApplicationConstants;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class HouseResponse {
    private UUID uuid;
    private Double area;
    private String country;
    private String city;
    private String street;
    private Integer number;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ApplicationConstants.DATE_TIME_FORMAT)
    private LocalDateTime createDate;
}
