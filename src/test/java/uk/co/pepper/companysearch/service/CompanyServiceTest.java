package uk.co.pepper.companysearch.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.WebApplicationContext;
import uk.co.pepper.companysearch.api.Address;
import uk.co.pepper.companysearch.api.CompaniesApiResponse;
import uk.co.pepper.companysearch.api.Company;
import uk.co.pepper.companysearch.api.Officer;
import uk.co.pepper.companysearch.api.OfficersApiResponse;
import uk.co.pepper.companysearch.exception.CompanyNotFoundException;
import uk.co.pepper.companysearch.model.SearchRequest;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@WebAppConfiguration
@ActiveProfiles("test")
public class CompanyServiceTest {

    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private final String companyNumber = "06500244";
    private final String companyName = "BBC LIMITED";
    private final String inactiveOfficerCompanyNumber = "06500299";
    private final String badCompanyNumber = "99999999";
    private final Address address = new Address("England", "The Leeming Building", "North Leverton", "Boswell Cottage Main Street", "DN22 0AD");
    private final Officer officer = new Officer(
            null,
            address,
            "director",
            "Software Developer",
            "British",
            "United Kingdom",
            "Jane Doe",
            LocalDate.now().minusYears(5)
    );
    private final Officer inactiveOfficer = new Officer(
            LocalDate.now().minusMonths(6),
            address,
            "director",
            "Software Developer",
            "British",
            "United Kingdom",
            "Jane Doe",
            LocalDate.now().minusYears(5)
    );
    private final Company company = new Company(
            "Boswell Cottage Main Street, North Leverton, Retford, England, DN22 0AD",
            "ltd",
            "06500244",
            address,
            LocalDate.now().minusYears(8),
            "active",
            "06500244 - Incorporated on 11 February 2008",
            "BBC LIMITED"
    );
    private final Company inactiveOfficerCompany = new Company(
            "Boswell Cottage Main Street, North Leverton, Retford, England, DN22 0AD",
            "ltd",
            "06500299",
            address,
            LocalDate.now().minusYears(8),
            "active",
            "06500299 - Incorporated on 11 February 2008",
            "BBC LIMITED"
    );
    @Value("${company.search.path}")
    private String companySearchPath;
    @Value("${officer.search.path}")
    private String officerSearchPath;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CompanyService companyService;

    @BeforeEach
    public void setup() throws IOException {
        final Dispatcher dispatcher = new Dispatcher() {
            @NotNull
            @Override
            public MockResponse dispatch(@NotNull final RecordedRequest request) {
                if (Objects.equals(request.getPath(), companySearchPath + companyNumber)) {
                    return new MockResponse().setResponseCode(200)
                            .setBody(asJson(new CompaniesApiResponse(0, List.of(company), 1)))
                            .addHeader("Content-Type", "application/json");
                }
                if (Objects.equals(request.getPath(), companySearchPath + inactiveOfficerCompanyNumber)) {
                    return new MockResponse().setResponseCode(200)
                            .setBody(asJson(new CompaniesApiResponse(0, List.of(inactiveOfficerCompany), 1)))
                            .addHeader("Content-Type", "application/json");
                }
                if (Objects.equals(request.getPath(), officerSearchPath + inactiveOfficerCompanyNumber)) {
                    return new MockResponse().setResponseCode(200)
                            .setBody(asJson(new OfficersApiResponse(0, List.of(inactiveOfficer))))
                            .addHeader("Content-Type", "application/json");
                }
                if (Objects.equals(request.getPath(), officerSearchPath + companyNumber)) {
                    return new MockResponse().setResponseCode(200)
                            .setBody(asJson(new OfficersApiResponse(0, List.of(officer))))
                            .addHeader("Content-Type", "application/json");
                }
                if (Objects.equals(request.getPath(), companySearchPath + badCompanyNumber)) {
                    return new MockResponse().setResponseCode(404)
                            .setBody("Company not found");
                }
                return new MockResponse().setResponseCode(HttpStatus.NOT_FOUND.value());
            }
        };

        final MockWebServer mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(dispatcher);
        mockWebServer.start();

        ReflectionTestUtils.setField(companyService, "restClient", RestClient.create(mockWebServer.url("/").toString()));
    }

    @DisplayName("Verify successful company search")
    @Test
    public void canSuccessFullySearchForCompanies() {
        final var result = companyService.findCompanies("mock-api-key", new SearchRequest(companyName, companyNumber), true);

        final uk.co.pepper.companysearch.model.Company actualCompany = result.items().get(0);
        final uk.co.pepper.companysearch.model.Officer actualOfficer = result.items().get(0).officers().get(0);

        assertEquals(company.companyStatus(), actualCompany.companyStatus());
        assertEquals(company.companyType(), actualCompany.companyType());
        assertEquals(company.dateOfCreation().toString(), actualCompany.dateOfCreation());
        assertEquals(company.title(), actualCompany.title());
        assertEquals(company.companyNumber(), actualCompany.companyNumber());
        assertEquals(company.address().premises(), actualCompany.address().premises());
        assertEquals(company.address().locality(), actualCompany.address().locality());
        assertEquals(company.address().addressLine1(), actualCompany.address().addressLine1());
        assertEquals(company.address().country(), actualCompany.address().country());
        assertEquals(company.address().postalCode(), actualCompany.address().postalCode());
        assertEquals(officer.name(), actualOfficer.name());
        assertEquals(officer.officerRole(), actualOfficer.officerRole());
        assertEquals(officer.appointedOn().toString(), actualOfficer.appointedOn());
        assertEquals(officer.address().premises(), actualOfficer.address().premises());
        assertEquals(officer.address().locality(), actualOfficer.address().locality());
        assertEquals(officer.address().addressLine1(), actualOfficer.address().addressLine1());
        assertEquals(officer.address().country(), actualOfficer.address().country());
        assertEquals(officer.address().postalCode(), actualOfficer.address().postalCode());
    }

    @DisplayName("Won't return inactive officers")
    @Test
    public void willNotReturnInactiveOfficers() {
        final var result = companyService.findCompanies("mock-api-key", new SearchRequest(companyName, inactiveOfficerCompanyNumber), true);

        final uk.co.pepper.companysearch.model.Company actualCompany = result.items().get(0);

        assertEquals(new ArrayList<>(), actualCompany.officers());
    }

    @DisplayName("404 response for company not found")
    @Test()
    public void companyNotFoundReturnsCorrectStatus() {
        final CompanyNotFoundException e = assertThrows(CompanyNotFoundException.class, () -> {
            companyService.findCompanies("mock-api-key", new SearchRequest("BBC LIMITED", badCompanyNumber), false);
        });

        assertEquals("No company data found for " + companySearchPath + badCompanyNumber, e.getMessage());
    }

    private String asJson(final Object o) {
        try {
            return mapper.writeValueAsString(o);
        } catch (final JsonProcessingException e) {
            return "";
        }
    }

}
