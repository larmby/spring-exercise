package uk.co.pepper.companysearch.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record CompanySearchResponse(
        @JsonProperty("total_results") int total_results,
        List<Company> items
) {}
