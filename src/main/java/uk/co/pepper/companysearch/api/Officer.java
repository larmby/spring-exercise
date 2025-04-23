package uk.co.pepper.companysearch.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public record Officer(
        @JsonProperty("resigned_on")
        LocalDate resignedOn,
        Address address,
        @JsonProperty("officer_role")
        String officerRole,
        String occupation,
        String nationality,
        @JsonProperty("country_of_residence")
        String countryOfResidence,
        String name,
        @JsonProperty("appointed_on")
        LocalDate appointedOn) {
}
