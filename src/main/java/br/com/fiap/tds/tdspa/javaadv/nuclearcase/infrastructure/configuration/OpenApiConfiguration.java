package br.com.fiap.tds.tdspa.javaadv.nuclearcase.infrastructure.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {
    @Bean
    public OpenAPI nuclearCaseOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Nuclear Case API")
                .version("0.0.1")
                .description("API educativa de casos investigativos sobre tecnologia nuclear. "
                        + "CRUD de casos investigativos educativos."));
    }
}
