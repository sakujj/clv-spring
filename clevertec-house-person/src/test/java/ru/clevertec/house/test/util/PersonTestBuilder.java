package ru.clevertec.house.test.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.With;
import ru.clevertec.house.dto.PersonRequest;
import ru.clevertec.house.dto.PersonResponse;
import ru.clevertec.house.entity.House;
import ru.clevertec.house.entity.Person;
import ru.clevertec.house.enumeration.Sex;

import java.time.LocalDateTime;
import java.util.Set;
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
    private Sex sex = Sex.MALE;

    private String passportSeries = "LP";

    private String passportNumber = "1234567890000";

    private LocalDateTime createDate = LocalDateTime.MIN;

    private LocalDateTime updateDate = LocalDateTime.MIN;

    private House houseOfResidence = House.builder()
            .uuid(UUID.fromString("9dffe39f-1926-4139-9db2-ad102429b55b"))
            .build();

    private Set<House> ownedHouses = null;

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
