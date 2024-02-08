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
import ru.clevertec.house.dto.HouseResponse;
import ru.clevertec.house.dto.PersonRequest;
import ru.clevertec.house.dto.PersonResponse;
import ru.clevertec.house.exception.ApiError;

import java.util.UUID;

import static ru.clevertec.house.constant.ControllerConstants.FIRST_PAGE_NUMBER;
import static ru.clevertec.house.constant.ControllerConstants.MAX_PAGE_SIZE;
import static ru.clevertec.house.constant.ControllerConstants.MIN_PAGE_SIZE;

@Tag(name = "People")
public interface PersonControllerOpenApiSpec {

    @Operation(summary = "find a person", responses = {
            @ApiResponse(
                    responseCode = StatusCodes.OK + "",
                    description = "The person is found.",
                    useReturnTypeSchema = true
            ),
            @ApiResponse(
                    responseCode = StatusCodes.NOT_FOUND + "",
                    description = "The person is not found.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = StatusCodes.BAD_REQUEST + "",
                    description = "A malformed uuid is passed.",
                    content = @Content(schema = @Schema(implementation = ApiError.class)
                    )
            )
    })
    ResponseEntity<PersonResponse> findPersonByUUID(
            @Parameter(example = OpenApiSchema.Examples.PersonDTO.UUID_EXAMPLE)
            UUID uuid);


    @Operation(summary = "find all people within a page specified by request parameters", responses = {
            @ApiResponse(
                    responseCode = StatusCodes.OK + "",
                    description = "The requested page of people is found.",
                    useReturnTypeSchema = true
            ),
            @ApiResponse(
                    responseCode = StatusCodes.BAD_REQUEST + "",
                    description = "An invalid page number OR an invalid page size.",
                    content = @Content(schema = @Schema(implementation = ApiError.class)
                    )
            )
    })
    ResponseEntity<Page<PersonResponse>> findAllPeople(
            @Min(FIRST_PAGE_NUMBER)
            @Parameter(description = "A page number.")
            Integer page,

            @Max(MAX_PAGE_SIZE)
            @Min(MIN_PAGE_SIZE)
            @Parameter(description = "A page size.")
            Integer size);


    @Operation(summary = """
            find all houses,
            that have ever been owned by a specified person,
            by a page specified by request parameters""", responses = {
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
    ResponseEntity<Page<HouseResponse>> findAllEverOwnedHousesByPersonUUID(
            @Parameter(example = OpenApiSchema.Examples.PersonDTO.UUID_EXAMPLE)
            UUID uuid,

            @Min(FIRST_PAGE_NUMBER)
            @Parameter(description = "A page number.")
            Integer page,

            @Max(MAX_PAGE_SIZE)
            @Min(MIN_PAGE_SIZE)
            @Parameter(description = "A page size.")
            Integer size);


    @Operation(summary = """
            find all houses,
            where a specified person have ever lived,
            by a page specified by request parameters""", responses = {
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
    ResponseEntity<Page<HouseResponse>> findAllEverLivedInHousesByPersonUUID(
            @Parameter(example = OpenApiSchema.Examples.PersonDTO.UUID_EXAMPLE)
            UUID uuid,

            @Min(FIRST_PAGE_NUMBER)
            @Parameter(description = "A page number.")
            Integer page,

            @Max(MAX_PAGE_SIZE)
            @Min(MIN_PAGE_SIZE)
            @Parameter(description = "A page size.")
            Integer size);


    @Operation(summary = """
            find all houses,
            that are currently owned by a specified person,
            by a page specified by request parameters""", responses = {
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
    ResponseEntity<Page<HouseResponse>> findAllOwnedHousesByOwnerUUID(
            @Parameter(example = OpenApiSchema.Examples.PersonDTO.UUID_EXAMPLE)
            UUID ownerUUID,

            @Min(FIRST_PAGE_NUMBER)
            @Parameter(description = "A page number.")
            Integer page,

            @Max(MAX_PAGE_SIZE)
            @Min(MIN_PAGE_SIZE)
            @Parameter(description = "A page size.")
            Integer size);


    @Operation(summary = "delete a person", responses = {
            @ApiResponse(
                    responseCode = StatusCodes.NO_CONTENT + "",
                    description = "The person was successfully deleted or did not exist.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = StatusCodes.BAD_REQUEST + "",
                    description = "The person can not be deleted due to certain constraints.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    ResponseEntity<PersonResponse> deletePersonByUUID(
            @Parameter(example = OpenApiSchema.Examples.PersonDTO.UUID_EXAMPLE)
            UUID ownerUUID);


    @Operation(summary = "update a person", responses = {
            @ApiResponse(
                    responseCode = StatusCodes.OK + "",
                    description = "The person was successfully updated.",
                    useReturnTypeSchema = true
            ),
            @ApiResponse(
                    responseCode = StatusCodes.NOT_FOUND + "",
                    description = "The person was not found.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = StatusCodes.BAD_REQUEST + "",
                    description = "A malformed uuid OR an invalid request body.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    ResponseEntity<PersonResponse> updatePersonByUUID(
            @Parameter(example = OpenApiSchema.Examples.PersonDTO.UUID_EXAMPLE)
            UUID ownerUUID,

            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody
            PersonRequest personRequest);


    @Operation(summary = "create a new person", responses = {
            @ApiResponse(
                    responseCode = StatusCodes.OK + "",
                    description = "The new person was successfully created.",
                    useReturnTypeSchema = true
            ),
            @ApiResponse(
                    responseCode = StatusCodes.BAD_REQUEST + "",
                    description = "An invalid request body.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    ResponseEntity<PersonResponse> createPerson(
            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody
            PersonRequest personRequest);
}
