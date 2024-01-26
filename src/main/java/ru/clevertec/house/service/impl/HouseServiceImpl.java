package ru.clevertec.house.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
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
import ru.clevertec.house.exception.ServiceException;
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
        try {
            return houseRepository.findByUuid(uuid)
                    .map(houseMapper::toResponse);
        } catch (RuntimeException ex) {
            throw new ServiceException(ex.getMessage(), ex);
        }
    }

    @Override
    public Page<HouseResponse> findAll(Pageable pageable) {
        try {
            return houseRepository.findAll(pageable)
                    .map(houseMapper::toResponse);
        } catch (RuntimeException ex) {
            throw new ServiceException(ex.getMessage(), ex);
        }
    }

    @Override
    public Page<HouseResponse> findAllHousesByOwnerUUID(UUID ownerUUID, Pageable pageable) {
        try {
            return houseRepository.findAllHousesByOwnerUuid(ownerUUID, pageable)
                    .map(houseMapper::toResponse);
        } catch (RuntimeException ex) {
            throw new ServiceException(ex.getMessage(), ex);
        }
    }

    @Override
    @Transactional
    public void addNewOwnerToHouse(UUID houseUUID, UUID newOwnerUUID) {
        try {
            Person newOwner = personRepository.findByUuid(newOwnerUUID)
                    .orElseThrow(() -> new ServiceException("the specified owner does not exist"));

            House house = houseRepository.findByUuid(houseUUID)
                    .orElseThrow(() -> new ServiceException("the specified house does not exist"));

            // add to owner list because Person is the owning side
            newOwner.getOwnedHouses().add(house);
        } catch (RuntimeException ex) {
            throw new ServiceException(ex.getMessage(), ex);
        }
    }

    @Override
    @Transactional
    @CacheableDeleteByUUID
    public long deleteByUUID(UUID uuid) throws DataIntegrityViolationException {
        try {
            return houseRepository.deleteByUuid(uuid);
        } catch (RuntimeException ex) {
            throw new ServiceException(ex.getMessage(), ex);
        }
    }

    @Override
    @Transactional
    @CacheableCreate
    public HouseResponse create(HouseRequest houseRequest) {
        try {
            House houseToCreate = houseMapper.fromRequest(houseRequest);
            return houseMapper.toResponse(houseRepository.save(houseToCreate));
        } catch (RuntimeException ex) {
            throw new ServiceException(ex.getMessage(), ex);
        }
    }

    @Override
    @Transactional
    @CacheableUpdateByUUID
    public Optional<HouseResponse> update(HouseRequest houseToUpdateRequest, UUID houseUUID) {
        try {
            Optional<House> optionalHouse = houseRepository.findByUuid(houseUUID);
            if (optionalHouse.isEmpty()) {
                return Optional.empty();
            }

            House existingHouse = optionalHouse.get();
            House houseToUpdate = houseMapper.fromRequest(houseToUpdateRequest);

            setFieldsToUpdateOnExistingHouse(existingHouse, houseToUpdate);

            House updatedVersion = houseRepository.save(existingHouse);

            return Optional.of(houseMapper.toResponse(updatedVersion));
        } catch (RuntimeException ex) {
            throw new ServiceException(ex.getMessage(), ex);
        }
    }

    private static void setFieldsToUpdateOnExistingHouse(House existingHouse, House houseToUpdate) {
        existingHouse.setCity(houseToUpdate.getCity());
        existingHouse.setStreet(houseToUpdate.getStreet());
        existingHouse.setCountry(houseToUpdate.getCountry());
        existingHouse.setNumber(houseToUpdate.getNumber());
        existingHouse.setArea(houseToUpdate.getArea());
    }
}
