package ru.clevertec.house.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.clevertec.house.dto.HouseRequest;
import ru.clevertec.house.dto.HouseResponse;
import ru.clevertec.house.dto.PersonResponse;
import ru.clevertec.house.exception.ServiceException;
import ru.clevertec.house.mapper.HouseMapper;
import ru.clevertec.house.mapper.PersonMapper;
import ru.clevertec.house.entity.House;
import ru.clevertec.house.repository.HouseRepository;
import ru.clevertec.house.service.HouseService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class HouseServiceImpl implements HouseService {

    private final HouseRepository houseRepository;
    private final HouseMapper houseMapper;
    private final PersonMapper personMapper;

    @Override
    public Optional<HouseResponse> findByUUID(UUID uuid) {
        try {
            return houseRepository.findByUUID(uuid)
                    .map(houseMapper::toResponse);
        } catch (RuntimeException ex) {
            throw new ServiceException(ex.getMessage(), ex);
        }
    }

    @Override
    public List<HouseResponse> findAll(int page, int size) {
        try {
            return houseRepository.findAll(page, size)
                    .stream()
                    .map(houseMapper::toResponse)
                    .toList();
        } catch (RuntimeException ex) {
            throw new ServiceException(ex.getMessage(), ex);
        }
    }

    @Override
    public List<HouseResponse> findAllHousesByOwnerUUID(UUID ownerUUID, int page, int size) {
        try {
            return houseRepository.findAllHousesByOwnerUUID(ownerUUID, page, size)
                    .stream()
                    .map(houseMapper::toResponse)
                    .toList();
        } catch (RuntimeException ex) {
            throw new ServiceException(ex.getMessage(), ex);
        }
    }

    @Override
    public void deleteByUUID(UUID uuid) throws DataIntegrityViolationException {
        try {
            houseRepository.deleteByUUID(uuid);
        } catch (RuntimeException ex) {
            throw new ServiceException(ex.getMessage(), ex);
        }
    }

    @Override
    public void update(HouseRequest houseToUpdateRequest, UUID houseUUID) {
        try {
            House houseToUpdate = houseMapper.fromRequest(houseToUpdateRequest);
            houseRepository.update(houseToUpdate, houseUUID);
        } catch (RuntimeException ex) {
            throw new ServiceException(ex.getMessage(), ex);
        }
    }

    @Override
    public void create(HouseRequest houseRequest) {
        try {
            House houseToCreate = houseMapper.fromRequest(houseRequest);
            houseRepository.create(houseToCreate);
        } catch (RuntimeException ex) {
            throw new ServiceException(ex.getMessage(), ex);
        }
    }

}
