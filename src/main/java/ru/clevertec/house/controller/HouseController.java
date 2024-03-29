package ru.clevertec.house.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.clevertec.house.dto.HouseRequest;
import ru.clevertec.house.dto.HouseResponse;
import ru.clevertec.house.mapper.HouseMapper;
import ru.clevertec.house.service.HouseService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/houses")
@RequiredArgsConstructor
public class HouseController {
    private static final int DEFAULT_SIZE_PER_PAGE = 15;

    private final HouseService houseService;

    @GetMapping("/{uuid}")
    public ResponseEntity<HouseResponse> findHouseByUUID(@PathVariable("uuid") UUID uuid) {

        Optional<HouseResponse> found = houseService.findByUUID(uuid);
        return found.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND.value()).build());
    }

    @GetMapping
    public ResponseEntity<List<HouseResponse>> findAllHouses(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "" + DEFAULT_SIZE_PER_PAGE) Integer size) {

        List<HouseResponse> found = houseService.findAll(page, size);
        return ResponseEntity.ok(found);
    }

    @GetMapping("/owner/{ownerUUID}")
    public ResponseEntity<List<HouseResponse>> findAllHousesByOwnerUUID(
            @PathVariable("ownerUUID") UUID ownerUUID,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "" + DEFAULT_SIZE_PER_PAGE) Integer size) {

        List<HouseResponse> found = houseService.findAllByOwnerUUID(ownerUUID, page, size);
        return ResponseEntity.ok(found);
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<HouseResponse> deleteHouseByUUID(@PathVariable("uuid") UUID ownerUUID) {

        houseService.deleteByUUID(ownerUUID);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<HouseResponse> updateHouseByUUID(
            @PathVariable("uuid") UUID ownerUUID,
            @RequestBody HouseRequest houseRequest) {

        houseService.update(houseRequest, ownerUUID);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<HouseResponse> createHouse(@RequestBody HouseRequest houseRequest) {

        houseService.create(houseRequest);
        return ResponseEntity.noContent().build();
    }

}
