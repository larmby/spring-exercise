package uk.co.pepper.companysearch.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CompaniesApiResponse(
        @JsonProperty("page_number")
        long pageNumber,
        @JsonProperty("items")
        List<Company> companies,
        @JsonProperty("total_results")
        long totalResults) {
}
