# HMPPS Breach Notice API

[![repo standards badge](https://img.shields.io/badge/endpoint.svg?&style=flat&logo=github&url=https%3A%2F%2Foperations-engineering-reports.cloud-platform.service.justice.gov.uk%2Fapi%2Fv1%2Fcompliant_public_repositories%2Fhmpps-breach-notice-api)](https://operations-engineering-reports.cloud-platform.service.justice.gov.uk/public-report/hmpps-breach-notice-api "Link to report")
[![Docker Repository on ghcr](https://img.shields.io/badge/ghcr.io-repository-2496ED.svg?logo=docker)](https://ghcr.io/ministryofjustice/hmpps-breach-notice-api)
[![API docs](https://img.shields.io/badge/API_docs_-view-85EA2D.svg?logo=swagger)](https://breach-notice-api-dev.hmpps.service.justice.gov.uk/swagger-ui/index.html)

API back-end for the [HMPPS Breach Notice](https://github.com/ministryofjustice/hmpps-breach-notice-ui) service.

# Instructions

## Running the application locally

The application comes with a `dev` spring profile that includes default settings for running locally. This is not
necessary when deploying to kubernetes as these values are included in the helm configuration templates -
e.g. `values-dev.yaml`.

There is also a `docker-compose.yml` that can be used to run a local instance of the template in docker and also an
instance of HMPPS Auth (required if your service calls out to other services using a token).

```bash
docker compose pull && docker compose up
```

will build the application and run it and HMPPS Auth within a local docker instance.

## Running the application in IntelliJ IDEA

```bash
docker compose pull && docker compose up --scale hmpps-breach-notice-api=0
```

will just start a docker instance of HMPPS Auth. The application should then be started with a `dev` active profile
in IntelliJ.

## Authentication

All API endpoints require an OAuth2 client token from HMPPS Auth with the `BREACH_NOTICE` role.

When running locally, there is a built-in client you can use that has the correct role:
```properties
CLIENT_ID=hmpps-breach-notice-ui-client
CLIENT_SECRET=clientsecret
```