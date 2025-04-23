package uk.co.pepper.companysearch.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Company(
        @JsonProperty("address_snippet")
        String addressSnippet,
        @JsonProperty("company_type")
        String companyType,
        @JsonProperty("company_number")
        String companyNumber,
        Address address,
        @JsonProperty("date_of_creation")
        LocalDate dateOfCreation,
        @JsonProperty("company_status")
        String companyStatus,
        String description,
        String title) {
}
