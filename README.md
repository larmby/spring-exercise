## Company Search Application
A company search application using Spring Boot 3.4.4 (with embedded wiremock for integration testing and 
demo capabilities).

Exposes an endpoint that uses the `TruProxyAPI` to do a company and officer lookup
via name or registration number.

## Build

To build the project and execute tests, run:

```shell
mvn clean install
```

## Local running
The company search application can be excecuted using maven:

```shell
mvn spring-boot:run
```

and then the api accessed via:

```shell
http://localhost:8080/api/companies/search?active=true
```

where the request parameter `active` will return only active companies.

An example request body is:

<pre>
{
    "companyName" : "BBC LIMITED",
    "companyNumber" : "06500244"
}
</pre>

Include the following request headers:

<pre>
    "headers": {
        "Content-Type": "application/json",
        "x-api-key": "mock-api-key"
    }
</pre>

