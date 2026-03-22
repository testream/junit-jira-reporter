# JUnit Reporter for Jira Teams using Testream

This repository demonstrates how to integrate [JUnit 5](https://junit.org/junit5/) with [Testream](https://testream.app) so that test results are automatically uploaded to your Jira workspace after every CI run.

## What is Testream?

[Testream](https://testream.app) is a test reporting tool for Jira teams. It imports CI/CD test results from native reporters (Vitest, Playwright, Jest, Cypress, .NET, JUnit, and others), giving your team failure inspection, trends, and release visibility directly inside Jira — without manual test case management.

Once configured, every JUnit run streams structured results to Testream. Failed tests appear in Jira with the full error message and stack trace attached, so triage starts with complete context.

## Project structure

```
src/
  main/java/com/example/shoppingcart/
    Cart.java        — Cart class: add/remove items, calculate totals, checkout
    CartItem.java    — Cart item value object
    Product.java     — Product class with validation, pricing, and formatting helpers
    Discount.java    — Coupon class with percentage, fixed, and validation logic
  test/java/com/example/shoppingcart/
    CartTest.java     — Cart tests (passing + 1 intentional failure)
    ProductTest.java  — Product tests (passing + 1 intentional failure)
    DiscountTest.java — Discount tests (passing + 1 intentional failure)
pom.xml
.github/workflows/junit.yml
.env.example
```

The three intentionally failing tests exist so you can see exactly what a failed test looks like inside Testream and Jira — with the error diff and stack trace surfaced in the dashboard.

## Getting started

### 1. Install Testream for Jira

Install the **[Testream for Jira](https://marketplace.atlassian.com/apps/3048460704/testream-for-jira)** app from the Atlassian Marketplace into your Jira workspace. This is what surfaces test results, failure details, trends, and dashboards inside Jira.

### 2. Create a Testream project

1. Sign in at [testream.app](https://testream.app) (free plan available).
2. Create a project and copy your API key.

### 3. Install dependencies

```bash
mvn test -DskipTests
```

> Requires [Java 17+](https://adoptium.net) and [Maven 3.8+](https://maven.apache.org/download.cgi).

### 4. Configure your API key

Set `TESTREAM_API_KEY` as a GitHub Actions secret (see [CI with GitHub Actions](#ci-with-github-actions) below). For local uploads, export the variable in your shell:

```bash
export TESTREAM_API_KEY=your_api_key_here
```

### 5. Run the tests

```bash
mvn test
```

Maven writes JUnit XML reports to `target/surefire-reports/`. To upload results locally:

```bash
npx @testream/junit-reporter \
  --api-key "$TESTREAM_API_KEY" \
  --app-name junit-jira-reporter-example \
  --test-environment local \
  --test-type unit
```

## Testream reporter configuration

The Testream JUnit reporter is a CLI tool (`@testream/junit-reporter`) rather than an in-process reporter. It reads the JUnit XML files that Maven Surefire writes to `target/surefire-reports/`, converts them to CTRF format, and uploads to Testream. Key points:

- Maven must run with `-Dmaven.test.failure.ignore=true` in CI so the build exits with code 0 even when tests fail — this ensures the upload step always runs and no results are lost.
- The default JUnit XML path (`target/surefire-reports/TEST-*.xml`) matches Maven Surefire's output with no extra configuration. If your project writes XML reports to a different location, pass the path with `--junit-path`:
  ```bash
  npx @testream/junit-reporter --api-key "$TESTREAM_API_KEY" --junit-path "./build/test-results/**/*.xml"
  ```
- `--fail-on-error` is recommended in CI so a broken upload does not silently swallow results.
- `--branch`, `--commit-sha`, `--repository-url`, `--build-number`, and `--build-url` are **auto-detected** by the reporter — no manual wiring needed.

See the [Testream JUnit reporter docs](https://docs.testream.app/reporters/junit) for the full list of CLI options.

## CI with GitHub Actions

The workflow at `.github/workflows/junit.yml` runs all tests on every push and pull request. The only secret you need to add is your Testream API key:

**Settings → Secrets and variables → Actions → New repository secret**

| Name | Value |
|---|---|
| `TESTREAM_API_KEY` | your Testream API key |

All other metadata (branch, commit SHA, build number, build URL, repository URL) is resolved automatically — nothing else to configure.

## Viewing results in Jira

Once tests are uploaded, open your Testream project and connect it to your Jira workspace. With the **[Testream for Jira](https://marketplace.atlassian.com/apps/3048460704/testream-for-jira)** app installed you get:

- **Dashboard** — pass rates, failure counts, flaky test detection, and execution summaries at a glance
- **Failure Insights** — inspect failed tests with the full error, stack trace, and diff
- **Trends & Analytics** — pass/fail trends, duration patterns, and suite growth over custom date ranges
- **Test Suite Changes** — see which tests were added or removed between runs
- **Release Visibility** — link test runs to Jira releases to track quality before shipping
- **Jira Issues** — create issues directly from any failed test with failure context pre-filled

See the [Testream JUnit reporter docs](https://docs.testream.app/reporters/junit) for the full list of configuration options.
