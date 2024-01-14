package ru.clevertec.house.test.util;

import jakarta.persistence.*;
import lombok.*;
import ru.clevertec.house.dto.PersonRequest;
import ru.clevertec.house.dto.PersonResponse;
import ru.clevertec.house.model.House;
import ru.clevertec.house.model.Person;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@With
@Getter
@NoArgsConstructor(staticName = "aPerson")
@AllArgsConstructor
public class PersonTestBuilder implements TestBuilder<Person> {
    private Long id = 666L;
    private UUID uuid = UUID.fromString("7d031492-daad-4b76-83d5-9515d46328f9");
    private String name = "Pablo";
    private String surname = "Fernandez";
    private String sex = "M";

    private String passportSeries = "LP";

    private String passportNumber = "1234567890000";

    private LocalDateTime createDate = LocalDateTime.MIN;

    private LocalDateTime updateDate = LocalDateTime.MIN;

    private House houseOfResidence = House.builder()
            .uuid(UUID.fromString("9dffe39f-1926-4139-9db2-ad102429b55b"))
            .build();

    private List<House> ownedHouses = null;

    public PersonResponse buildResponse() {
        return PersonResponse.builder()
                .uuid(uuid)
                .createDate(createDate)
                .updateDate(updateDate)
                .sex(sex)
                .name(name)
                .surname(surname)
                .passportNumber(passportNumber)
                .passportSeries(passportSeries)
                .houseOfResidenceUUID(houseOfResidence.getUuid())
                .build();
    }

    public PersonRequest buildRequest() {
        return PersonRequest.builder()
                .sex(sex)
                .name(name)
                .surname(surname)
                .passportNumber(passportNumber)
                .passportSeries(passportSeries)
                .houseOfResidenceUUID(houseOfResidence.getUuid())
                .build();
    }

    @Override
    public Person build() {
        return new Person(id, uuid, name, surname, sex, passportSeries, passportNumber, createDate, updateDate,
                houseOfResidence, ownedHouses);
    }
}
