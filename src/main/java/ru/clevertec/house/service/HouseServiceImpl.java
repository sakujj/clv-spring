package ru.clevertec.house.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.clevertec.house.cache.aop.CacheableCreate;
import ru.clevertec.house.cache.aop.CacheableDeleteByUUID;
import ru.clevertec.house.cache.aop.CacheableFindByUUID;
import ru.clevertec.house.cache.aop.CacheableUpdateByUUID;
import ru.clevertec.house.dto.HouseRequest;
import ru.clevertec.house.dto.HouseResponse;
import ru.clevertec.house.entity.House;
import ru.clevertec.house.entity.Person;
import ru.clevertec.house.mapper.HouseMapper;
import ru.clevertec.house.repository.HouseRepository;
import ru.clevertec.house.repository.PersonRepository;
import ru.clevertec.house.service.HouseService;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HouseServiceImpl implements HouseService {

    private final HouseRepository houseRepository;
    private final HouseMapper houseMapper;
    private final PersonRepository personRepository;

    @Override
    @CacheableFindByUUID
    public Optional<HouseResponse> findByUUID(UUID uuid) {

        return houseRepository.findByUuid(uuid)
                .map(houseMapper::toResponse);
    }

    @Override
    public Page<HouseResponse> findAll(Pageable pageable) {

        return houseRepository.findAll(pageable)
                .map(houseMapper::toResponse);
    }

    @Override
    public Page<HouseResponse> findAllHousesByOwnerUUID(UUID ownerUUID, Pageable pageable) {

        return houseRepository.findAllHousesByOwnerUuid(ownerUUID, pageable)
                .map(houseMapper::toResponse);
    }

    @Override
    @Transactional
    public void addNewOwnerToHouse(UUID houseUUID, UUID newOwnerUUID) {

        Person newOwner = personRepository.findByUuid(newOwnerUUID)
                .orElseThrow(() -> new RuntimeException("the specified owner does not exist"));

        House house = houseRepository.findByUuid(houseUUID)
                .orElseThrow(() -> new RuntimeException("the specified house does not exist"));

        // add to owner list because Person is the owning side
        newOwner.getOwnedHouses().add(house);
    }

    @Override
    @Transactional
    @CacheableDeleteByUUID
    public long deleteByUUID(UUID uuid) {

        long l = houseRepository.deleteByUuid(uuid);
        return l;
    }

    @Override
    @Transactional
    @CacheableCreate
    public HouseResponse create(HouseRequest houseRequest) {

            House houseToCreate = houseMapper.fromRequest(houseRequest);
            return houseMapper.toResponse(houseRepository.save(houseToCreate));
    }

    @Override
    @Transactional
    @CacheableUpdateByUUID
    public Optional<HouseResponse> update(HouseRequest houseToUpdateRequest, UUID houseUUID) {

            Optional<House> optionalHouse = houseRepository.findByUuid(houseUUID);
            if (optionalHouse.isEmpty()) {
                return Optional.empty();
            }

            House existingHouse = optionalHouse.get();
            House houseToUpdate = houseMapper.fromRequest(houseToUpdateRequest);

            setFieldsToUpdateOnExistingHouse(existingHouse, houseToUpdate);

            House updatedVersion = houseRepository.save(existingHouse);

            return Optional.of(houseMapper.toResponse(updatedVersion));
    }

    private static void setFieldsToUpdateOnExistingHouse(House existingHouse, House houseToUpdate) {
        existingHouse.setCity(houseToUpdate.getCity());
        existingHouse.setStreet(houseToUpdate.getStreet());
        existingHouse.setCountry(houseToUpdate.getCountry());
        existingHouse.setNumber(houseToUpdate.getNumber());
        existingHouse.setArea(houseToUpdate.getArea());
    }
}
