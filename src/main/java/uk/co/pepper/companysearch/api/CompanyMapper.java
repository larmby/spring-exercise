package uk.co.pepper.companysearch.api;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;

public class CompanyMapper {

    public static List<uk.co.pepper.companysearch.model.Company> fromCompaniesApiResponses(final CompaniesApiResponse companiesApiResponse) {
        return companiesApiResponse.companies().stream()
                .map(CompanyMapper::fromApiCompany)
                .toList();
    }

    public static uk.co.pepper.companysearch.model.Company fromApiCompany(final Company company) {
        return new uk.co.pepper.companysearch.model.Company(company.companyNumber(),
                company.companyType(),
                company.title(),
                company.companyStatus(),
                formatDate(company.dateOfCreation()),
                fromApiAddress(company.address()),
                new ArrayList<>());
    }

    public static uk.co.pepper.companysearch.model.Address fromApiAddress(final Address address) {
        return new uk.co.pepper.companysearch.model.Address(
                address.premises(),
                address.locality(),
                address.addressLine1(),
                address.country(),
                address.postalCode());
    }

    public static List<uk.co.pepper.companysearch.model.Officer> fromOfficersApiResponses(final OfficersApiResponse officersApiResponse) {
        return officersApiResponse.officers().stream()
                .map(CompanyMapper::fromApiOfficer)
                .toList();
    }

    public static uk.co.pepper.companysearch.model.Officer fromApiOfficer(final Officer officer) {
        return new uk.co.pepper.companysearch.model.Officer(
                officer.name(),
                officer.officerRole(),
                formatDate(officer.appointedOn()),
                formatDate(officer.resignedOn()),
                fromApiAddress(officer.address()));
    }

    private static String formatDate(final LocalDate date) {
        return date != null ? date.format(ISO_LOCAL_DATE) : null;
    }
}
