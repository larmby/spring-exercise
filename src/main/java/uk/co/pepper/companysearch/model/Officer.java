package uk.co.pepper.companysearch.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Officer(
        String name,
        @JsonProperty("officer_role")
        String officerRole,
        @JsonProperty("appointed_on")
        String appointedOn,
        @JsonProperty("resigned_on")
        String resignedOn,
        Address address
) {
}
