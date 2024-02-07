package ru.clevertec.house.controller;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import ru.clevertec.house.controller.spec.HouseControllerOpenApiSpec;
import ru.clevertec.house.documentation.OpenApiSchema;
import ru.clevertec.house.dto.HouseRequest;
import ru.clevertec.house.dto.HouseResponse;
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
@RequestMapping(value = "/houses", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Validated
public class HouseController implements HouseControllerOpenApiSpec {

    private final HouseService houseService;
    private final PersonService personService;
    private final HouseHistoryService houseHistoryService;

    @Override
    @GetMapping(value = "/{uuid}")
    public ResponseEntity<HouseResponse> findHouseByUUID(@PathVariable("uuid") UUID uuid) {

        Optional<HouseResponse> found = houseService.findByUUID(uuid);

        return found.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(StatusCodes.NOT_FOUND).build());
    }


    @Override
    @GetMapping
    public ResponseEntity<Page<HouseResponse>> findAllHouses(
            @Min(FIRST_PAGE_NUMBER)
            @RequestParam(value = PAGE_NUMBER_PARAMETER_NAME, defaultValue = "" + FIRST_PAGE_NUMBER)
            Integer page,

            @Max(MAX_PAGE_SIZE)
            @Min(MIN_PAGE_SIZE)
            @RequestParam(value = PAGE_SIZE_PARAMETER_NAME, defaultValue = DEFAULT_PAGE_SIZE)
            Integer size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<HouseResponse> found = houseService.findAll(pageable);

        return ResponseEntity.ok(found);
    }


    @Override
    @GetMapping("/{houseUUID}/ever-owners")
    public ResponseEntity<Page<PersonResponse>> findAllEverOwnersByHouseUUID(
            @PathVariable("houseUUID")
            UUID houseUUID,

            @RequestParam(value = PAGE_NUMBER_PARAMETER_NAME, defaultValue = "" + FIRST_PAGE_NUMBER)
            @Min(FIRST_PAGE_NUMBER)
            Integer page,

            @Max(MAX_PAGE_SIZE)
            @Min(MIN_PAGE_SIZE)
            @RequestParam(value = PAGE_SIZE_PARAMETER_NAME, defaultValue = DEFAULT_PAGE_SIZE)
            Integer size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<PersonResponse> found = houseHistoryService.findAllPeopleThatOwnedHouseByHouseUuid(houseUUID, pageable);

        return ResponseEntity.ok(found);
    }


    @Override
    @GetMapping("/{houseUUID}/ever-residents")
    public ResponseEntity<Page<PersonResponse>> findAllEverResidentsByHouseUUID(
            @PathVariable("houseUUID")
            UUID houseUUID,

            @RequestParam(value = PAGE_NUMBER_PARAMETER_NAME, defaultValue = "" + FIRST_PAGE_NUMBER)
            @Min(FIRST_PAGE_NUMBER)
            Integer page,

            @Max(MAX_PAGE_SIZE)
            @Min(MIN_PAGE_SIZE)
            @RequestParam(value = PAGE_SIZE_PARAMETER_NAME, defaultValue = DEFAULT_PAGE_SIZE)
            Integer size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<PersonResponse> found = houseHistoryService.findAllPeopleThatLivedInHouseByHouseUuid(houseUUID, pageable);

        return ResponseEntity.ok(found);
    }


    @Override
    @GetMapping("/{houseUUID}/residents")
    public ResponseEntity<Page<PersonResponse>> findAllResidentsByHouseUUID(
            @PathVariable("houseUUID")
            UUID houseUUID,

            @RequestParam(value = PAGE_NUMBER_PARAMETER_NAME, defaultValue = "" + FIRST_PAGE_NUMBER)
            @Min(FIRST_PAGE_NUMBER)
            Integer page,

            @Max(MAX_PAGE_SIZE)
            @Min(MIN_PAGE_SIZE)
            @RequestParam(value = PAGE_SIZE_PARAMETER_NAME, defaultValue = DEFAULT_PAGE_SIZE)
            Integer size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<PersonResponse> found = personService.findAllResidentsByHouseOfResidenceUUID(houseUUID, pageable);

        return ResponseEntity.ok(found);
    }


    @Override
    @DeleteMapping("/{uuid}")
    public ResponseEntity<HouseResponse> deleteHouseByUUID(
            @PathVariable("uuid")
            @Parameter(example = OpenApiSchema.Examples.HouseDTO.UUID_EXAMPLE)
            UUID uuid) {

        houseService.deleteByUUID(uuid);

        return ResponseEntity.noContent().build();
    }


    @Override
    @PutMapping("/{uuid}")
    public ResponseEntity<HouseResponse> updateHouseByUUID(
            @PathVariable("uuid")
            UUID uuid,

            @Valid
            @RequestBody
            HouseRequest houseRequest) {

        Optional<HouseResponse> optionalHouseResponse = houseService.update(houseRequest, uuid);

        return optionalHouseResponse.map(houseResponse -> ResponseEntity
                        .status(StatusCodes.OK)
                        .body(houseResponse))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    @Override
    @PutMapping("/{uuid}/add-owner")
    public ResponseEntity<HouseResponse> addNewOwnerToHouse(
            @PathVariable("uuid")
            UUID houseUuid,

            @RequestBody
            UUID ownerUuid) {

        houseService.addNewOwnerToHouse(houseUuid, ownerUuid);

        return ResponseEntity.noContent().build();
    }


    @Override
    @PostMapping
    public ResponseEntity<HouseResponse> createHouse(
            @Valid
            @RequestBody
            HouseRequest houseRequest) {

        HouseResponse houseResponse = houseService.create(houseRequest);

        return ResponseEntity
                .status(StatusCodes.CREATED)
                .body(houseResponse);
    }
}
