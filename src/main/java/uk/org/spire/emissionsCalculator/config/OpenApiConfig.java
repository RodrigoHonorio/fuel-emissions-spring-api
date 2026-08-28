package uk.org.spire.emissionsCalculator.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("S.P.I.R.E. - Spatial Prediction & Emissions API")
                        .version("1.0.0")
                        .description("Enterprise-grade API for spatial prediction and VOC emissions calculation. Built on CETESB methodologies and integrated with Transport for London (TfL) real-time air quality data for public health (NHS) monitoring."));
    }
}