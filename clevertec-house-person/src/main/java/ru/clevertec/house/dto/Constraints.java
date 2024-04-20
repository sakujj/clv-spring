package ru.clevertec.house.dto;

public class Constraints {

    public static class Patterns {

        public static class PersonDTO {

            public static final String PASSPORT_SERIES_PATTERN = "[A-Z]{2}";
            public static final String PASSPORT_NUMBER_PATTERN = "[0-9]{13}";
        }
    }
}
