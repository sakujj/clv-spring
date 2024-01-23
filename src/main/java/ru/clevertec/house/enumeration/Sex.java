package ru.clevertec.house.enumeration;

import java.util.Map;

public enum Sex {

    MALE, FEMALE;

    public static final Map<Sex, String> sexToDbString = Map.of(
            MALE, "M",
            FEMALE, "W"
    );

    public static final Map<String, Sex> dbStringToSex = Map.of(
            "M", MALE,
            "W", FEMALE
    );

    public String toDbString() {
        return sexToDbString.get(this);
    }

    public static Sex fromDbString(String dbString) {
        return dbStringToSex.get(dbString);
    }
}
