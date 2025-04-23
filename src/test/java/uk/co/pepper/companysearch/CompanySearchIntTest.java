package uk.co.pepper.companysearch;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import uk.co.pepper.companysearch.model.Address;
import uk.co.pepper.companysearch.model.Company;
import uk.co.pepper.companysearch.model.CompanySearchResponse;
import uk.co.pepper.companysearch.model.Officer;
import uk.co.pepper.companysearch.model.SearchRequest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
@EnableWireMock({
        @ConfigureWireMock(
                name = "company-search-mock",
                port = 8090)
})
@ActiveProfiles("test")
public class CompanySearchIntTest {

    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    public void setup() {
        mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @DisplayName("Can find company for a search request with company number and name")
    @Test
    public void searchCompaniesByNumberAndNameSuccess() throws Exception {
        final MvcResult result = mockMvc
                .perform(post("http://localhost:9000/api/companies/search")
                        .content(mapper.writeValueAsString(new SearchRequest("BBC LIMITED", "06500244")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("x-api-key", "mock-api-key"))
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        final String responseJson = formatJson(result.getResponse().getContentAsString());
        final String expectedResponseJson = formatJson(readJsonFile("src/test/resources/example-response.json"));

        assertEquals(expectedResponseJson, responseJson);
        final CompanySearchResponse companySearchResponse = asCompanySearchResponse(responseJson);
        final Company company = companySearchResponse.items().get(0);
        final Address companyAddress = company.address();
        final Officer officer = company.officers().get(0);
        final Address officerAddress = officer.address();

        assertEquals("06500244", company.companyNumber());
        assertEquals("active", company.companyStatus());
        assertEquals("ltd", company.companyType());
        assertEquals("BBC LIMITED", company.title());
        assertEquals("2008-02-11", company.dateOfCreation());
        assertEquals("Boswell Cottage Main Street", companyAddress.premises());
        assertEquals("Retford", companyAddress.locality());
        assertEquals("North Leverton", companyAddress.addressLine1());
        assertEquals("England", companyAddress.country());
        assertEquals("DN22 0AD", companyAddress.postalCode());

        assertEquals("BOXALL, Sarah Victoria", officer.name());
        assertEquals("secretary", officer.officerRole());
        assertEquals("2008-02-11", officer.appointedOn());
        assertEquals("5", officerAddress.premises());
        assertEquals("London", officerAddress.locality());
        assertEquals("Cranford Close", officerAddress.addressLine1());
        assertEquals("England", officerAddress.country());
        assertEquals("SW20 0DP", officerAddress.postalCode());
    }

    @DisplayName("Can find company for a search request with company name only")
    @Test
    public void searchCompaniesByNameSuccess() throws Exception {
        final MvcResult result = mockMvc
                .perform(post("http://localhost:9000/api/companies/search")
                        .content(mapper.writeValueAsString(new SearchRequest("BBC LIMITED", null)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("x-api-key", "mock-api-key"))
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        final String responseJson = formatJson(result.getResponse().getContentAsString());
        final String expectedResponseJson = formatJson(readJsonFile("src/test/resources/example-response.json"));

        assertEquals(expectedResponseJson, responseJson);
        final CompanySearchResponse companySearchResponse = asCompanySearchResponse(responseJson);
        final Company company = companySearchResponse.items().get(0);
        final Address companyAddress = company.address();
        final Officer officer = company.officers().get(0);
        final Address officerAddress = officer.address();

        assertEquals("06500244", company.companyNumber());
        assertEquals("active", company.companyStatus());
        assertEquals("ltd", company.companyType());
        assertEquals("BBC LIMITED", company.title());
        assertEquals("2008-02-11", company.dateOfCreation());
        assertEquals("Boswell Cottage Main Street", companyAddress.premises());
        assertEquals("Retford", companyAddress.locality());
        assertEquals("North Leverton", companyAddress.addressLine1());
        assertEquals("England", companyAddress.country());
        assertEquals("DN22 0AD", companyAddress.postalCode());

        assertEquals("BOXALL, Sarah Victoria", officer.name());
        assertEquals("secretary", officer.officerRole());
        assertEquals("2008-02-11", officer.appointedOn());
        assertEquals("5", officerAddress.premises());
        assertEquals("London", officerAddress.locality());
        assertEquals("Cranford Close", officerAddress.addressLine1());
        assertEquals("England", officerAddress.country());
        assertEquals("SW20 0DP", officerAddress.postalCode());
    }

    @DisplayName("404 returned for a search request non-existent company number")
    @Test
    public void searchCompaniesByNumberNotFound() throws Exception {
        final MvcResult result = mockMvc
                .perform(post("http://localhost:9000/api/companies/search")
                        .content(mapper.writeValueAsString(new SearchRequest(null, "99999999")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("x-api-key", "mock-api-key"))
                .andExpect(status().isNotFound())
                .andReturn();

        assertEquals(HttpStatus.NOT_FOUND.value(), result.getResponse().getStatus());
    }

    @DisplayName("400 (bad request) returned for a search request with no company name or number")
    @Test
    public void searchCompaniesByNumberAndNameBadRequest() throws Exception {
        final MvcResult result = mockMvc.perform(post("http://localhost:9000/api/companies/search")
                        .content(mapper.writeValueAsString(new SearchRequest(null, null)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("x-api-key", "mock-api-key"))
                .andExpect(status().isBadRequest())
                .andReturn();
        assertEquals("Company name or number must be provided in the request body", result.getResponse().getContentAsString());
    }

    @DisplayName("400 (bad request) returned for a search request with no api key")
    @Test
    public void searchCompaniesNoApiKeyBadRequest() throws Exception {
        final MvcResult result = mockMvc
                .perform(post("http://localhost:9000/api/companies/search")
                        .content(mapper.writeValueAsString(new SearchRequest(null, "06500244")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
        final String responseJson = formatJson(result.getResponse().getContentAsString());

        assertTrue(responseJson.contains("Required header 'x-api-key' is not present."));
    }

    @DisplayName("400 (bad request) returned for a search request with blank api key header")
    @Test
    public void searchCompaniesBlankApiKeyBadRequest() throws Exception {
        final MvcResult result = mockMvc
                .perform(post("http://localhost:9000/api/companies/search")
                        .content(mapper.writeValueAsString(new SearchRequest(null, "06500244")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("x-api-key", ""))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertEquals("API key must be provided in the request header", result.getResponse().getContentAsString());
    }

    @DisplayName("403 (forbidden) returned for a search request with invalid api key header")
    @Test
    public void searchCompaniesInvalidApiKeyForbidden() throws Exception {
        final MvcResult result = mockMvc
                .perform(post("http://localhost:9000/api/companies/search")
                        .content(mapper.writeValueAsString(new SearchRequest(null, "06500244")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("x-api-key", "wrong-api-key"))
                .andExpect(status().isForbidden())
                .andReturn();

        assertEquals("Unable to authenticate", result.getResponse().getContentAsString());
    }

    private CompanySearchResponse asCompanySearchResponse(final String json) throws IOException {
        return mapper.readValue(json, new TypeReference<>() {
        });
    }

    private String formatJson(final String json) throws IOException {
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapper.readValue(json, Object.class));
    }

    private String readJsonFile(final String fileName) throws IOException {
        final Path path = Path.of(fileName);

        try (final var lines = Files.lines(path, StandardCharsets.UTF_8)) {
            return lines.collect(Collectors.joining(System.lineSeparator()));
        }
    }
}