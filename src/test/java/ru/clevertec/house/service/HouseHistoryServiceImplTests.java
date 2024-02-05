package ru.clevertec.house.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.clevertec.house.dto.HouseResponse;
import ru.clevertec.house.dto.PersonResponse;
import ru.clevertec.house.entity.House;
import ru.clevertec.house.entity.HouseHistory;
import ru.clevertec.house.entity.Person;
import ru.clevertec.house.enumeration.PersonType;
import ru.clevertec.house.mapper.HouseMapper;
import ru.clevertec.house.mapper.PersonMapper;
import ru.clevertec.house.repository.HouseHistoryRepository;
import ru.clevertec.house.test.util.HouseTestBuilder;
import ru.clevertec.house.test.util.PersonTestBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.clevertec.house.constant.ControllerConstants.FIRST_PAGE_NUMBER;
import static ru.clevertec.house.constant.ControllerConstants.MAX_PAGE_SIZE;

@ExtendWith(MockitoExtension.class)
public class HouseHistoryServiceImplTests {

    @Mock
    private PersonMapper personMapper;

    @Mock
    private HouseMapper houseMapper;

    @Mock
    private HouseHistoryRepository houseHistoryRepository;

    @InjectMocks
    private HouseHistoryServiceImpl houseHistoryService;

    @Test
    void shouldFindAllHousesWherePersonLivedByPersonUuid() {
        // given
        UUID personUUID = UUID.fromString("d06dbab0-a05d-4036-b66a-3161610eea0f");
        PageRequest pageRequest = PageRequest.of(FIRST_PAGE_NUMBER, MAX_PAGE_SIZE);

        Person person = PersonTestBuilder.aPerson().build();
        House house = HouseTestBuilder.aHouse().build();

        HouseHistory expectedContent = HouseHistory.builder()
                .person(person)
                .house(house)
                .date(LocalDateTime.MIN)
                .id(1L)
                .build();

        HouseResponse expectedByIndexZero = HouseTestBuilder.aHouse().buildResponse();

        when(houseMapper.toResponse(house))
                .thenReturn(expectedByIndexZero);
        when(houseHistoryRepository.findAllByPersonUuidAndType(personUUID, PersonType.TENANT, pageRequest))
                .thenReturn(new PageImpl<>(List.of(expectedContent)));

        // when
        Page<HouseResponse> actual = houseHistoryService.findAllHousesWherePersonLivedByPersonUuid(personUUID, pageRequest);

        // then
        assertThat(actual.getContent().get(0)).isEqualTo(expectedByIndexZero);
        verify(houseHistoryRepository).findAllByPersonUuidAndType(personUUID, PersonType.TENANT, pageRequest);
    }

    @Test
    void shouldFindAllHousesWhichPersonOwnedByPersonUuid() {
        // given
        UUID personUUID = UUID.fromString("d06dbab0-a05d-4036-b66a-3161610eea0f");
        PageRequest pageRequest = PageRequest.of(FIRST_PAGE_NUMBER, MAX_PAGE_SIZE);

        Person person = PersonTestBuilder.aPerson().build();
        House house = HouseTestBuilder.aHouse().build();

        HouseHistory expectedContent = HouseHistory.builder()
                .person(person)
                .house(house)
                .date(LocalDateTime.MIN)
                .id(1L)
                .build();

        HouseResponse expectedByIndexZero = HouseTestBuilder.aHouse().buildResponse();

        when(houseMapper.toResponse(house))
                .thenReturn(expectedByIndexZero);
        when(houseHistoryRepository.findAllByPersonUuidAndType(personUUID, PersonType.OWNER, pageRequest))
                .thenReturn(new PageImpl<>(List.of(expectedContent)));

        // when
        Page<HouseResponse> actual = houseHistoryService.findAllHousesWhichPersonOwnedByPersonUuid(personUUID, pageRequest);

        // then
        assertThat(actual.getContent().get(0)).isEqualTo(expectedByIndexZero);
        verify(houseHistoryRepository).findAllByPersonUuidAndType(personUUID, PersonType.OWNER, pageRequest);
    }


    @Test
    void shouldFindAllPeopleThatLivedInHouseByHouseUuid() {
        // given
        UUID houseUUID = UUID.fromString("d06dbab0-a05d-4036-b66a-3161610eea0f");
        PageRequest pageRequest = PageRequest.of(FIRST_PAGE_NUMBER, MAX_PAGE_SIZE);

        Person person = PersonTestBuilder.aPerson().build();
        House house = HouseTestBuilder.aHouse().build();

        HouseHistory expectedContent = HouseHistory.builder()
                .person(person)
                .house(house)
                .date(LocalDateTime.MIN)
                .id(1L)
                .build();

        PersonResponse expectedByIndexZero = PersonTestBuilder.aPerson().buildResponse();

        when(personMapper.toResponse(person))
                .thenReturn(expectedByIndexZero);
        when(houseHistoryRepository.findAllByHouseUuidAndType(houseUUID, PersonType.TENANT, pageRequest))
                .thenReturn(new PageImpl<>(List.of(expectedContent)));

        // when
        Page<PersonResponse> actual = houseHistoryService.findAllPeopleThatLivedInHouseByHouseUuid(houseUUID, pageRequest);

        // then
        assertThat(actual.getContent().get(0)).isEqualTo(expectedByIndexZero);
        verify(houseHistoryRepository).findAllByHouseUuidAndType(houseUUID, PersonType.TENANT, pageRequest);
    }

    @Test
    void shouldFindAllPeopleThatOwnedHouseByHouseUuid() {
        // given
        UUID houseUUID = UUID.fromString("d06dbab0-a05d-4036-b66a-3161610eea0f");
        PageRequest pageRequest = PageRequest.of(FIRST_PAGE_NUMBER, MAX_PAGE_SIZE);

        Person person = PersonTestBuilder.aPerson().build();
        House house = HouseTestBuilder.aHouse().build();

        HouseHistory expectedContent = HouseHistory.builder()
                .person(person)
                .house(house)
                .date(LocalDateTime.MIN)
                .id(1L)
                .build();

        PersonResponse expectedByIndexZero = PersonTestBuilder.aPerson().buildResponse();

        when(personMapper.toResponse(person))
                .thenReturn(expectedByIndexZero);
        when(houseHistoryRepository.findAllByHouseUuidAndType(houseUUID, PersonType.OWNER, pageRequest))
                .thenReturn(new PageImpl<>(List.of(expectedContent)));

        // when
        Page<PersonResponse> actual = houseHistoryService.findAllPeopleThatOwnedHouseByHouseUuid(houseUUID, pageRequest);

        // then
        assertThat(actual.getContent().get(0)).isEqualTo(expectedByIndexZero);
        verify(houseHistoryRepository).findAllByHouseUuidAndType(houseUUID, PersonType.OWNER, pageRequest);
    }
}
