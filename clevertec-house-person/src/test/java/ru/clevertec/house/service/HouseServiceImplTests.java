package ru.clevertec.house.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.clevertec.house.dto.HouseRequest;
import ru.clevertec.house.dto.HouseResponse;
import ru.clevertec.house.entity.House;
import ru.clevertec.house.mapper.HouseMapper;
import ru.clevertec.house.repository.HouseRepository;
import ru.clevertec.house.test.util.HouseTestBuilder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HouseServiceImplTests {

    @Mock
    private HouseRepository houseRepository;

    @Mock
    private HouseMapper houseMapper;

    @InjectMocks
    private HouseServiceImpl houseServiceImpl;

    @Test
    public void shouldFindHouseById_whenRepositoryFoundById() {
        // given
        UUID uuidToFindBy = HouseTestBuilder.aHouse().getUuid();
        House expectedFromRepo = HouseTestBuilder.aHouse().build();
        HouseResponse expected = HouseTestBuilder.aHouse().buildResponse();

        when(houseRepository.findByUuid(uuidToFindBy))
                .thenReturn(Optional.of(expectedFromRepo));
        when(houseMapper.toResponse(expectedFromRepo))
                .thenReturn(expected);

        // when
        Optional<HouseResponse> actual = houseServiceImpl.findByUUID(uuidToFindBy);

        // then
        assertThat(actual).isPresent();
        assertThat(actual.get()).isEqualTo(expected);
    }

    @Test
    public void shouldNotFindHouseById_whenRepositoryDidNotFindById() {
        // given
        UUID uuidToFindBy = HouseTestBuilder.aHouse().getUuid();

        when(houseRepository.findByUuid(uuidToFindBy))
                .thenReturn(Optional.empty());

        // when
        Optional<HouseResponse> actual = houseServiceImpl.findByUUID(uuidToFindBy);

        // then
        assertThat(actual).isEmpty();
    }

    @Test
    public void shouldFindAll() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        List<House> expectedContentFromRepo = List.of(
                HouseTestBuilder.aHouse()
                        .build(),
                HouseTestBuilder.aHouse()
                        .withUuid(UUID.fromString("3e020b18-18dd-4cd5-94d5-37ac24482d70"))
                        .build(),
                HouseTestBuilder.aHouse()
                        .withUuid(UUID.fromString("77bcde0b-069d-41db-beab-a7e6580c1a24"))
                        .build()
        );

        List<HouseResponse> expectedContent = expectedContentFromRepo.stream()
                .map(house -> HouseTestBuilder.aHouse()
                        .withUuid(house.getUuid())
                        .buildResponse())
                .toList();

        IntStream.range(0, 3).forEach(i ->
                when(houseMapper.toResponse(expectedContentFromRepo.get(i))).
                        thenReturn(expectedContent.get(i)));

        Page<House> expectedFromRepo = new PageImpl<>(expectedContentFromRepo);
        when(houseRepository.findAll(pageable))
                .thenReturn(expectedFromRepo);

        // when
        Page<HouseResponse> actual = houseServiceImpl.findAll(pageable);

        // then
        assertThat(actual.getNumberOfElements()).isPositive();
        assertThat(actual.getContent()).isEqualTo(expectedContent);
    }

    @Test
    public void shouldCreate() {
        // given
        HouseRequest personRequestToCreate = HouseTestBuilder.aHouse().buildRequest();
        House personFromRequest = HouseTestBuilder.aHouse().build();
        HouseResponse expected = HouseTestBuilder.aHouse().buildResponse();

        when(houseMapper.fromRequest(personRequestToCreate))
                .thenReturn(personFromRequest);
        when(houseRepository.save(personFromRequest))
                .thenReturn(personFromRequest);
        when(houseMapper.toResponse(personFromRequest))
                .thenReturn(expected);

        // when
        HouseResponse actual = houseServiceImpl.create(personRequestToCreate);

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldUpdate() {
        // given
        HouseTestBuilder houseFromRequestBuilder = HouseTestBuilder.aHouse()
                .withCity("NEW CITY")
                .withCountry("NEW COUNTRY")
                .withStreet("NEW STREET")
                .withArea(9999.9999)
                .withNumber(666);

        HouseRequest houseToUpdateRequest = houseFromRequestBuilder.buildRequest();
        UUID uuidOfHouseToBeUpdated = houseFromRequestBuilder.getUuid();
        House houseFromRequest = houseFromRequestBuilder.build();

        HouseTestBuilder existingHouseBuilder = HouseTestBuilder.aHouse();
        House existingHouse = existingHouseBuilder.build();
        House existingHouseWithUpdatedFields = existingHouseBuilder
                .withCity(houseFromRequest.getCity())
                .withCountry(houseFromRequest.getCountry())
                .withStreet(houseFromRequest.getStreet())
                .withArea(houseFromRequest.getArea())
                .withNumber(houseFromRequest.getNumber())
                .build();

        HouseTestBuilder expectedPersonBuilder = HouseTestBuilder.aHouse()
                .withCity(houseFromRequest.getCity())
                .withCountry(houseFromRequest.getCountry())
                .withStreet(houseFromRequest.getStreet())
                .withArea(houseFromRequest.getArea())
                .withNumber(houseFromRequest.getNumber());
        House expectedFromSave = expectedPersonBuilder.build();
        HouseResponse expected = expectedPersonBuilder.buildResponse();

        when(houseRepository.findByUuid(uuidOfHouseToBeUpdated))
                .thenReturn(Optional.of(existingHouse));
        when(houseMapper.fromRequest(houseToUpdateRequest))
                .thenReturn(houseFromRequest);
        when(houseRepository.save(existingHouseWithUpdatedFields))
                .thenReturn(expectedFromSave);
        when(houseMapper.toResponse(expectedFromSave))
                .thenReturn(expected);

        // when

        Optional<HouseResponse> actual = houseServiceImpl.update(houseToUpdateRequest, uuidOfHouseToBeUpdated);

        // then
        assertThat(actual).isPresent();
        assertThat(actual.get()).isEqualTo(expected);
    }

    @Test
    void shouldNotUpdate_whenNotFoundInRepository() {
        // given
        HouseRequest houseRequest = HouseTestBuilder.aHouse().buildRequest();
        UUID uuid = HouseTestBuilder.aHouse().getUuid();


        when(houseRepository.findByUuid(any(UUID.class)))
                .thenReturn(Optional.empty());

        // when
        Optional<HouseResponse> actual = houseServiceImpl.update(houseRequest, uuid);

        // then
        assertThat(actual).isEmpty();
    }

    @Test
    void shouldDelete() {
        // given
        long expected = 666L;
        UUID uuidToDeleteBy = HouseTestBuilder.aHouse().getUuid();
        when(houseRepository.deleteByUuid(uuidToDeleteBy))
                .thenReturn(expected);

        // when
        long actual = houseServiceImpl.deleteByUUID(uuidToDeleteBy);

        // then
        assertThat(actual).isEqualTo(expected);
    }

}
