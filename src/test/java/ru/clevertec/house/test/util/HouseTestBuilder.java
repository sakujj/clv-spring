package ru.clevertec.house.test.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.With;
import ru.clevertec.house.entity.House;
import ru.clevertec.house.entity.Person;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@With
@Getter
@NoArgsConstructor(staticName = "aHouse")
@AllArgsConstructor
public class HouseTestBuilder implements TestBuilder<House> {

    private Long id = 666L;

    private UUID uuid = UUID.fromString("c28c0367-f754-4be6-b53b-4586ea361503");
    private Double area = 1111.11;
    private String country = "Belarus";
    private String city = "Minsk";
    private String street = "Sovetskaya";
    private Integer number = 44;

    private List<Person> owners = null;

    private List<Person> residents = null;

    private LocalDateTime createDate = LocalDateTime.MIN;

    @Override
    public House build() {
        return new House(id, uuid, area, country, city, street, number, owners, residents, createDate);
    }
}
