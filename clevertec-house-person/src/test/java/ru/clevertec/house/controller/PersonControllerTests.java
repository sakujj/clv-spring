package ru.clevertec.house.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.clevertec.house.constant.ControllerConstants;
import ru.clevertec.house.constant.FormatConstants;
import ru.clevertec.house.dto.HouseResponse;
import ru.clevertec.house.dto.PersonRequest;
import ru.clevertec.house.dto.PersonResponse;
import ru.clevertec.house.entity.House;
import ru.clevertec.house.entity.Person;
import ru.clevertec.house.enumeration.Sex;
import ru.clevertec.house.exception.ApiError;
import ru.clevertec.house.exception.RestExceptionHandler;
import ru.clevertec.house.service.HouseHistoryService;
import ru.clevertec.house.service.HouseService;
import ru.clevertec.house.service.PersonService;
import ru.clevertec.house.test.util.HouseTestBuilder;
import ru.clevertec.house.test.util.JsonReader;
import ru.clevertec.house.test.util.PersonTestBuilder;
import ru.clevertec.house.testcontainer.CommonPostgresContainerInitializer;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional(readOnly = true)
class PersonControllerTests extends CommonPostgresContainerInitializer {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HouseHistoryService houseHistoryService;

    @SpyBean
    private HouseService houseService;

    @MockBean
    private PersonService personService;

    @SpyBean
    private ObjectMapper objectMapper;

    @SpyBean
    private RestExceptionHandler restExceptionHandler;

    private static final String PAGE_NUMBER_JSON_FIELD_NAME = "number";
    private static final String PAGE_SIZE_JSON_FIELD_NAME = "size";
    private static final String PAGE_CONTENT_JSON_FIELD_NAME = "content";

    private static final String JSON_FOLDER = "JSONs/Person";
    private static final String VALID_PERSON_REQUEST = "validPersonRequest.json";
    private static final String INVALID_PERSON_REQUEST = "invalidPersonRequest.json";


    @Nested
    class findPersonByUuidTests {

        @Test
        void shouldThrowTypeMismatchException_onInvalidUUIDVersion4InTheURL() throws Exception {
            // given
            String malformedUUID = "malformed-uuid-v4-possible-content";

            // when
            mockMvc.perform(get("/people/{uuid}", malformedUUID)
                            .contentType(APPLICATION_JSON))
                    // then
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$." + ApiError.Fields.errorMessage).exists());

            verify(restExceptionHandler).handleTypeMismatch(any(TypeMismatchException.class), any(), any(), any());
        }

        @Test
        void shouldNotThrowException_onCorrectUUIDVersionV4InTheURI() throws Exception {
            // given
            UUID uuid = UUID.fromString("709cf39e-a60a-451a-a5bb-4ac832c6e6b4");
            when(personService.findByUUID(uuid))
                    .thenReturn(Optional.of(PersonResponse.builder().build()));

            // when
            mockMvc.perform(get("/people/{uuid}", uuid)
                            .contentType(APPLICATION_JSON))
                    // then
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").exists())
                    .andExpect(jsonPath("$." + ApiError.Fields.errorMessage).doesNotExist())
                    .andExpect(jsonPath("$." + ApiError.Fields.validationErrors).doesNotExist());

            verifyNoInteractions(restExceptionHandler);
        }

        @Test
        void shouldRespondWithStatusOkAndCorrectBody_onSuccessfulFindByUUIDUsingPersonService() throws Exception {
            // given
            UUID uuid = UUID.fromString("709cf39e-a60a-451a-a5bb-4ac832c6e6b4");
            PersonResponse expectedResponse = PersonTestBuilder.aPerson().buildResponse();

            when(personService.findByUUID(uuid))
                    .thenReturn(Optional.of(expectedResponse));

            //when
            ResultActions actions = mockMvc.perform(get("/people/{uuid}", uuid)
                            .contentType(APPLICATION_JSON))
                    // then
                    .andExpect(status().isOk());

            andExpectJsonPathToHave(expectedResponse, actions);
        }

        @Test
        void shouldReturnNotFoundStatusAndEmptyBody_whenEmptyOptionalFromPersonServiceIsReturned() throws Exception {
            // given
            UUID uuid = UUID.fromString("709cf39e-a60a-451a-a5bb-4ac832c6e6b4");
            Optional<HouseResponse> expectedOptional = Optional.empty();

            when(houseService.findByUUID(uuid))
                    .thenReturn(expectedOptional);

            //when
            mockMvc.perform(get("/houses/{uuid}", uuid)
                            .contentType(APPLICATION_JSON))
                    // then
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$").doesNotExist());

            verify(houseService).findByUUID(uuid);
        }

    }

    @Nested
    class findAllPeopleTests {

        @Test
        void shouldThrowValidationException_onPageParameterLessThanNumberOfFirstPage() throws Exception {
            // given, when
            mockMvc.perform(get("/people")
                            .param(
                                    ControllerConstants.PAGE_NUMBER_PARAMETER_NAME,
                                    String.valueOf(ControllerConstants.FIRST_PAGE_NUMBER - 1))
                            .contentType(APPLICATION_JSON))
                    // then
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$." + ApiError.Fields.errorMessage).exists())
                    .andExpect(jsonPath("$." + ApiError.Fields.validationErrors).exists());

            verify(restExceptionHandler).handleConstraintValidationException(any(ConstraintViolationException.class));
        }

