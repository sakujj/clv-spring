package ru.clevertec.house.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ControllerConstants {
    public static final String PAGE_NUMBER_PARAMETER_NAME = "page";
    public static final String PAGE_SIZE_PARAMETER_NAME = "size";

    public static final int FIRST_PAGE_NUMBER = 0;
    public static final int MAX_PAGE_SIZE = 250;
    public static final int MIN_PAGE_SIZE = 1;

    public static final String DEFAULT_PAGE_SIZE = "15";
}
