package ru.clevertec.house.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.clevertec.house.dto.HouseResponse;
import ru.clevertec.house.dto.PersonResponse;
import ru.clevertec.house.entity.HouseHistory;
import ru.clevertec.house.enumeration.PersonType;
import ru.clevertec.house.mapper.HouseMapper;
import ru.clevertec.house.mapper.PersonMapper;
import ru.clevertec.house.repository.HouseHistoryRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HouseHistoryServiceImpl implements HouseHistoryService {

    private final HouseHistoryRepository houseHistoryRepository;
    private final HouseMapper houseMapper;
    private final PersonMapper personMapper;

    @Override
    public Page<HouseResponse> findAllHousesWherePersonLivedByPersonUuid(UUID personUuid, Pageable pageable) {
        return findAllHousesByPersonUuidAndType(personUuid, PersonType.TENANT, pageable);
    }

    @Override
    public Page<HouseResponse> findAllHousesWhichPersonOwnedByPersonUuid(UUID personUuid, Pageable pageable) {
        return findAllHousesByPersonUuidAndType(personUuid, PersonType.OWNER, pageable);
    }

    @Override
    public Page<PersonResponse> findAllPeopleThatLivedInHouseByHouseUuid(UUID houseUuid, Pageable pageable) {
        return findAllPeopleByHouseUuidAndType(houseUuid, PersonType.TENANT, pageable);
    }

    @Override
    public Page<PersonResponse> findAllPeopleThatOwnedHouseByHouseUuid(UUID houseUuid, Pageable pageable) {
        return findAllPeopleByHouseUuidAndType(houseUuid, PersonType.OWNER, pageable);
    }
    
    private Page<PersonResponse> findAllPeopleByHouseUuidAndType(UUID houseUuid, PersonType type, Pageable pageable) {

            Page<HouseHistory> historyPage = houseHistoryRepository.findAllByHouseUuidAndType(
                    houseUuid,
                    type,
                    pageable);

            return historyPage.map(history -> personMapper.toResponse(history.getPerson()));
    }

    private Page<HouseResponse> findAllHousesByPersonUuidAndType(UUID personUuid, PersonType type, Pageable pageable) {

            Page<HouseHistory> historyPage = houseHistoryRepository.findAllByPersonUuidAndType(
                    personUuid,
                    type,
                    pageable);

            return historyPage.map(history -> houseMapper.toResponse(history.getHouse()));
    }
}