        @Test
        void shouldThrowValidationException_onPageSizeParameterLessThanMin() throws Exception {
            // given, when
            mockMvc.perform(get("/people")
                            .param(
                                    ControllerConstants.PAGE_SIZE_PARAMETER_NAME,
                                    String.valueOf(ControllerConstants.MIN_PAGE_SIZE - 1))
                            .contentType(APPLICATION_JSON))
                    // then
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$." + ApiError.Fields.errorMessage).exists())
                    .andExpect(jsonPath("$." + ApiError.Fields.validationErrors).exists());

            verify(restExceptionHandler).handleConstraintValidationException(any(ConstraintViolationException.class));
        }

        @Test
        void shouldThrowValidationException_onPageSizeParameterBiggerThanMax() throws Exception {
            // given, when
            mockMvc.perform(get("/houses")
                            .param(
                                    ControllerConstants.PAGE_SIZE_PARAMETER_NAME,
                                    String.valueOf(ControllerConstants.MAX_PAGE_SIZE + 1))
                            .contentType(APPLICATION_JSON))
                    // then
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$." + ApiError.Fields.errorMessage).exists())
                    .andExpect(jsonPath("$." + ApiError.Fields.validationErrors).exists());

            verify(restExceptionHandler).handleConstraintValidationException(any(ConstraintViolationException.class));
        }

        @Test
        void shouldNotThrowException_onCorrectRequestParametersLikePageAndPageSize() throws Exception {
            // given
            when(personService.findAll(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of()));

            // when
            mockMvc.perform(get("/people")
                            .param(
                                    ControllerConstants.PAGE_NUMBER_PARAMETER_NAME,
                                    String.valueOf(ControllerConstants.FIRST_PAGE_NUMBER))
                            .param(
                                    ControllerConstants.PAGE_SIZE_PARAMETER_NAME,
                                    String.valueOf(ControllerConstants.MAX_PAGE_SIZE))
                            .contentType(APPLICATION_JSON))
                    // then
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").exists())
                    .andExpect(jsonPath("$." + ApiError.Fields.errorMessage).doesNotExist())
                    .andExpect(jsonPath("$." + ApiError.Fields.validationErrors).doesNotExist());

            verifyNoInteractions(restExceptionHandler);
        }

        @Test
        void shouldRespondWithStatusOkAndCorrectBody_onSuccessfulOperationUsingHouseService() throws Exception {
            // given
            int requestedPageNumber = ControllerConstants.FIRST_PAGE_NUMBER;
            int requestedPageSize = ControllerConstants.MAX_PAGE_SIZE;
            Pageable expectedPageRequest = PageRequest.of(requestedPageNumber, requestedPageSize);

            PersonResponse expectedResponse = PersonTestBuilder.aPerson().buildResponse();

            List<PersonResponse> expectedContent = List.of(expectedResponse);
            when(personService.findAll(expectedPageRequest))
                    .thenReturn(new PageImpl<>(expectedContent));

            // when
            ResultActions actions = mockMvc.perform(get("/people")
                            .param(
                                    ControllerConstants.PAGE_NUMBER_PARAMETER_NAME,
                                    String.valueOf(requestedPageNumber))
                            .param(
                                    ControllerConstants.PAGE_SIZE_PARAMETER_NAME,
                                    String.valueOf(requestedPageSize))
                            .contentType(APPLICATION_JSON))
                    // then

                    .andExpect(status().isOk());

            andExpectedJsonPathToHavePageContentFirstItemLike(expectedResponse, actions);

            verify(personService).findAll(expectedPageRequest);
        }
    }

    @Nested
    class findAllEverOwnedHousesByPersonUUIDTests {

        @Test
        void shouldThrowTypeMismatchException_onInvalidUUIDVersion4InTheURL() throws Exception {
            // given
            String malformedUUID = "malformed-uuid-v4-possible-content";

            // when
            mockMvc.perform(get("/people/{uuid}/ever-owned-houses", malformedUUID)
                            .contentType(APPLICATION_JSON))
                    // then
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$." + ApiError.Fields.errorMessage).exists());

            verify(restExceptionHandler).handleTypeMismatch(any(TypeMismatchException.class), any(), any(), any());
        }

        @Test
        void shouldThrowValidationException_onPageParameterLessThanNumberOfFirstPage() throws Exception {
            // given
            UUID uuid = UUID.fromString("b1983ac8-4069-432b-9f13-39da739b127a");

            // when
            mockMvc.perform(get("/people/{uuid}/ever-owned-houses", uuid)
                            .param(
                                    ControllerConstants.PAGE_NUMBER_PARAMETER_NAME,
                                    String.valueOf(ControllerConstants.FIRST_PAGE_NUMBER - 1))
                            .contentType(APPLICATION_JSON))
                    // then
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$." + ApiError.Fields.errorMessage).exists())
                    .andExpect(jsonPath("$." + ApiError.Fields.validationErrors).exists());

            verify(restExceptionHandler).handleConstraintValidationException(any(ConstraintViolationException.class));
        }

        @Test
        void shouldThrowValidationException_onPageSizeParameterLessThanMin() throws Exception {
            // given
            UUID uuid = UUID.fromString("b1983ac8-4069-432b-9f13-39da739b127a");

            // when
            mockMvc.perform(get("/people/{uuid}/ever-owned-houses", uuid)
                            .param(
                                    ControllerConstants.PAGE_SIZE_PARAMETER_NAME,
                                    String.valueOf(ControllerConstants.MIN_PAGE_SIZE - 1))
                            .contentType(APPLICATION_JSON))
                    // then
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$." + ApiError.Fields.errorMessage).exists())
                    .andExpect(jsonPath("$." + ApiError.Fields.validationErrors).exists());

            verify(restExceptionHandler).handleConstraintValidationException(any(ConstraintViolationException.class));
        }

