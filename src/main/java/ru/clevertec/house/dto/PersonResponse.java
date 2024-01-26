package ru.clevertec.house.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import ru.clevertec.house.constant.FormatConstants;
import ru.clevertec.house.entity.IdentifiableByUUID;
import ru.clevertec.house.enumeration.Sex;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PersonResponse implements IdentifiableByUUID {
    private UUID uuid;
    private String name;
    private String surname;
    private Sex sex;
    private String passportSeries;
    private String passportNumber;
    private UUID houseOfResidenceUUID;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = FormatConstants.DATE_TIME_FORMAT)
    private LocalDateTime createDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = FormatConstants.DATE_TIME_FORMAT)
    private LocalDateTime updateDate;
}

