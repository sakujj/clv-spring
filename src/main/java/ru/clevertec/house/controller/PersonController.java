package ru.clevertec.house.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.clevertec.house.dto.HouseResponse;
import ru.clevertec.house.dto.PersonRequest;
import ru.clevertec.house.dto.PersonResponse;
import ru.clevertec.house.exception.InvalidRequestException;
import ru.clevertec.house.service.PersonService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/people")
@RequiredArgsConstructor
public class PersonController {

    private static final int DEFAULT_SIZE_PER_PAGE = 15;

    private final PersonService personService;

    @GetMapping("/{uuid}")
    public ResponseEntity<PersonResponse> findPersonByUUID(@PathVariable("uuid") UUID uuid) {

        Optional<PersonResponse> found = personService.findByUUID(uuid);
        return found.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND.value()).build());
    }

    @GetMapping
    public ResponseEntity<List<PersonResponse>> findAllPeople(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "" + DEFAULT_SIZE_PER_PAGE) Integer size) {

        List<PersonResponse> found = personService.findAll(page, size);
        return ResponseEntity.ok(found);
    }

    @GetMapping("/{ownerUUID}/owned-houses")
    public ResponseEntity<List<HouseResponse>> findAllPeopleHouseOfResidenceUUID(
            @PathVariable("ownerUUID") UUID ownerUUID,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "" + DEFAULT_SIZE_PER_PAGE) Integer size) {

        List<HouseResponse> found = personService.findAllHousesByOwnerUUID(ownerUUID, page, size);
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
            @RequestBody(required = false) PersonRequest personRequest) {

        if (personRequest == null) {
            throw new InvalidRequestException();
        }

        personService.update(personRequest, ownerUUID);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<PersonResponse> createPerson(@RequestBody(required = false) @Valid PersonRequest personRequest) {

        if (personRequest == null) {
            throw new InvalidRequestException();
        }

        personService.create(personRequest);
        return ResponseEntity.noContent().build();
    }

}
