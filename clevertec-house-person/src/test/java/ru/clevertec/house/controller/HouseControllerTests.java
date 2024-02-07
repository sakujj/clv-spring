package ru.clevertec.house.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.clevertec.house.constant.ControllerConstants;
import ru.clevertec.house.constant.FormatConstants;
import ru.clevertec.house.dto.HouseRequest;
import ru.clevertec.house.dto.HouseResponse;
import ru.clevertec.house.dto.PersonResponse;
import ru.clevertec.house.entity.House;
import ru.clevertec.house.entity.Person;
import ru.clevertec.house.exception.ApiError;
import ru.clevertec.house.exception.RestExceptionHandler;
import ru.clevertec.house.service.HouseHistoryService;
import ru.clevertec.house.service.HouseService;
import ru.clevertec.house.service.PersonService;
import ru.clevertec.house.test.util.HouseTestBuilder;
import ru.clevertec.house.test.util.JsonReader;
import ru.clevertec.house.test.util.PersonTestBuilder;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
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

@WebMvcTest
@ActiveProfiles("test")
class HouseControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HouseHistoryService houseHistoryService;

    @MockBean
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

    private static final String JSON_FOLDER = "JSONs/House";
    private static final String VALID_HOUSE_REQUEST = "validHouseRequest.json";
    private static final String INVALID_HOUSE_REQUEST = "invalidHouseRequest.json";


    @Nested
    class findHouseByUuidTests {

        @Test
        void shouldThrowTypeMismatchException_onInvalidUUIDVersion4InTheURL() throws Exception {
            // given
            String malformedUUID = "malformed-uuid-v4-possible-content";

            // when
            mockMvc.perform(get("/houses/{uuid}", malformedUUID)
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
            when(houseService.findByUUID(uuid))
                    .thenReturn(Optional.of(HouseResponse.builder().build()));

            // when
            mockMvc.perform(get("/houses/{uuid}", uuid)
                            .contentType(APPLICATION_JSON))
                    // then
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").exists())
                    .andExpect(jsonPath("$." + ApiError.Fields.errorMessage).doesNotExist())
                    .andExpect(jsonPath("$." + ApiError.Fields.validationErrors).doesNotExist());

            verifyNoInteractions(restExceptionHandler);
        }

        @Test
        void shouldRespondWithStatusOkAndCorrectBody_onSuccessfulFindByUUIDUsingHouseService() throws Exception {
            // given
            UUID uuid = UUID.fromString("709cf39e-a60a-451a-a5bb-4ac832c6e6b4");
            HouseResponse expectedResponse = HouseTestBuilder.aHouse().buildResponse();

            when(houseService.findByUUID(uuid))
                    .thenReturn(Optional.of(expectedResponse));

            //when
            ResultActions actions = mockMvc.perform(get("/houses/{uuid}", uuid)
                            .contentType(APPLICATION_JSON))
                    // then
                    .andExpect(status().isOk());

            andExpectJsonPathToHave(expectedResponse, actions);
        }

        @Test
        void shouldReturnNotFoundStatusAndEmptyBody_whenEmptyOptionalFromHouseServiceIsReturned() throws Exception {
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
    class findAllHousesTests {

        @Test
        void shouldThrowValidationException_onPageParameterLessThanNumberOfFirstPage() throws Exception {
            // given, when
            mockMvc.perform(get("/houses")
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
            mockMvc.perform(get("/houses")
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
            when(houseService.findAll(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of()));

            // when
            mockMvc.perform(get("/houses")
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

            HouseResponse expectedResponse = HouseTestBuilder.aHouse().buildResponse();

            List<HouseResponse> expectedContent = List.of(expectedResponse);
            when(houseService.findAll(expectedPageRequest))
                    .thenReturn(new PageImpl<>(expectedContent));

            // when
            ResultActions actions = mockMvc.perform(get("/houses")
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

            verify(houseService).findAll(expectedPageRequest);
        }
    }

    @Nested
    class findAllEverOwnersByHouseUUIDTests {

        @Test
        void shouldThrowTypeMismatchException_onInvalidUUIDVersion4InTheURL() throws Exception {
            // given
            String malformedUUID = "malformed-uuid-v4-possible-content";

            // when
            mockMvc.perform(get("/houses/{uuid}/ever-owners", malformedUUID)
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
            mockMvc.perform(get("/houses/{uuid}/ever-owners", uuid)
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
            mockMvc.perform(get("/houses/{uuid}/ever-owners", uuid)
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
            mockMvc.perform(get("/houses/{uuid}/ever-owners", uuid)
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

            when(houseHistoryService.findAllPeopleThatOwnedHouseByHouseUuid(eq(uuid), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of()));

            // when
            mockMvc.perform(get("/houses/{uuid}/ever-owners", uuid)
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

            PersonResponse expectedResponse = PersonTestBuilder.aPerson().buildResponse();

            List<PersonResponse> expectedContent = List.of(expectedResponse);
            when(houseHistoryService.findAllPeopleThatOwnedHouseByHouseUuid(uuid, expectedPageRequest))
                    .thenReturn(new PageImpl<>(expectedContent));

            // when
            ResultActions actions = mockMvc.perform(get("/houses/{uuid}/ever-owners", uuid)
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

            verify(houseHistoryService).findAllPeopleThatOwnedHouseByHouseUuid(uuid, expectedPageRequest);
        }
    }

    @Nested
    class findAllEverResidentsByHouseUUIDTests {

        @Test
        void shouldThrowTypeMismatchException_onInvalidUUIDVersion4InTheURL() throws Exception {
            // given
            String malformedUUID = "malformed-uuid-v4-possible-content";

            // when
            mockMvc.perform(get("/houses/{uuid}/ever-residents", malformedUUID)
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
            mockMvc.perform(get("/houses/{uuid}/ever-residents", uuid)
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
            mockMvc.perform(get("/houses/{uuid}/ever-residents", uuid)
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
            mockMvc.perform(get("/houses/{uuid}/ever-residents", uuid)
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

            when(houseHistoryService.findAllPeopleThatLivedInHouseByHouseUuid(eq(uuid), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of()));

            // when
            mockMvc.perform(get("/houses/{uuid}/ever-residents", uuid)
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

            PersonResponse expectedResponse = PersonTestBuilder.aPerson().buildResponse();

            List<PersonResponse> expectedContent = List.of(expectedResponse);
            when(houseHistoryService.findAllPeopleThatLivedInHouseByHouseUuid(uuid, expectedPageRequest))
                    .thenReturn(new PageImpl<>(expectedContent));

            // when
            ResultActions actions = mockMvc.perform(get("/houses/{uuid}/ever-residents", uuid)
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

            verify(houseHistoryService).findAllPeopleThatLivedInHouseByHouseUuid(uuid, expectedPageRequest);
        }
    }

    @Nested
    class findAllResidentsByHouseUUIDTests {

        @Test
        void shouldThrowTypeMismatchException_onInvalidUUIDVersion4InTheURL() throws Exception {
            // given
            String malformedUUID = "malformed-uuid-v4-possible-content";

            // when
            mockMvc.perform(get("/houses/{uuid}/residents", malformedUUID)
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
            mockMvc.perform(get("/houses/{uuid}/residents", uuid)
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
            mockMvc.perform(get("/houses/{uuid}/residents", uuid)
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
            mockMvc.perform(get("/houses/{uuid}/residents", uuid)
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

            when(personService.findAllResidentsByHouseOfResidenceUUID(eq(uuid), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of()));

            // when
            mockMvc.perform(get("/houses/{uuid}/residents", uuid)
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

            PersonResponse expectedResponse = PersonTestBuilder.aPerson().buildResponse();

            List<PersonResponse> expectedContent = List.of(expectedResponse);
            when(personService.findAllResidentsByHouseOfResidenceUUID(uuid, expectedPageRequest))
                    .thenReturn(new PageImpl<>(expectedContent));

            // when
            ResultActions actions = mockMvc.perform(get("/houses/{uuid}/residents", uuid)
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

            verify(personService).findAllResidentsByHouseOfResidenceUUID(uuid, expectedPageRequest);
        }
    }

    @Nested
    class deleteByUUIDTests {

        @Test
        void shouldThrowTypeMismatchException_onInvalidUUIDVersion4InTheURL() throws Exception {
            // given
            String malformedUUID = "malformed-uuid-v4-possible-content";

            // when
            mockMvc.perform(delete("/houses/{uuid}", malformedUUID)
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

            when(houseService.deleteByUUID(uuid))
                    .thenReturn(expected);

            // when
            mockMvc.perform(delete("/houses/{uuid}", uuid)
                            .contentType(APPLICATION_JSON))
                    // then
                    .andExpect(status().isNoContent())
                    .andExpect(jsonPath("$").doesNotExist());

            verifyNoInteractions(restExceptionHandler);
        }

        @Test
        void shouldRespondWithStatusNoContentAndEmptyBody_onSuccessfulDeleteUsingHouseService() throws Exception {
            // given
            UUID uuid = UUID.fromString("b1983ac8-4069-432b-9f13-39da739b127a");
            long expected = 1L;

            when(houseService.deleteByUUID(uuid))
                    .thenReturn(expected);

            // when
            mockMvc.perform(delete("/houses/{uuid}", uuid)
                            .contentType(APPLICATION_JSON))
                    // then
                    .andExpect(status().isNoContent())
                    .andExpect(jsonPath("$").doesNotExist());

            verify(houseService).deleteByUUID(uuid);
        }
    }

    @Nested
    class updateByHouseUUIDTests {

        @Test
        void shouldThrowTypeMismatchException_onInvalidUUIDVersion4InTheURL() throws Exception {
            // given
            String malformedUUID = "malformed-uuid-v4-possible-content";
            String body = JsonReader.jsonFromFile(JSON_FOLDER + "/" + VALID_HOUSE_REQUEST);

            // when
            mockMvc.perform(put("/houses/{uuid}", malformedUUID)
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
            String invalidBody = JsonReader.jsonFromFile(JSON_FOLDER + "/" + INVALID_HOUSE_REQUEST);

            // when
            mockMvc.perform(put("/houses/{uuid}", uuid)
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
            String malformedBody = JsonReader.jsonFromFile(JSON_FOLDER + "/" + VALID_HOUSE_REQUEST)
                    .replace('"', ' ');

            // when
            mockMvc.perform(put("/houses/{uuid}", uuid)
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
            String body = JsonReader.jsonFromFile(JSON_FOLDER + "/" + VALID_HOUSE_REQUEST);

            Optional<HouseResponse> responseFromService = Optional.of(HouseTestBuilder.aHouse().buildResponse());
            when(houseService.update(any(HouseRequest.class), any(UUID.class)))
                    .thenReturn(responseFromService);

            // when
            mockMvc.perform(put("/houses/{uuid}", uuid)
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
            HouseRequest validHouseRequestDeserialized = HouseRequest.builder()
                    .area(125.5)
                    .country("Germany")
                    .city("Hamburg")
                    .street("Bäckerbreitergang")
                    .number(29)
                    .build();

            UUID uuid = UUID.fromString("b1983ac8-4069-432b-9f13-39da739b127a");
            String body = JsonReader.jsonFromFile(JSON_FOLDER + "/" + VALID_HOUSE_REQUEST);

            mockMvc.perform(put("/houses/{uuid}", uuid)
                    .contentType(APPLICATION_JSON)
                    .content(body));

            verify(houseService).update(validHouseRequestDeserialized, uuid);
        }

        @Test
        void shouldRespondWithStatusNotFoundAndEmptyBody_onEmptyOptionalFromHouseService() throws Exception {
            // given
            UUID uuid = UUID.fromString("b1983ac8-4069-432b-9f13-39da739b127a");
            String body = JsonReader.jsonFromFile(JSON_FOLDER + "/" + VALID_HOUSE_REQUEST);

            when(houseService.update(any(HouseRequest.class), any(UUID.class)))
                    .thenReturn(Optional.empty());

            // when
            mockMvc.perform(put("/houses/{uuid}", uuid)
                            .contentType(APPLICATION_JSON)
                            .content(body))
                    // then
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$").doesNotExist());
        }

        @Test
        void shouldRespondWithStatusOkAndCorrectBody_onPresentOptionalFromHouseService() throws Exception {
            // given
            UUID uuid = UUID.fromString("b1983ac8-4069-432b-9f13-39da739b127a");
            String validBody = JsonReader.jsonFromFile(JSON_FOLDER + "/" + VALID_HOUSE_REQUEST);

            HouseResponse expectedResponse = HouseTestBuilder.aHouse().buildResponse();

            when(houseService.update(any(HouseRequest.class), any(UUID.class)))
                    .thenReturn(Optional.of(expectedResponse));

            // when
            ResultActions actions = mockMvc.perform(put("/houses/{uuid}", uuid)
                            .contentType(APPLICATION_JSON)
                            .content(validBody))
                    // then
                    .andExpect(status().isOk());

            andExpectJsonPathToHave(expectedResponse, actions);
        }

    }

    @Nested
    class addNewOwnerToHouseTests {

        @Test
        void shouldThrowTypeMismatchException_onInvalidUUIDVersion4InTheURL() throws Exception {
            // given
            String malformedUUID = "malformed-uuid-v4-possible-content";
            String body = "\"62e3dd89-8823-479d-8f84-f1c8457f713d\"";

            // when
            mockMvc.perform(put("/houses/{uuid}/add-owner", malformedUUID)
                            .contentType(APPLICATION_JSON)
                            .content(body))
                    // then
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$." + ApiError.Fields.errorMessage).exists());

            verify(restExceptionHandler).handleTypeMismatch(any(TypeMismatchException.class), any(), any(), any());
        }

        @Test
        void shouldThrowMessageNotReadableException_onMalformedRequestBody() throws Exception {
            // given
            UUID uuid = UUID.fromString("b1983ac8-4069-432b-9f13-39da739b127a");
            String invalidBody = "invalid-uuid-v4";

            // when
            mockMvc.perform(put("/houses/{uuid}/add-owner", uuid)
                            .contentType(APPLICATION_JSON)
                            .content(invalidBody))
                    // then
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$." + ApiError.Fields.errorMessage).exists());

            verify(restExceptionHandler).handleHttpMessageNotReadable(any(HttpMessageNotReadableException.class), any(), any(), any());
        }

        @Test
        void shouldThrowServiceException_onHouseServiceError() throws Exception {
            // given
            UUID uuid = UUID.fromString("b1983ac8-4069-432b-9f13-39da739b127a");
            String body = "\"62e3dd89-8823-479d-8f84-f1c8457f713d\"";

            doThrow(new RuntimeException("error msg"))
                    .when(houseService).addNewOwnerToHouse(any(UUID.class), any(UUID.class));

            // when
            mockMvc.perform(put("/houses/{uuid}/add-owner", uuid)
                            .contentType(APPLICATION_JSON)
                            .content(body))
                    // then
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$." + ApiError.Fields.errorMessage).exists());

            verify(restExceptionHandler).handleServiceException(any(RuntimeException.class));
        }

        @Test
        void shouldNotThrowException_whenQueryFormedCorrectly() throws Exception {
            // given
            UUID uuid = UUID.fromString("b1983ac8-4069-432b-9f13-39da739b127a");
            String body = "\"62e3dd89-8823-479d-8f84-f1c8457f713d\"";

            doNothing()
                    .when(houseService).addNewOwnerToHouse(any(UUID.class), any(UUID.class));

            // when
            mockMvc.perform(put("/houses/{uuid}/add-owner", uuid)
                            .contentType(APPLICATION_JSON)
                            .content(body))
                    // then
                    .andExpect(status().isNoContent())
                    .andExpect(jsonPath("$." + ApiError.Fields.errorMessage).doesNotExist())
                    .andExpect(jsonPath("$." + ApiError.Fields.validationErrors).doesNotExist());

            verifyNoInteractions(restExceptionHandler);
        }

        @Test
        void shouldDeserializeCorrectly() throws Exception {
            UUID uuid = UUID.fromString("b1983ac8-4069-432b-9f13-39da739b127a");

            UUID expectedToBeDeserialized = UUID.fromString("62e3dd89-8823-479d-8f84-f1c8457f713d");
            String body = "\"62e3dd89-8823-479d-8f84-f1c8457f713d\"";

            mockMvc.perform(put("/houses/{uuid}/add-owner", uuid)
                    .contentType(APPLICATION_JSON)
                    .content(body));

            verify(houseService).addNewOwnerToHouse(uuid, expectedToBeDeserialized);
        }

        @Test
        void shouldRespondWithStatusNoContentAndEmptyBody_onSuccessfulOperationUsingHouseService() throws Exception {
            // given
            UUID uuid = UUID.fromString("b1983ac8-4069-432b-9f13-39da739b127a");
            String validBody = "\"62e3dd89-8823-479d-8f84-f1c8457f713d\"";

            doNothing()
                    .when(houseService).addNewOwnerToHouse(any(UUID.class), any(UUID.class));

            // when
            ResultActions actions = mockMvc.perform(put("/houses/{uuid}/add-owner", uuid)
                            .contentType(APPLICATION_JSON)
                            .content(validBody))
                    // then
                    .andExpect(status().isNoContent());
        }
    }

    @Nested
    class createHouseTests {
        @Test
        void shouldThrowMethodArgumentNotValidException_onInvalidRequestBody() throws Exception {
            // given
            UUID uuid = UUID.fromString("b1983ac8-4069-432b-9f13-39da739b127a");
            String invalidBody = JsonReader.jsonFromFile(JSON_FOLDER + "/" + INVALID_HOUSE_REQUEST);

            // when
            mockMvc.perform(post("/houses", uuid)
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
            String malformedBody = JsonReader.jsonFromFile(JSON_FOLDER + "/" + VALID_HOUSE_REQUEST)
                    .replace('"', ' ');

            // when
            mockMvc.perform(post("/houses", uuid)
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
            String body = JsonReader.jsonFromFile(JSON_FOLDER + "/" + VALID_HOUSE_REQUEST);

            Optional<HouseResponse> responseFromService = Optional.of(HouseTestBuilder.aHouse().buildResponse());
            when(houseService.update(any(HouseRequest.class), any(UUID.class)))
                    .thenReturn(responseFromService);

            // when
            mockMvc.perform(post("/houses", uuid)
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
            HouseRequest validHouseRequestDeserialized = HouseRequest.builder()
                    .area(125.5)
                    .country("Germany")
                    .city("Hamburg")
                    .street("Bäckerbreitergang")
                    .number(29)
                    .build();

            String body = JsonReader.jsonFromFile(JSON_FOLDER + "/" + VALID_HOUSE_REQUEST);

            mockMvc.perform(post("/houses")
                    .contentType(APPLICATION_JSON)
                    .content(body));

            verify(houseService).create(validHouseRequestDeserialized);
        }

        @Test
        void shouldRespondWithStatusCreatedAndCorrectBody_onSuccessfulCreateUsingHouseService() throws Exception {
            // given
            UUID uuid = UUID.fromString("b1983ac8-4069-432b-9f13-39da739b127a");
            String validBody = JsonReader.jsonFromFile(JSON_FOLDER + "/" + VALID_HOUSE_REQUEST);

            HouseResponse expectedResponse = HouseTestBuilder.aHouse().buildResponse();

            when(houseService.create(any(HouseRequest.class)))
                    .thenReturn(expectedResponse);

            // when
            ResultActions actions = mockMvc.perform(post("/houses", uuid)
                            .contentType(APPLICATION_JSON)
                            .content(validBody))
                    // then
                    .andExpect(status().isCreated());

            andExpectJsonPathToHave(expectedResponse, actions);
        }

    }

    private static ResultActions andExpectJsonPathToHave(HouseResponse expectedResponse, ResultActions actions) throws Exception {
        String uuidExpected = expectedResponse.getUuid().toString();
        String areaExpected = expectedResponse.getArea().toString();
        String countryExpected = expectedResponse.getCountry();
        String cityExpected = expectedResponse.getCity();
        String streetExpected = expectedResponse.getStreet();
        String numberExpected = expectedResponse.getNumber().toString();
        String createDateExpected = expectedResponse.getCreateDate()
                .format(DateTimeFormatter.ofPattern(FormatConstants.DATE_TIME_FORMAT));

        return actions.andExpect(jsonPath("$." + House.Fields.area).value(areaExpected))
                .andExpect(jsonPath("$." + House.Fields.uuid).value(uuidExpected))
                .andExpect(jsonPath("$." + House.Fields.country).value(countryExpected))
                .andExpect(jsonPath("$." + House.Fields.city).value(cityExpected))
                .andExpect(jsonPath("$." + House.Fields.street).value(streetExpected))
                .andExpect(jsonPath("$." + House.Fields.number).value(numberExpected))
                .andExpect(jsonPath("$." + House.Fields.createDate).value(createDateExpected));
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
