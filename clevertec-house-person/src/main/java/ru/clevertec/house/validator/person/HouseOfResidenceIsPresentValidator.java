package ru.clevertec.house.validator.person;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.clevertec.house.service.HouseService;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class HouseOfResidenceIsPresentValidator implements ConstraintValidator<HouseOfResidenceIsPresent, UUID> {

    private final HouseService houseService;

    @Override
    public boolean isValid(UUID value, ConstraintValidatorContext context) {
        return houseService.findByUUID(value).isPresent();
    }
}