        @Test
        void shouldThrowValidationException_onPageSizeParameterBiggerThanMax() throws Exception {
            // given
            UUID uuid = UUID.fromString("b1983ac8-4069-432b-9f13-39da739b127a");

            // when
            mockMvc.perform(get("/people/{uuid}/ever-owned-houses", uuid)
                            .param(
                                    ControllerConstants.PAGE_SIZE_PARAMETER_NAME,
                                    String.valueOf(ControllerConstants.MAX_PAGE_SIZE + 1))
                            .contentType(APPLICATION_JSON))
                    // then
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$." + ApiError.Fields.errorMessage).exists())
                    .andExpect(jsonPath("$." + ApiError.Fields.validationErrors).exists());

            verify(restExceptionHandler).handleConstraintValidationException(any(ConstraintViolationException.class));
        }

        @Test
        void shouldNotThrowException_onCorrectUUIDAndRequestParametersLikePageAndPageSize() throws Exception {
            // given
            UUID uuid = UUID.fromString("b1983ac8-4069-432b-9f13-39da739b127a");

            when(houseHistoryService.findAllHousesWhichPersonOwnedByPersonUuid(eq(uuid), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of()));

            // when
            mockMvc.perform(get("/people/{uuid}/ever-owned-houses", uuid)
                            .param(
                                    ControllerConstants.PAGE_NUMBER_PARAMETER_NAME,
                                    String.valueOf(ControllerConstants.FIRST_PAGE_NUMBER))
                            .param(
                                    ControllerConstants.PAGE_SIZE_PARAMETER_NAME,
                                    String.valueOf(ControllerConstants.MAX_PAGE_SIZE))
                            .contentType(APPLICATION_JSON))
                    // then
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").exists())
                    .andExpect(jsonPath("$." + ApiError.Fields.errorMessage).doesNotExist())
                    .andExpect(jsonPath("$." + ApiError.Fields.validationErrors).doesNotExist());

            verifyNoInteractions(restExceptionHandler);
        }

        @Test
        void shouldRespondWithStatusOkAndCorrectBody_onSuccessfulOperationUsingHouseService() throws Exception {
            // given
            UUID uuid = UUID.fromString("b1983ac8-4069-432b-9f13-39da739b127a");

            int requestedPageNumber = ControllerConstants.FIRST_PAGE_NUMBER;
            int requestedPageSize = ControllerConstants.MAX_PAGE_SIZE;
            Pageable expectedPageRequest = PageRequest.of(requestedPageNumber, requestedPageSize);

            HouseResponse expectedResponse = HouseTestBuilder.aHouse().buildResponse();

            List<HouseResponse> expectedContent = List.of(expectedResponse);
            when(houseHistoryService.findAllHousesWhichPersonOwnedByPersonUuid(uuid, expectedPageRequest))
                    .thenReturn(new PageImpl<>(expectedContent));

            // when
            ResultActions actions = mockMvc.perform(get("/people/{uuid}/ever-owned-houses", uuid)
                            .param(
                                    ControllerConstants.PAGE_NUMBER_PARAMETER_NAME,
                                    String.valueOf(requestedPageNumber))
                            .param(
                                    ControllerConstants.PAGE_SIZE_PARAMETER_NAME,
                                    String.valueOf(requestedPageSize))
                            .contentType(APPLICATION_JSON))
                    // then
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$." + PAGE_NUMBER_JSON_FIELD_NAME).exists())
                    .andExpect(jsonPath("$." + PAGE_SIZE_JSON_FIELD_NAME).exists());

            andExpectedJsonPathToHavePageContentFirstItemLike(expectedResponse, actions);

            verify(houseHistoryService).findAllHousesWhichPersonOwnedByPersonUuid(uuid, expectedPageRequest);
        }
    }

    @Nested
    class findAllEverLivedInHousesByPersonUUIDTests {

        @Test
        void shouldThrowTypeMismatchException_onInvalidUUIDVersion4InTheURL() throws Exception {
            // given
            String malformedUUID = "malformed-uuid-v4-possible-content";

            // when
            mockMvc.perform(get("/people/{uuid}/ever-lived-in-houses", malformedUUID)
                            .contentType(APPLICATION_JSON))
                    // then
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$." + ApiError.Fields.errorMessage).exists());

            verify(restExceptionHandler).handleTypeMismatch(any(TypeMismatchException.class), any(), any(), any());
        }

        @Test
        void shouldThrowValidationException_onPageParameterLessThanNumberOfFirstPage() throws Exception {
            // given
            UUID uuid = UUID.fromString("b1983ac8-4069-432b-9f13-39da739b127a");

            // when
            mockMvc.perform(get("/people/{uuid}/ever-lived-in-houses", uuid)
                            .param(
                                    ControllerConstants.PAGE_NUMBER_PARAMETER_NAME,
                                    String.valueOf(ControllerConstants.FIRST_PAGE_NUMBER - 1))
                            .contentType(APPLICATION_JSON))
                    // then
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$." + ApiError.Fields.errorMessage).exists())
                    .andExpect(jsonPath("$." + ApiError.Fields.validationErrors).exists());

            verify(restExceptionHandler).handleConstraintValidationException(any(ConstraintViolationException.class));
        }

