package io.github.sakujj.configprops;

import io.github.sakujj.config.CacheAutoConfiguration;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = CacheAutoConfiguration.PREFIX)
@Data
public class CacheProperties {
    private String type;
    private Integer capacity;
}
