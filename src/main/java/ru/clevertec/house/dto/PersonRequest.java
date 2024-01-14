package ru.clevertec.house.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonRequest {
    private String name;
    private String surname;
    private String sex;
    private String passportSeries;
    private String passportNumber;
    private UUID houseOfResidenceUUID;
}