        @Test
        void shouldThrowValidationException_onPageSizeParameterLessThanMin() throws Exception {
            // given
            UUID uuid = UUID.fromString("b1983ac8-4069-432b-9f13-39da739b127a");

            // when
            mockMvc.perform(get("/people/{uuid}/ever-lived-in-houses", uuid)
                            .param(
                                    ControllerConstants.PAGE_SIZE_PARAMETER_NAME,
                                    String.valueOf(ControllerConstants.MIN_PAGE_SIZE - 1))
                            .contentType(APPLICATION_JSON))
                    // then
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$." + ApiError.Fields.errorMessage).exists())
                    .andExpect(jsonPath("$." + ApiError.Fields.validationErrors).exists());

            verify(restExceptionHandler).handleConstraintValidationException(any(ConstraintViolationException.class));
        }

        @Test
        void shouldThrowValidationException_onPageSizeParameterBiggerThanMax() throws Exception {
            // given
            UUID uuid = UUID.fromString("b1983ac8-4069-432b-9f13-39da739b127a");

            // when
            mockMvc.perform(get("/people/{uuid}/ever-lived-in-houses", uuid)
                            .param(
                                    ControllerConstants.PAGE_SIZE_PARAMETER_NAME,
                                    String.valueOf(ControllerConstants.MAX_PAGE_SIZE + 1))
                            .contentType(APPLICATION_JSON))
                    // then
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$." + ApiError.Fields.errorMessage).exists())
                    .andExpect(jsonPath("$." + ApiError.Fields.validationErrors).exists());

            verify(restExceptionHandler).handleConstraintValidationException(any(ConstraintViolationException.class));
        }

        @Test
        void shouldNotThrowException_onCorrectUUIDAndRequestParametersLikePageAndPageSize() throws Exception {
            // given
            UUID uuid = UUID.fromString("b1983ac8-4069-432b-9f13-39da739b127a");

            when(houseHistoryService.findAllHousesWherePersonLivedByPersonUuid(eq(uuid), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of()));

            // when
            mockMvc.perform(get("/people/{uuid}/ever-lived-in-houses", uuid)
                            .param(
                                    ControllerConstants.PAGE_NUMBER_PARAMETER_NAME,
                                    String.valueOf(ControllerConstants.FIRST_PAGE_NUMBER))
                            .param(
                                    ControllerConstants.PAGE_SIZE_PARAMETER_NAME,
                                    String.valueOf(ControllerConstants.MAX_PAGE_SIZE))
                            .contentType(APPLICATION_JSON))
                    // then
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").exists())
                    .andExpect(jsonPath("$." + ApiError.Fields.errorMessage).doesNotExist())
                    .andExpect(jsonPath("$." + ApiError.Fields.validationErrors).doesNotExist());

            verifyNoInteractions(restExceptionHandler);
        }

        @Test
        void shouldRespondWithStatusOkAndCorrectBody_onSuccessfulOperationUsingPersonService() throws Exception {
            // given
            UUID uuid = UUID.fromString("b1983ac8-4069-432b-9f13-39da739b127a");

            int requestedPageNumber = ControllerConstants.FIRST_PAGE_NUMBER;
            int requestedPageSize = ControllerConstants.MAX_PAGE_SIZE;
            Pageable expectedPageRequest = PageRequest.of(requestedPageNumber, requestedPageSize);

            HouseResponse expectedResponse = HouseTestBuilder.aHouse().buildResponse();

            List<HouseResponse> expectedContent = List.of(expectedResponse);
            when(houseHistoryService.findAllHousesWherePersonLivedByPersonUuid(uuid, expectedPageRequest))
                    .thenReturn(new PageImpl<>(expectedContent));

            // when
            ResultActions actions = mockMvc.perform(get("/people/{uuid}/ever-lived-in-houses", uuid)
                            .param(
                                    ControllerConstants.PAGE_NUMBER_PARAMETER_NAME,
                                    String.valueOf(requestedPageNumber))
                            .param(
                                    ControllerConstants.PAGE_SIZE_PARAMETER_NAME,
                                    String.valueOf(requestedPageSize))
                            .contentType(APPLICATION_JSON))
                    // then
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$." + PAGE_NUMBER_JSON_FIELD_NAME).exists())
                    .andExpect(jsonPath("$." + PAGE_SIZE_JSON_FIELD_NAME).exists());

            andExpectedJsonPathToHavePageContentFirstItemLike(expectedResponse, actions);

            verify(houseHistoryService).findAllHousesWherePersonLivedByPersonUuid(uuid, expectedPageRequest);
        }
    }

    @Nested
    class findAllOwnedHousesByOwnerUUIDTests {

        @Test
        void shouldThrowTypeMismatchException_onInvalidUUIDVersion4InTheURL() throws Exception {
            // given
            String malformedUUID = "malformed-uuid-v4-possible-content";

            // when
            mockMvc.perform(get("/people/{uuid}/owned-houses", malformedUUID)
                            .contentType(APPLICATION_JSON))
                    // then
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$." + ApiError.Fields.errorMessage).exists());

            verify(restExceptionHandler).handleTypeMismatch(any(TypeMismatchException.class), any(), any(), any());
        }

