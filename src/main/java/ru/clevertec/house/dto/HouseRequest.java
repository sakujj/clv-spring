package ru.clevertec.house.dto;

import lombok.Data;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class HouseRequest {
    private Integer area;
    private String country;
    private String city;
    private String street;
    private Integer number;
}
