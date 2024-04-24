package com.kazmiruk.clearsolution.model.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "user")
@Validated
@Getter
@Setter
public class UserProperties {

    @Min(1)
    @NotNull
    private Integer age;

}