        @Test
        void shouldThrowValidationException_onPageParameterLessThanNumberOfFirstPage() throws Exception {
            // given
            UUID uuid = UUID.fromString("b1983ac8-4069-432b-9f13-39da739b127a");

            // when
            mockMvc.perform(get("/people/{uuid}/owned-houses", uuid)
                            .param(
                                    ControllerConstants.PAGE_NUMBER_PARAMETER_NAME,
                                    String.valueOf(ControllerConstants.FIRST_PAGE_NUMBER - 1))
                            .contentType(APPLICATION_JSON))
                    // then
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$." + ApiError.Fields.errorMessage).exists())
                    .andExpect(jsonPath("$." + ApiError.Fields.validationErrors).exists());

            verify(restExceptionHandler).handleConstraintValidationException(any(ConstraintViolationException.class));
        }

        @Test
        void shouldThrowValidationException_onPageSizeParameterLessThanMin() throws Exception {
            // given
            UUID uuid = UUID.fromString("b1983ac8-4069-432b-9f13-39da739b127a");

            // when
            mockMvc.perform(get("/people/{uuid}/owned-houses", uuid)
                            .param(
                                    ControllerConstants.PAGE_SIZE_PARAMETER_NAME,
                                    String.valueOf(ControllerConstants.MIN_PAGE_SIZE - 1))
                            .contentType(APPLICATION_JSON))
                    // then
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$." + ApiError.Fields.errorMessage).exists())
                    .andExpect(jsonPath("$." + ApiError.Fields.validationErrors).exists());

            verify(restExceptionHandler).handleConstraintValidationException(any(ConstraintViolationException.class));
        }

        @Test
        void shouldThrowValidationException_onPageSizeParameterBiggerThanMax() throws Exception {
            // given
            UUID uuid = UUID.fromString("b1983ac8-4069-432b-9f13-39da739b127a");

            // when
            mockMvc.perform(get("/people/{uuid}/owned-houses", uuid)
                            .param(
                                    ControllerConstants.PAGE_SIZE_PARAMETER_NAME,
                                    String.valueOf(ControllerConstants.MAX_PAGE_SIZE + 1))
                            .contentType(APPLICATION_JSON))
                    // then
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$." + ApiError.Fields.errorMessage).exists())
                    .andExpect(jsonPath("$." + ApiError.Fields.validationErrors).exists());

            verify(restExceptionHandler).handleConstraintValidationException(any(ConstraintViolationException.class));
        }

        @Test
        void shouldNotThrowException_onCorrectUUIDAndRequestParameters() throws Exception {
            // given
            UUID uuid = UUID.fromString("b1983ac8-4069-432b-9f13-39da739b127a");

            when(houseService.findAllHousesByOwnerUUID(eq(uuid), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of()));

            // when
            mockMvc.perform(get("/people/{uuid}/owned-houses", uuid)
                            .param(
                                    ControllerConstants.PAGE_NUMBER_PARAMETER_NAME,
                                    String.valueOf(ControllerConstants.FIRST_PAGE_NUMBER))
                            .param(
                                    ControllerConstants.PAGE_SIZE_PARAMETER_NAME,
                                    String.valueOf(ControllerConstants.MAX_PAGE_SIZE))
                            .contentType(APPLICATION_JSON))
                    // then
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").exists())
                    .andExpect(jsonPath("$." + ApiError.Fields.errorMessage).doesNotExist())
                    .andExpect(jsonPath("$." + ApiError.Fields.validationErrors).doesNotExist());

            verifyNoInteractions(restExceptionHandler);
        }

        @Test
        void shouldRespondWithStatusOkAndCorrectBody_onSuccessfulOperationUsingHouseService() throws Exception {
            // given
            UUID uuid = UUID.fromString("b1983ac8-4069-432b-9f13-39da739b127a");

            int requestedPageNumber = ControllerConstants.FIRST_PAGE_NUMBER;
            int requestedPageSize = ControllerConstants.MAX_PAGE_SIZE;
            Pageable expectedPageRequest = PageRequest.of(requestedPageNumber, requestedPageSize);

            HouseResponse expectedResponse = HouseTestBuilder.aHouse().buildResponse();

            List<HouseResponse> expectedContent = List.of(expectedResponse);
            when(houseService.findAllHousesByOwnerUUID(uuid, expectedPageRequest))
                    .thenReturn(new PageImpl<>(expectedContent));

            // when
            ResultActions actions = mockMvc.perform(get("/people/{uuid}/owned-houses", uuid)
                            .param(
                                    ControllerConstants.PAGE_NUMBER_PARAMETER_NAME,
                                    String.valueOf(requestedPageNumber))
                            .param(
                                    ControllerConstants.PAGE_SIZE_PARAMETER_NAME,
                                    String.valueOf(requestedPageSize))
                            .contentType(APPLICATION_JSON))
                    // then
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$." + PAGE_NUMBER_JSON_FIELD_NAME).exists())
                    .andExpect(jsonPath("$." + PAGE_SIZE_JSON_FIELD_NAME).exists());

            andExpectedJsonPathToHavePageContentFirstItemLike(expectedResponse, actions);

            verify(houseService).findAllHousesByOwnerUUID(uuid, expectedPageRequest);
        }
    }

    @Nested
    class deleteByUUIDTests {

        @Test
        void shouldThrowTypeMismatchException_onInvalidUUIDVersion4InTheURL() throws Exception {
            // given
            String malformedUUID = "malformed-uuid-v4-possible-content";

            // when
            mockMvc.perform(delete("/people/{uuid}", malformedUUID)
                            .contentType(APPLICATION_JSON))
                    // then
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$." + ApiError.Fields.errorMessage).exists());

            verify(restExceptionHandler).handleTypeMismatch(any(TypeMismatchException.class), any(), any(), any());
        }

