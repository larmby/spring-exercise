package uk.co.pepper.companysearch.service;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.co.pepper.companysearch.api.CompaniesApiResponse;
import uk.co.pepper.companysearch.api.CompanyMapper;
import uk.co.pepper.companysearch.api.OfficersApiResponse;
import uk.co.pepper.companysearch.exception.CompanyNotFoundException;
import uk.co.pepper.companysearch.exception.ServiceUnavailableException;
import uk.co.pepper.companysearch.exception.TruApiAuthException;
import uk.co.pepper.companysearch.model.Company;
import uk.co.pepper.companysearch.model.CompanySearchResponse;
import uk.co.pepper.companysearch.model.Officer;
import uk.co.pepper.companysearch.model.SearchRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CompanyService {

    private Logger logger = org.apache.logging.log4j.LogManager.getLogger(CompanyService.class);

    @Value("${api.key.name}")
    private String apiKeyHeaderName;

    @Value("${company.search.path}")
    private String companySearchPath;

    @Value("${officer.search.path}")
    private String officerSearchPath;

    @Autowired
    private RestClient restClient;

    public CompanySearchResponse findCompanies(final String apiKey,
                                               final SearchRequest searchRequest,
                                               final boolean onlyActiveCompanies) {

        final String companySearchUrl = companySearchPath + queryOf(searchRequest);
        final CompaniesApiResponse companiesResponse = getCompanies(companySearchUrl, apiKey);

        if (companiesResponse == null || companiesResponse.companies().isEmpty()) {
            throw new CompanyNotFoundException("No company data found for " + searchRequest);
        }

        final List<Company> companies = CompanyMapper.fromCompaniesApiResponses(companiesResponse)
                .stream()
                .filter(company -> isActiveCompany(company) || !onlyActiveCompanies)
                .peek(company -> logger.info("Found company {}", company))
                .map(company -> company.withOfficers(activeOfficersFor(company.companyNumber(), apiKey)))
                .toList();

        return new CompanySearchResponse(companies.size(), companies);
    }

    private boolean isActiveCompany(final Company company) {
        return "active".equalsIgnoreCase(company.companyStatus());
    }

    private List<Officer> activeOfficersFor(final String companyNumber, final String apiKey) {
        final String officerSearchUrl = officerSearchPath + companyNumber;
        final OfficersApiResponse officersResponse = getOfficers(officerSearchUrl, apiKey);

        if (officersResponse.officers() == null || officersResponse.officers().isEmpty()) {
            return new ArrayList<>();
        }

        final List<Officer> mappedOfficers = CompanyMapper.fromOfficersApiResponses(officersResponse);

        return mappedOfficers.stream()
                .filter(officer -> Strings.isBlank(officer.resignedOn()))
                .collect(Collectors.toList());
    }

    private String queryOf(final SearchRequest searchRequest) {
        final String companyNumber = searchRequest.companyNumber();
        final String companyName = searchRequest.companyName();

        return Strings.isBlank(companyNumber) && !Strings.isBlank(companyName)
                ? companyName
                : companyNumber;
    }

    private CompaniesApiResponse getCompanies(final String query, final String apiKey) {
        return getResponse(query, apiKey)
                .body(new ParameterizedTypeReference<>() {
                });
    }

    private OfficersApiResponse getOfficers(final String query, final String apiKey) {
        return getResponse(query, apiKey)
                .body(new ParameterizedTypeReference<>() {
                });
    }

    private RestClient.ResponseSpec getResponse(final String uri, final String apiKey) {
        logger.debug("GET {}", uri);
        return restClient.get()
                .uri(uri)
                .header(apiKeyHeaderName, apiKey)
                .retrieve()
                .onStatus(status -> status.value() == 403, (request, response) -> {
                    throw new TruApiAuthException("Unable to authenticate");
                })
                .onStatus(status -> status.value() == 503, (request, response) -> {
                    throw new ServiceUnavailableException("TruProxyAPI service unavailable");
                })
                .onStatus(status -> status.value() == 404, (request, response) -> {
                    throw new CompanyNotFoundException("No company data found for " + uri);
                });
    }
}
