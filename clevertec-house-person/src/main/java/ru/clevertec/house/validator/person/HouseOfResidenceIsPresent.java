package ru.clevertec.house.validator.person;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = HouseOfResidenceIsPresentValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface HouseOfResidenceIsPresent {
    String message() default "the specified house of residence does not exist";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
