package uk.co.pepper.companysearch.controller;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.co.pepper.companysearch.exception.BadRequestException;
import uk.co.pepper.companysearch.model.CompanySearchResponse;
import uk.co.pepper.companysearch.model.SearchRequest;
import uk.co.pepper.companysearch.service.CompanyService;

@Controller
@RequestMapping("/api/companies")
public class CompanySearchController {

    private final CompanyService companyService;

    @Autowired
    public CompanySearchController(final CompanyService companyService) {
        this.companyService = companyService;
    }

    @PostMapping(value = "/search", consumes = "application/json", produces = "application/json")
    public ResponseEntity<CompanySearchResponse> searchCompanies(
            @RequestHeader(name = "x-api-key") final String apiKey,
            @RequestBody final SearchRequest searchRequest,
            @RequestParam(name = "active", defaultValue = "false") boolean onlyActiveCompanies
    ) {
        if (Strings.isBlank(apiKey)) {
            throw new BadRequestException("API key must be provided in the request header");
        }
        if (Strings.isBlank(searchRequest.companyName()) && Strings.isBlank(searchRequest.companyNumber())) {
            throw new BadRequestException("Company name or number must be provided in the request body");
        }

        return ResponseEntity.ok(companyService.findCompanies(apiKey, searchRequest, onlyActiveCompanies));
    }
}

