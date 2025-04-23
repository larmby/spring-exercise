package uk.co.pepper.companysearch.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record OfficersApiResponse(
        @JsonProperty("items_per_page")
        long itemsPerPage,
        List<Officer> officers) {
}