        @Test
        void shouldNotThrowException_onValidUUIDVersion4InTheURL() throws Exception {
            // given
            UUID uuid = UUID.fromString("b1983ac8-4069-432b-9f13-39da739b127a");
            long expected = 1L;

            when(personService.deleteByUUID(uuid))
                    .thenReturn(expected);

            // when
            mockMvc.perform(delete("/people/{uuid}", uuid)
                            .contentType(APPLICATION_JSON))
                    // then
                    .andExpect(status().isNoContent())
                    .andExpect(jsonPath("$").doesNotExist());

            verifyNoInteractions(restExceptionHandler);
        }

        @Test
        void shouldRespondWithStatusNoContentAndEmptyBody_onSuccessfulDeleteUsingPersonService() throws Exception {
            // given
            UUID uuid = UUID.fromString("b1983ac8-4069-432b-9f13-39da739b127a");
            long expected = 1L;

            when(personService.deleteByUUID(uuid))
                    .thenReturn(expected);

            // when
            mockMvc.perform(delete("/people/{uuid}", uuid)
                            .contentType(APPLICATION_JSON))
                    // then
                    .andExpect(status().isNoContent())
                    .andExpect(jsonPath("$").doesNotExist());

            verify(personService).deleteByUUID(uuid);
        }
    }

    @Nested
    class updateByPersonUUIDTests {

        @Test
        void shouldThrowTypeMismatchException_onInvalidUUIDVersion4InTheURL() throws Exception {
            // given
            String malformedUUID = "malformed-uuid-v4-possible-content";
            String body = JsonReader.jsonFromFile(JSON_FOLDER + "/" + VALID_PERSON_REQUEST);

            // when
            mockMvc.perform(put("/people/{uuid}", malformedUUID)
                            .contentType(APPLICATION_JSON)
                            .content(body))
                    // then
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$." + ApiError.Fields.errorMessage).exists());

            verify(restExceptionHandler).handleTypeMismatch(any(TypeMismatchException.class), any(), any(), any());
        }

        @Test
        void shouldThrowMethodArgumentNotValidException_onInvalidRequestBody() throws Exception {
            // given
            UUID uuid = UUID.fromString("b1983ac8-4069-432b-9f13-39da739b127a");
            String invalidBody = JsonReader.jsonFromFile(JSON_FOLDER + "/" + INVALID_PERSON_REQUEST);

            // when
            mockMvc.perform(put("/people/{uuid}", uuid)
                            .contentType(APPLICATION_JSON)
                            .content(invalidBody))
                    // then
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$." + ApiError.Fields.errorMessage).exists())
                    .andExpect(jsonPath("$." + ApiError.Fields.validationErrors).exists());

            verify(restExceptionHandler).handleMethodArgumentNotValid(any(MethodArgumentNotValidException.class), any(), any(), any());
        }

        @Test
        void shouldThrowHttpMessageNotReadableException_onMalformedRequestBody() throws Exception {
            // given
            UUID uuid = UUID.fromString("b1983ac8-4069-432b-9f13-39da739b127a");
            String malformedBody = JsonReader.jsonFromFile(JSON_FOLDER + "/" + VALID_PERSON_REQUEST)
                    .replace('"', ' ');

            // when
            mockMvc.perform(put("/people/{uuid}", uuid)
                            .contentType(APPLICATION_JSON)
                            .content(malformedBody))
                    // then
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$." + ApiError.Fields.errorMessage).exists());

            verify(restExceptionHandler).handleHttpMessageNotReadable(any(HttpMessageNotReadableException.class), any(), any(), any());
        }

        @Test
        void shouldNotThrowException_whenQueryFormedCorrectly() throws Exception {
            // given
            UUID uuid = UUID.fromString("b1983ac8-4069-432b-9f13-39da739b127a");
            String body = JsonReader.jsonFromFile(JSON_FOLDER + "/" + VALID_PERSON_REQUEST);

            Optional<PersonResponse> responseFromService = Optional.of(PersonTestBuilder.aPerson().buildResponse());
            when(personService.update(any(PersonRequest.class), any(UUID.class)))
                    .thenReturn(responseFromService);

            // when
            mockMvc.perform(put("/people/{uuid}", uuid)
                            .contentType(APPLICATION_JSON)
                            .content(body))
                    // then
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$." + ApiError.Fields.errorMessage).doesNotExist())
                    .andExpect(jsonPath("$." + ApiError.Fields.validationErrors).doesNotExist());

            verifyNoInteractions(restExceptionHandler);
        }

        @Test
        void shouldDeserializeCorrectly() throws Exception {
            // given
            UUID houseUUID = UUID.fromString("acb8316d-3d13-4096-b1d6-f997b7307f0e");
            Optional<HouseResponse> notEmptyResponse = Optional.of(
                    HouseTestBuilder.aHouse().buildResponse()
            );

            PersonRequest validPersonRequestDeserialized = PersonRequest.builder()
                    .name("my_name_1")
                    .surname("surname_1")
                    .sex(Sex.MALE)
                    .passportNumber("1234567893332")
                    .passportSeries("LL")
                    .houseOfResidenceUUID(houseUUID)
                    .build();

            doReturn(notEmptyResponse)
                    .when(houseService).findByUUID(houseUUID);

            UUID uuid = UUID.fromString("b1983ac8-4069-432b-9f13-39da739b127a");
            String body = JsonReader.jsonFromFile(JSON_FOLDER + "/" + VALID_PERSON_REQUEST);

            // when, then
            mockMvc.perform(put("/people/{uuid}", uuid)
                    .contentType(APPLICATION_JSON)
                    .content(body));

            verify(personService).update(validPersonRequestDeserialized, uuid);
        }

