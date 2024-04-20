package ru.clevertec.house.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import ru.clevertec.house.enumeration.Sex;

import java.util.Arrays;

@Converter
public class SexConverter implements AttributeConverter<Sex, String> {
    @Override
    public String convertToDatabaseColumn(Sex attribute) {
        if (attribute == null) {
            return null;
        }

        return attribute.toDbString();
    }

    @Override
    public Sex convertToEntityAttribute(String dbData) {
        return Arrays.stream(Sex.values())
                .map(Sex::toDbString)
                .filter(v -> v.equals(dbData))
                .map(Sex::fromDbString)
                .findAny()
                .orElseThrow(IllegalArgumentException::new);
    }
}
