package ru.clevertec.house.test.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.With;
import ru.clevertec.house.dto.HouseRequest;
import ru.clevertec.house.dto.HouseResponse;
import ru.clevertec.house.entity.House;
import ru.clevertec.house.entity.Person;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
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

    private Set<Person> owners = null;

    private List<Person> residents = null;

    private LocalDateTime createDate = LocalDateTime.MIN;

    @Override
    public House build() {
        return new House(id, uuid, area, country, city, street, number, owners, residents, createDate);
    }

    public HouseResponse buildResponse() {
        return HouseResponse.builder()
                .uuid(uuid)
                .area(area)
                .city(city)
                .street(street)
                .country(country)
                .number(number)
                .createDate(createDate)
                .build();
    }

    public HouseRequest buildRequest() {
        return HouseRequest.builder()
                .area(area)
                .city(city)
                .street(street)
                .country(country)
                .number(number)
                .build();
    }
}
