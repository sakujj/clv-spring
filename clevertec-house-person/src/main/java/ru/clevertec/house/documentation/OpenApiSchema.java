package ru.clevertec.house.documentation;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import lombok.experimental.UtilityClass;
import ru.clevertec.house.constant.FormatConstants;
import ru.clevertec.house.dto.Constraints;

@OpenAPIDefinition(
        info = @Info(
                contact = @Contact(
                        name = "Pavel Rysnik",
                        url = "https://github.com/sakujj",
                        email = "pavelrysnik@gmail.com"
                ),
                description = "Houses REST-service documentation",
                title = "OpenApi | Houses REST-service documentation",
                version = "1.0"
        )
)
@UtilityClass
public class OpenApiSchema {

    public static class Examples {

        public static class HouseDTO {

            public static final String UUID_EXAMPLE = "acb8316d-3d13-4096-b1d6-f997b7307f0e";
            public static final String AREA_EXAMPLE = "66.5";
            public static final String NUMBER_EXAMPLE = "13";
            public static final String STREET_EXAMPLE = "Bäckerbreitergang";
            public static final String CITY_EXAMPLE = "Hamburg";
            public static final String COUNTRY_EXAMPLE = "Germany";
        }

        public static class PersonDTO {

            public static final String UUID_EXAMPLE = "e95a9fd0-b305-4b0f-acec-993015fa8035";
            public static final String NAME_EXAMPLE = "Vladimir";
            public static final String SURNAME_EXAMPLE = "Ulyanov";
            public static final String PASSPORT_SERIES_EXAMPLE = "XX";
            public static final String PASSPORT_NUMBER_EXAMPLE = "0123456789012";
            public static final String SEX_EXAMPLE = "MALE";
            public static final String HOUSE_OF_RESIDENCE_UUID_EXAMPLE = HouseDTO.UUID_EXAMPLE;
        }
    }

    public static class Minimums {

        public static class HouseDTO {

            public static final String AREA_EXCLUSIVE_MINIMUM = "0";
            public static final String NUMBER_EXCLUSIVE_MINIMUM = "0";
        }
    }

    public static class Descriptions {

        public static final String NOT_BLANK_DESCRIPTION = "not blank";
    }

    public static class Patterns {

        public static final String DATE_TIME_FORMAT = FormatConstants.DATE_TIME_FORMAT;

        public static class PersonDTO {

            public static final String PASSPORT_SERIES_PATTERN = Constraints.Patterns.PersonDTO.PASSPORT_SERIES_PATTERN;
            public static final String PASSPORT_NUMBER_PATTERN = Constraints.Patterns.PersonDTO.PASSPORT_NUMBER_PATTERN;
        }
    }

}
