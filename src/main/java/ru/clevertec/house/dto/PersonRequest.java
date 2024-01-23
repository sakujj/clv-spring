package ru.clevertec.house.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import ru.clevertec.house.enumeration.Sex;
import ru.clevertec.house.validator.person.HouseOfResidenceIsPresent;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String surname;

    @NotNull
    private Sex sex;

    @NotNull
    @Pattern(regexp = "[A-Z]{2}")
    private String passportSeries;

    @NotBlank
    @Pattern(regexp = "[0-9]{13}")
    private String passportNumber;

    @NotNull
    @HouseOfResidenceIsPresent
    private UUID houseOfResidenceUUID;
}
