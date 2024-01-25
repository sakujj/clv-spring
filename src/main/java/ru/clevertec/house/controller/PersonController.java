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
import ru.clevertec.house.dto.HouseResponse;
import ru.clevertec.house.dto.PersonRequest;
import ru.clevertec.house.dto.PersonResponse;
import ru.clevertec.house.service.HouseHistoryService;
import ru.clevertec.house.service.HouseService;
import ru.clevertec.house.service.PersonService;

import java.util.Optional;
import java.util.UUID;

import static ru.clevertec.house.constant.ControllerConstants.*;
import static ru.clevertec.house.constant.ControllerConstants.MIN_SIZE;

@RestController
@Validated
@RequestMapping("/people")
@RequiredArgsConstructor
public class PersonController {

    private final PersonService personService;
    private final HouseService houseService;
    private final HouseHistoryService houseHistoryService;

    @GetMapping("/{uuid}")
    public ResponseEntity<PersonResponse> findPersonByUUID(@PathVariable("uuid") UUID uuid) {

        Optional<PersonResponse> found = personService.findByUUID(uuid);

        return found.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND.value()).build());
    }

    @GetMapping
    public ResponseEntity<Page<PersonResponse>> findAllPeople(
            @RequestParam(value = PAGE_PARAMETER_NAME, defaultValue = "" + FIRST_PAGE_NUMBER)
            @Min(FIRST_PAGE_NUMBER)
            Integer page,

            @RequestParam(value = SIZE_PARAMETER_NAME, defaultValue = DEFAULT_SIZE)
            @Max(MAX_SIZE)
            @Min(MIN_SIZE)
            Integer size) {

        Page<PersonResponse> found = personService.findAll(PageRequest.of(page, size));

        return ResponseEntity.ok(found);
    }

    @GetMapping("/{uuid}/ever-owned-houses")
    public ResponseEntity<Page<HouseResponse>> findAllEverOwnedHousesByPersonUUID(
            @PathVariable("uuid")
            UUID uuid,

            @RequestParam(value = PAGE_PARAMETER_NAME, defaultValue = "" + FIRST_PAGE_NUMBER)
            @Min(FIRST_PAGE_NUMBER)
            Integer page,

            @RequestParam(value = SIZE_PARAMETER_NAME, defaultValue = DEFAULT_SIZE)
            @Max(MAX_SIZE)
            @Min(MIN_SIZE)
            Integer size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<HouseResponse> found = houseHistoryService.findAllHousesWhichPersonOwnedByPersonUuid(uuid, pageable);

        return ResponseEntity.ok(found);
    }

    @GetMapping("/{uuid}/ever-lived-in-houses")
    public ResponseEntity<Page<HouseResponse>> findAllEverLivedInHousesByPersonUUID(
            @PathVariable("uuid")
            UUID uuid,

            @RequestParam(value = PAGE_PARAMETER_NAME, defaultValue = "" + FIRST_PAGE_NUMBER)
            @Min(FIRST_PAGE_NUMBER)
            Integer page,

            @RequestParam(value = SIZE_PARAMETER_NAME, defaultValue = DEFAULT_SIZE)
            @Max(MAX_SIZE)
            @Min(MIN_SIZE)
            Integer size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<HouseResponse> found = houseHistoryService.findAllHousesWherePersonLivedByPersonUuid(uuid, pageable);

        return ResponseEntity.ok(found);
    }

    @GetMapping("/{ownerUUID}/owned-houses")
    public ResponseEntity<Page<HouseResponse>> findAllOwnedHousesByOwnerUUID(
            @PathVariable("ownerUUID")
            UUID ownerUUID,

            @RequestParam(value = PAGE_PARAMETER_NAME, defaultValue = "" + FIRST_PAGE_NUMBER)
            @Min(FIRST_PAGE_NUMBER)
            Integer page,

            @RequestParam(value = SIZE_PARAMETER_NAME, defaultValue = DEFAULT_SIZE)
            @Max(MAX_SIZE)
            @Min(MIN_SIZE)
            Integer size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<HouseResponse> found = houseService.findAllHousesByOwnerUUID(ownerUUID, pageable);

        return ResponseEntity.ok(found);
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<PersonResponse> deletePersonByUUID(@PathVariable("uuid") UUID ownerUUID) {

        personService.deleteByUUID(ownerUUID);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<PersonResponse> updatePersonByUUID(
            @PathVariable("uuid") UUID ownerUUID,
            @RequestBody @Valid PersonRequest personRequest) {

        Optional<PersonResponse> optionalPersonResponse = personService.update(personRequest, ownerUUID);

        return optionalPersonResponse
                .map(personResponse -> ResponseEntity
                        .status(StatusCode.OK)
                        .body(personResponse))
                .orElseGet(() -> ResponseEntity.notFound().build());

    }

    @PostMapping
    public ResponseEntity<PersonResponse> createPerson(@RequestBody @Valid PersonRequest personRequest) {

        PersonResponse personResponse = personService.create(personRequest);

        return ResponseEntity
                .status(StatusCode.CREATED)
                .body(personResponse);
    }

}
