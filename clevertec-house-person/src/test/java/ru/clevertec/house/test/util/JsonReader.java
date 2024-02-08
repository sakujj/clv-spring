package ru.clevertec.house.test.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.experimental.UtilityClass;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@UtilityClass
public class JsonReader {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        objectMapper.registerModule(javaTimeModule);
    }

    public static String jsonFromFile(String filePath) throws RuntimeException {
        try {
            File resource = new ClassPathResource(filePath).getFile();
            byte[] byteArray = Files.readAllBytes(resource.toPath());

            return new String(byteArray);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
