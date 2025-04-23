package uk.co.pepper.companysearch.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Address(
        String premises,
        String locality,
        @JsonProperty("address_line_1")
        String addressLine1,
        String country,
        @JsonProperty("postal_code")
        String postalCode
) {}