        @Test
        void shouldRespondWithStatusNotFoundAndEmptyBody_onEmptyOptionalFromPersonService() throws Exception {
            // given
            UUID uuid = UUID.fromString("b1983ac8-4069-432b-9f13-39da739b127a");
            String body = JsonReader.jsonFromFile(JSON_FOLDER + "/" + VALID_PERSON_REQUEST);

            when(personService.update(any(PersonRequest.class), any(UUID.class)))
                    .thenReturn(Optional.empty());

            // when
            mockMvc.perform(put("/people/{uuid}", uuid)
                            .contentType(APPLICATION_JSON)
                            .content(body))
                    // then
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$").doesNotExist());
        }

        @Test
        void shouldRespondWithStatusOkAndCorrectBody_onPresentOptionalFromPersonService() throws Exception {
            // given
            UUID uuid = UUID.fromString("b1983ac8-4069-432b-9f13-39da739b127a");
            String validBody = JsonReader.jsonFromFile(JSON_FOLDER + "/" + VALID_PERSON_REQUEST);

            PersonResponse expectedResponse = PersonTestBuilder.aPerson().buildResponse();

            when(personService.update(any(PersonRequest.class), any(UUID.class)))
                    .thenReturn(Optional.of(expectedResponse));

            // when
            ResultActions actions = mockMvc.perform(put("/people/{uuid}", uuid)
                            .contentType(APPLICATION_JSON)
                            .content(validBody))
                    // then
                    .andExpect(status().isOk());

            andExpectJsonPathToHave(expectedResponse, actions);
        }
    }

    @Nested
    class createPersonTests {
        @Test
        void shouldThrowMethodArgumentNotValidException_onInvalidRequestBody() throws Exception {
            // given
            UUID uuid = UUID.fromString("b1983ac8-4069-432b-9f13-39da739b127a");
            String invalidBody = JsonReader.jsonFromFile(JSON_FOLDER + "/" + INVALID_PERSON_REQUEST);

            // when
            mockMvc.perform(post("/people", uuid)
                            .contentType(APPLICATION_JSON)
                            .content(invalidBody))
                    // then
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$." + ApiError.Fields.errorMessage).exists())
                    .andExpect(jsonPath("$." + ApiError.Fields.validationErrors).exists());

            verify(restExceptionHandler).handleMethodArgumentNotValid(any(MethodArgumentNotValidException.class), any(), any(), any());
        }

        @Test
        void shouldThrowHttpMessageNotReadableException_onMalformedRequestBody() throws Exception {
            // given
            UUID uuid = UUID.fromString("b1983ac8-4069-432b-9f13-39da739b127a");
            String malformedBody = JsonReader.jsonFromFile(JSON_FOLDER + "/" + VALID_PERSON_REQUEST)
                    .replace('"', ' ');

            // when
            mockMvc.perform(post("/people", uuid)
                            .contentType(APPLICATION_JSON)
                            .content(malformedBody))
                    // then
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$." + ApiError.Fields.errorMessage).exists());

            verify(restExceptionHandler).handleHttpMessageNotReadable(any(HttpMessageNotReadableException.class), any(), any(), any());
        }

        @Test
        void shouldNotThrowException_whenQueryFormedCorrectly() throws Exception {
            // given
            UUID uuid = UUID.fromString("b1983ac8-4069-432b-9f13-39da739b127a");
            String body = JsonReader.jsonFromFile(JSON_FOLDER + "/" + VALID_PERSON_REQUEST);

            Optional<PersonResponse> responseFromService = Optional.of(PersonTestBuilder.aPerson().buildResponse());
            when(personService.update(any(PersonRequest.class), any(UUID.class)))
                    .thenReturn(responseFromService);

            // when
            mockMvc.perform(post("/people", uuid)
                            .contentType(APPLICATION_JSON)
                            .content(body))
                    // then
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$." + ApiError.Fields.errorMessage).doesNotExist())
                    .andExpect(jsonPath("$." + ApiError.Fields.validationErrors).doesNotExist());

            verifyNoInteractions(restExceptionHandler);
        }

        @Test
        void shouldDeserializeCorrectly() throws Exception {
            // given
            UUID houseUUID = UUID.fromString("acb8316d-3d13-4096-b1d6-f997b7307f0e");
            Optional<HouseResponse> notEmptyResponse = Optional.of(
                    HouseTestBuilder.aHouse().buildResponse()
            );

            PersonRequest validPersonRequestDeserialized = PersonRequest.builder()
                    .name("my_name_1")
                    .surname("surname_1")
                    .sex(Sex.MALE)
                    .passportNumber("1234567893332")
                    .passportSeries("LL")
                    .houseOfResidenceUUID(houseUUID)
                    .build();

            String body = JsonReader.jsonFromFile(JSON_FOLDER + "/" + VALID_PERSON_REQUEST);

            doReturn(notEmptyResponse)
                    .when(houseService).findByUUID(houseUUID);

            // when, then
            mockMvc.perform(post("/people")
                    .contentType(APPLICATION_JSON)
                    .content(body));

            verify(personService).create(validPersonRequestDeserialized);
        }

