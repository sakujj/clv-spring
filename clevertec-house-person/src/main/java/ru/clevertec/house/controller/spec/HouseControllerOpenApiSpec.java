package ru.clevertec.house.controller.spec;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import ru.clevertec.house.constant.StatusCodes;
import ru.clevertec.house.documentation.OpenApiSchema;
import ru.clevertec.house.dto.HouseRequest;
import ru.clevertec.house.dto.HouseResponse;
import ru.clevertec.house.dto.PersonResponse;
import ru.clevertec.house.exception.ApiError;

import java.util.UUID;

import static ru.clevertec.house.constant.ControllerConstants.FIRST_PAGE_NUMBER;
import static ru.clevertec.house.constant.ControllerConstants.MAX_PAGE_SIZE;
import static ru.clevertec.house.constant.ControllerConstants.MIN_PAGE_SIZE;

@Tag(name = "Houses")
public interface HouseControllerOpenApiSpec {

    @Operation(summary = "find a house", responses = {
            @ApiResponse(
                    responseCode = StatusCodes.OK + "",
                    description = "The house is found.",
                    content = @Content(
                            schema = @Schema(implementation = HouseResponse.class))
            ),
            @ApiResponse(
                    responseCode = StatusCodes.NOT_FOUND + "",
                    description = "The house is not found.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = StatusCodes.BAD_REQUEST + "",
                    description = "A malformed uuid is passed.",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class)
                    )
            )
    })
    ResponseEntity<HouseResponse> findHouseByUUID(
            @Parameter(example = OpenApiSchema.Examples.HouseDTO.UUID_EXAMPLE)
            UUID uuid);


    @Operation(summary = "find all houses within a page specified by request parameters", responses = {
            @ApiResponse(
                    responseCode = StatusCodes.OK + "",
                    description = "The requested page of houses is found.",
                    useReturnTypeSchema = true
            ),
            @ApiResponse(
                    responseCode = StatusCodes.BAD_REQUEST + "",
                    description = "An invalid page number OR an invalid page size.",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class)
                    )
            )
    })
    ResponseEntity<Page<HouseResponse>> findAllHouses(
            @Min(FIRST_PAGE_NUMBER)
            @Parameter(description = "A page number.")
            Integer page,

            @Max(MAX_PAGE_SIZE)
            @Min(MIN_PAGE_SIZE)
            @Parameter(description = "A page size.")
            Integer size);


    @Operation(summary = """
            find all people,
            that have ever owned a specified house,
            within a page specified by request parameters""", responses = {
            @ApiResponse(
                    responseCode = StatusCodes.OK + "",
                    description = "The requested page of people is found.",
                    useReturnTypeSchema = true
            ),
            @ApiResponse(
                    responseCode = StatusCodes.BAD_REQUEST + "",
                    description = "A malformed uuid OR an invalid page number OR an invalid page size.",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class)
                    )
            )
    })
    ResponseEntity<Page<PersonResponse>> findAllEverOwnersByHouseUUID(
            @Parameter(example = OpenApiSchema.Examples.HouseDTO.UUID_EXAMPLE)
            UUID houseUUID,

            @Min(FIRST_PAGE_NUMBER)
            @Parameter(description = "A page number.")
            Integer page,

            @Max(MAX_PAGE_SIZE)
            @Min(MIN_PAGE_SIZE)
            @Parameter(description = "A page size.")
            Integer size);


    @Operation(summary = """
            find all people,
            that have ever lived in a specified house,
            within a page specified by request parameters""", responses = {
            @ApiResponse(
                    responseCode = StatusCodes.OK + "",
                    description = "The requested page of people is found.",
                    useReturnTypeSchema = true
            ),
            @ApiResponse(
                    responseCode = StatusCodes.BAD_REQUEST + "",
                    description = "A malformed uuid OR an invalid page number OR an invalid page size.",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class)
                    )
            )
    })
    ResponseEntity<Page<PersonResponse>> findAllEverResidentsByHouseUUID(
            @Parameter(example = OpenApiSchema.Examples.HouseDTO.UUID_EXAMPLE)
            UUID houseUUID,

            @Min(FIRST_PAGE_NUMBER)
            @Parameter(description = "A page number.")
            Integer page,

            @Max(MAX_PAGE_SIZE)
            @Min(MIN_PAGE_SIZE)
            @Parameter(description = "A page size.")
            Integer size);


    @Operation(summary = """
            find all people,
            that are currently living in a specified house,
            within a page specified by request parameters""", responses = {
            @ApiResponse(
                    responseCode = StatusCodes.OK + "",
                    description = "The requested page of people is found.",
                    useReturnTypeSchema = true
            ),
            @ApiResponse(
                    responseCode = StatusCodes.BAD_REQUEST + "",
                    description = "A malformed uuid OR an invalid page number OR an invalid page size.",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class)
                    )
            )
    })
    ResponseEntity<Page<PersonResponse>> findAllResidentsByHouseUUID(
            @Parameter(example = OpenApiSchema.Examples.HouseDTO.UUID_EXAMPLE)
            UUID houseUUID,

            @Min(FIRST_PAGE_NUMBER)
            @Parameter(description = "A page number.")
            Integer page,

            @Max(MAX_PAGE_SIZE)
            @Min(MIN_PAGE_SIZE)
            @Parameter(description = "A page size.")
            Integer size);


    @Operation(summary = "delete a house", responses = {
            @ApiResponse(
                    responseCode = StatusCodes.NO_CONTENT + "",
                    description = "The house was successfully deleted or did not exist.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = StatusCodes.BAD_REQUEST + "",
                    description = "The house can not be deleted due to certain constraints.",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class)
                    )
            )
    })
    ResponseEntity<HouseResponse> deleteHouseByUUID(
            @Parameter(example = OpenApiSchema.Examples.HouseDTO.UUID_EXAMPLE)
            UUID uuid);


    @Operation(summary = "update a house", responses = {
            @ApiResponse(
                    responseCode = StatusCodes.OK + "",
                    description = "The house was successfully updated.",
                    useReturnTypeSchema = true
            ),
            @ApiResponse(
                    responseCode = StatusCodes.BAD_REQUEST + "",
                    description = "A malformed uuid OR an invalid request body.",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class)
                    )
            ),
            @ApiResponse(
                    responseCode = StatusCodes.NOT_FOUND + "",
                    description = "House was not found.",
                    content = @Content
            )
    })
    ResponseEntity<HouseResponse> updateHouseByUUID(
            @Parameter(example = OpenApiSchema.Examples.HouseDTO.UUID_EXAMPLE)
            UUID uuid,

            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody
            HouseRequest houseRequest);


    @Operation(summary = "add a new owner to a house", responses = {
            @ApiResponse(
                    responseCode = StatusCodes.NO_CONTENT + "",
                    description = "A new owner was successfully added.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = StatusCodes.BAD_REQUEST + "",
                    description = "A malformed uuid OR an invalid request body "
                            + "OR the house was not found OR the owner was not found.",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class)
                    )
            )
    })
    ResponseEntity<HouseResponse> addNewOwnerToHouse(
            @Parameter(example = OpenApiSchema.Examples.HouseDTO.UUID_EXAMPLE)
            UUID houseUuid,

            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "A uuid of the new owner.")
            UUID ownerUuid);


    @Operation(summary = "create a new house", responses = {
            @ApiResponse(
                    responseCode = StatusCodes.CREATED + "",
                    description = "The new house was successfully created.",
                    useReturnTypeSchema = true),
            @ApiResponse(
                    responseCode = StatusCodes.BAD_REQUEST + "",
                    description = "An invalid request body.",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class)
                    )
            )
    })
    ResponseEntity<HouseResponse> createHouse(
            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody
            HouseRequest houseRequest);
}
