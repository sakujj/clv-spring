package ru.clevertec.house.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.clevertec.house.constant.StatusCodes;
import ru.clevertec.house.controller.spec.PersonControllerOpenApiSpec;
import ru.clevertec.house.dto.HouseResponse;
import ru.clevertec.house.dto.PersonRequest;
import ru.clevertec.house.dto.PersonResponse;
import ru.clevertec.house.service.HouseHistoryService;
import ru.clevertec.house.service.HouseService;
import ru.clevertec.house.service.PersonService;

import java.util.Optional;
import java.util.UUID;

import static ru.clevertec.house.constant.ControllerConstants.DEFAULT_PAGE_SIZE;
import static ru.clevertec.house.constant.ControllerConstants.FIRST_PAGE_NUMBER;
import static ru.clevertec.house.constant.ControllerConstants.MAX_PAGE_SIZE;
import static ru.clevertec.house.constant.ControllerConstants.MIN_PAGE_SIZE;
import static ru.clevertec.house.constant.ControllerConstants.PAGE_NUMBER_PARAMETER_NAME;
import static ru.clevertec.house.constant.ControllerConstants.PAGE_SIZE_PARAMETER_NAME;

@RestController
@Validated
@RequestMapping(value = "/people", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class PersonController implements PersonControllerOpenApiSpec {

    private final PersonService personService;
    private final HouseService houseService;
    private final HouseHistoryService houseHistoryService;

    @Override
    @GetMapping("/{uuid}")
    public ResponseEntity<PersonResponse> findPersonByUUID(@PathVariable("uuid") UUID uuid) {

        Optional<PersonResponse> found = personService.findByUUID(uuid);

        return found.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND.value()).build());
    }

    @Override
    @GetMapping
    public ResponseEntity<Page<PersonResponse>> findAllPeople(
            @RequestParam(value = PAGE_NUMBER_PARAMETER_NAME, defaultValue = "" + FIRST_PAGE_NUMBER)
            @Min(FIRST_PAGE_NUMBER)
            Integer page,

            @Max(MAX_PAGE_SIZE)
            @Min(MIN_PAGE_SIZE)
            @RequestParam(value = PAGE_SIZE_PARAMETER_NAME, defaultValue = DEFAULT_PAGE_SIZE)
            Integer size) {

        Page<PersonResponse> found = personService.findAll(PageRequest.of(page, size));

        return ResponseEntity.ok(found);
    }


    @Override
    @GetMapping("/{personUUID}/ever-owned-houses")
    public ResponseEntity<Page<HouseResponse>> findAllEverOwnedHousesByPersonUUID(
            @PathVariable("personUUID")
            UUID uuid,

            @RequestParam(value = PAGE_NUMBER_PARAMETER_NAME, defaultValue = "" + FIRST_PAGE_NUMBER)
            @Min(FIRST_PAGE_NUMBER)
            Integer page,

            @Max(MAX_PAGE_SIZE)
            @Min(MIN_PAGE_SIZE)
            @RequestParam(value = PAGE_SIZE_PARAMETER_NAME, defaultValue = DEFAULT_PAGE_SIZE)
            Integer size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<HouseResponse> found = houseHistoryService.findAllHousesWhichPersonOwnedByPersonUuid(uuid, pageable);

        return ResponseEntity.ok(found);
    }


    @Override
    @GetMapping("/{uuid}/ever-lived-in-houses")
    public ResponseEntity<Page<HouseResponse>> findAllEverLivedInHousesByPersonUUID(
            @PathVariable("uuid")
            UUID uuid,

            @RequestParam(value = PAGE_NUMBER_PARAMETER_NAME, defaultValue = "" + FIRST_PAGE_NUMBER)
            @Min(FIRST_PAGE_NUMBER)
            Integer page,

            @Max(MAX_PAGE_SIZE)
            @Min(MIN_PAGE_SIZE)
            @RequestParam(value = PAGE_SIZE_PARAMETER_NAME, defaultValue = DEFAULT_PAGE_SIZE)
            Integer size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<HouseResponse> found = houseHistoryService.findAllHousesWherePersonLivedByPersonUuid(uuid, pageable);

        return ResponseEntity.ok(found);
    }


    @Override
    @GetMapping("/{ownerUUID}/owned-houses")
    public ResponseEntity<Page<HouseResponse>> findAllOwnedHousesByOwnerUUID(
            @PathVariable("ownerUUID")
            UUID ownerUUID,

            @RequestParam(value = PAGE_NUMBER_PARAMETER_NAME, defaultValue = "" + FIRST_PAGE_NUMBER)
            @Min(FIRST_PAGE_NUMBER)
            Integer page,

            @Max(MAX_PAGE_SIZE)
            @Min(MIN_PAGE_SIZE)
            @RequestParam(value = PAGE_SIZE_PARAMETER_NAME, defaultValue = DEFAULT_PAGE_SIZE)
            Integer size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<HouseResponse> found = houseService.findAllHousesByOwnerUUID(ownerUUID, pageable);

        return ResponseEntity.ok(found);
    }


    @Override
    @DeleteMapping("/{uuid}")
    public ResponseEntity<PersonResponse> deletePersonByUUID(@PathVariable("uuid") UUID ownerUUID) {

        personService.deleteByUUID(ownerUUID);

        return ResponseEntity.noContent().build();
    }


    @Override
    @PutMapping("/{uuid}")
    public ResponseEntity<PersonResponse> updatePersonByUUID(
            @PathVariable("uuid")
            UUID ownerUUID,

            @Valid
            @RequestBody
            PersonRequest personRequest) {

        Optional<PersonResponse> optionalPersonResponse = personService.update(personRequest, ownerUUID);

        return optionalPersonResponse
                .map(personResponse -> ResponseEntity
                        .status(StatusCodes.OK)
                        .body(personResponse))
                .orElseGet(() -> ResponseEntity.notFound().build());

    }


    @Override
    @PostMapping
    public ResponseEntity<PersonResponse> createPerson(@Valid @RequestBody PersonRequest personRequest) {

        PersonResponse personResponse = personService.create(personRequest);

        return ResponseEntity
                .status(StatusCodes.CREATED)
                .body(personResponse);
    }
}