        @Test
        void shouldRespondWithStatusCreatedAndCorrectBody_onSuccessfulCreateUsingHouseService() throws Exception {
            // given
            UUID uuid = UUID.fromString("b1983ac8-4069-432b-9f13-39da739b127a");
            String validBody = JsonReader.jsonFromFile(JSON_FOLDER + "/" + VALID_PERSON_REQUEST);

            PersonResponse expectedResponse = PersonTestBuilder.aPerson().buildResponse();

            when(personService.create(any(PersonRequest.class)))
                    .thenReturn(expectedResponse);

            // when
            ResultActions actions = mockMvc.perform(post("/people", uuid)
                            .contentType(APPLICATION_JSON)
                            .content(validBody))
                    // then
                    .andExpect(status().isCreated());

            andExpectJsonPathToHave(expectedResponse, actions);
        }

    }

    private static ResultActions andExpectJsonPathToHave(PersonResponse expectedResponse, ResultActions actions) throws Exception {
        String uuidExpected = expectedResponse.getUuid().toString();
        String nameExpected = expectedResponse.getName();
        String surnameExpected = expectedResponse.getSurname();
        String passportNumberExpected = expectedResponse.getPassportNumber();
        String passportSeriesExpected = expectedResponse.getPassportSeries();
        String sexExpected = expectedResponse.getSex().toString();
        String createDateExpected = expectedResponse.getCreateDate()
                .format(DateTimeFormatter.ofPattern(FormatConstants.DATE_TIME_FORMAT));
        String updateDateExpected = expectedResponse.getUpdateDate()
                .format(DateTimeFormatter.ofPattern(FormatConstants.DATE_TIME_FORMAT));

        String prefix = "$.";
        return actions
                .andExpect(jsonPath(prefix + Person.Fields.uuid).value(uuidExpected))
                .andExpect(jsonPath(prefix + Person.Fields.name).value(nameExpected))
                .andExpect(jsonPath(prefix + Person.Fields.surname).value(surnameExpected))
                .andExpect(jsonPath(prefix + Person.Fields.passportNumber).value(passportNumberExpected))
                .andExpect(jsonPath(prefix + Person.Fields.passportSeries).value(passportSeriesExpected))
                .andExpect(jsonPath(prefix + Person.Fields.sex).value(sexExpected))
                .andExpect(jsonPath(prefix + Person.Fields.createDate).value(createDateExpected))
                .andExpect(jsonPath(prefix + Person.Fields.updateDate).value(updateDateExpected));
    }

    private static ResultActions andExpectedJsonPathToHavePageContentFirstItemLike(HouseResponse expectedResponse, ResultActions actions) throws Exception {
        String uuidExpected = expectedResponse.getUuid().toString();
        String areaExpected = expectedResponse.getArea().toString();
        String countryExpected = expectedResponse.getCountry();
        String cityExpected = expectedResponse.getCity();
        String streetExpected = expectedResponse.getStreet();
        String numberExpected = expectedResponse.getNumber().toString();
        String createDateExpected = expectedResponse.getCreateDate()
                .format(DateTimeFormatter.ofPattern(FormatConstants.DATE_TIME_FORMAT));

        String prefix = "$." + PAGE_CONTENT_JSON_FIELD_NAME + "[0]";
        return actions
                .andExpect(jsonPath(prefix + House.Fields.area).value(areaExpected))
                .andExpect(jsonPath(prefix + House.Fields.uuid).value(uuidExpected))
                .andExpect(jsonPath(prefix + House.Fields.country).value(countryExpected))
                .andExpect(jsonPath(prefix + House.Fields.city).value(cityExpected))
                .andExpect(jsonPath(prefix + House.Fields.street).value(streetExpected))
                .andExpect(jsonPath(prefix + House.Fields.number).value(numberExpected))
                .andExpect(jsonPath(prefix + House.Fields.createDate).value(createDateExpected));
    }

    private static ResultActions andExpectedJsonPathToHavePageContentFirstItemLike(PersonResponse expectedResponse,
                                                                                   ResultActions actions) throws Exception {
        String uuidExpected = expectedResponse.getUuid().toString();
        String nameExpected = expectedResponse.getName();
        String surnameExpected = expectedResponse.getSurname();
        String passportNumberExpected = expectedResponse.getPassportNumber();
        String passportSeriesExpected = expectedResponse.getPassportSeries();
        String sexExpected = expectedResponse.getSex().toString();
        String createDateExpected = expectedResponse.getCreateDate()
                .format(DateTimeFormatter.ofPattern(FormatConstants.DATE_TIME_FORMAT));
        String updateDateExpected = expectedResponse.getUpdateDate()
                .format(DateTimeFormatter.ofPattern(FormatConstants.DATE_TIME_FORMAT));

        String prefix = "$." + PAGE_CONTENT_JSON_FIELD_NAME + "[0]";
        return actions
                .andExpect(jsonPath(prefix + Person.Fields.uuid).value(uuidExpected))
                .andExpect(jsonPath(prefix + Person.Fields.name).value(nameExpected))
                .andExpect(jsonPath(prefix + Person.Fields.surname).value(surnameExpected))
                .andExpect(jsonPath(prefix + Person.Fields.passportNumber).value(passportNumberExpected))
                .andExpect(jsonPath(prefix + Person.Fields.passportSeries).value(passportSeriesExpected))
                .andExpect(jsonPath(prefix + Person.Fields.sex).value(sexExpected))
                .andExpect(jsonPath(prefix + Person.Fields.createDate).value(createDateExpected))
                .andExpect(jsonPath(prefix + Person.Fields.updateDate).value(updateDateExpected));
    }
}

