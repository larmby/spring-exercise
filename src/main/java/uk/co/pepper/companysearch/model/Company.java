package uk.co.pepper.companysearch.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Company(
        @JsonProperty("company_number")
        String companyNumber,
        @JsonProperty("company_type")
        String companyType,
        String title,
        @JsonProperty("company_status")
        String companyStatus,
        @JsonProperty("date_of_creation")
        String dateOfCreation,
        Address address,
        List<Officer> officers
) {

    public Company withOfficers(final List<Officer> officers) {
        return new Company(companyNumber, companyType, title, companyStatus, dateOfCreation, address, officers);
    }
}
