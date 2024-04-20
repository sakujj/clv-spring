package ru.clevertec.house.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.clevertec.house.dto.PersonRequest;
import ru.clevertec.house.dto.PersonResponse;
import ru.clevertec.house.entity.Person;
import ru.clevertec.house.test.util.PersonTestBuilder;

import static org.assertj.core.api.Assertions.assertThat;

class PersonMapperTests {

    private final PersonMapper personMapper = Mappers.getMapper(PersonMapper.class);

    @Test
    void shouldMapToResponse() {
        // given
        Person person = PersonTestBuilder.aPerson().build();
        PersonResponse expected = PersonTestBuilder.aPerson().buildResponse();

        // when
        PersonResponse actual = personMapper.toResponse(person);

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldMapFromRequest() {
        // given
        PersonRequest request = PersonTestBuilder.aPerson().buildRequest();
        Person expected = PersonTestBuilder.aPerson()
                .withId(null)
                .withUuid(null)
                .withUpdateDate(null)
                .withCreateDate(null)
                .build();

        // when
        Person actual = personMapper.fromRequest(request);

        // then
        assertThat(actual).isEqualTo(expected);
    }
}
