package uk.co.pepper.companysearch.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestClient;

@Configuration
public class AppConfig {

    @Value("${endpoint}")
    private String endpoint;

    @Value("${wiremock.port}")
    private int port;

    @Bean
    public RestClient restClient() {
        return RestClient.create(endpoint);
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    @Profile("!test")
    public WireMockServer wireMockServer() {
        return new WireMockServer(WireMockConfiguration.wireMockConfig()
                .port(port)
                .usingFilesUnderClasspath("wiremock/company-search-mock"));
    }
}
