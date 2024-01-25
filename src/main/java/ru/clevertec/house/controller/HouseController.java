package ru.clevertec.house.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.clevertec.house.constant.StatusCode;
import ru.clevertec.house.dto.HouseRequest;
import ru.clevertec.house.dto.HouseResponse;
import ru.clevertec.house.dto.PersonResponse;
import ru.clevertec.house.service.HouseHistoryService;
import ru.clevertec.house.service.HouseService;
import ru.clevertec.house.service.PersonService;

import java.util.Optional;
import java.util.UUID;

import static ru.clevertec.house.constant.ControllerConstants.*;

@RestController
@RequestMapping("/houses")
@RequiredArgsConstructor
@Validated
public class HouseController {

    private final HouseService houseService;
    private final PersonService personService;
    private final HouseHistoryService houseHistoryService;

    @GetMapping("/{uuid}")
    public ResponseEntity<HouseResponse> findHouseByUUID(@PathVariable("uuid") UUID uuid) {

        Optional<HouseResponse> found = houseService.findByUUID(uuid);

        return found.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND.value()).build());
    }

    @GetMapping
    public ResponseEntity<Page<HouseResponse>> findAllHouses(
            @RequestParam(value = PAGE_PARAMETER_NAME, defaultValue = "" + FIRST_PAGE_NUMBER)
            @Min(FIRST_PAGE_NUMBER)
            Integer page,

            @RequestParam(value = SIZE_PARAMETER_NAME, defaultValue = DEFAULT_SIZE)
            @Max(MAX_SIZE)
            @Min(MIN_SIZE)
            Integer size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<HouseResponse> found = houseService.findAll(pageable);

        return ResponseEntity.ok(found);
    }

    @GetMapping("/{houseUUID}/ever-owners")
    public ResponseEntity<Page<PersonResponse>> findAllEverOwnersByHouseUUID(
            @PathVariable("houseUUID")
            UUID houseUUID,

            @RequestParam(value = PAGE_PARAMETER_NAME, defaultValue = "" + FIRST_PAGE_NUMBER)
            @Min(FIRST_PAGE_NUMBER)
            Integer page,

            @RequestParam(value = SIZE_PARAMETER_NAME, defaultValue = DEFAULT_SIZE)
            @Max(MAX_SIZE)
            @Min(MIN_SIZE)
            Integer size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<PersonResponse> found = houseHistoryService.findAllPeopleThatOwnedHouseByHouseUuid(houseUUID, pageable);

        return ResponseEntity.ok(found);
    }

    @GetMapping("/{houseUUID}/ever-residents")
    public ResponseEntity<Page<PersonResponse>> findAllEverResidentsByHouseUUID(
            @PathVariable("houseUUID")
            UUID houseUUID,

            @RequestParam(value = PAGE_PARAMETER_NAME, defaultValue = "" + FIRST_PAGE_NUMBER)
            @Min(FIRST_PAGE_NUMBER)
            Integer page,

            @RequestParam(value = SIZE_PARAMETER_NAME, defaultValue = DEFAULT_SIZE)
            @Max(MAX_SIZE)
            @Min(MIN_SIZE)
            Integer size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<PersonResponse> found = houseHistoryService.findAllPeopleThatLivedInHouseByHouseUuid(houseUUID, pageable);

        return ResponseEntity.ok(found);
    }

    @GetMapping("/{houseUUID}/residents")
    public ResponseEntity<Page<PersonResponse>> findAllResidentsByHouseUUID(
            @PathVariable("houseUUID")
            UUID houseUUID,

            @RequestParam(value = PAGE_PARAMETER_NAME, defaultValue = "" + FIRST_PAGE_NUMBER)
            @Min(FIRST_PAGE_NUMBER)
            Integer page,

            @RequestParam(value = SIZE_PARAMETER_NAME, defaultValue = DEFAULT_SIZE)
            @Max(MAX_SIZE)
            @Min(MIN_SIZE)
            Integer size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<PersonResponse> found = personService.findAllResidentsByHouseOfResidenceUUID(houseUUID, pageable);

        return ResponseEntity.ok(found);
    }

    @PutMapping("/{uuid}/add-owner")
    public ResponseEntity<HouseResponse> addNewOwnerToHouse(@PathVariable("uuid") UUID houseUuid,
                                                  @RequestBody UUID ownerUuid) {
        houseService.addNewOwnerToHouse(houseUuid, ownerUuid);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<HouseResponse> deleteHouseByUUID(@PathVariable("uuid") UUID uuid) {

        houseService.deleteByUUID(uuid);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<HouseResponse> updateHouseByUUID(
            @PathVariable("uuid") UUID uuid,
            @RequestBody @Valid HouseRequest houseRequest) {

        Optional<HouseResponse> optionalHouseResponse = houseService.update(houseRequest, uuid);

        return optionalHouseResponse.map(houseResponse -> ResponseEntity
                        .status(StatusCode.OK)
                        .body(houseResponse))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<HouseResponse> createHouse(@RequestBody @Valid HouseRequest houseRequest) {

        HouseResponse houseResponse = houseService.create(houseRequest);

        return ResponseEntity
                .status(StatusCode.CREATED)
                .body(houseResponse);
    }
}
