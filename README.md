# JUnit Jira Reporter: Send JUnit 5 Results to Jira with Testream

This repository is a practical **JUnit + Jira integration example** using [`@testream/junit-reporter`](https://docs.testream.app/reporters/junit). It shows how to run JUnit 5 tests, generate Maven Surefire XML, and upload results to Jira through Testream from local runs and GitHub Actions.

If you are searching for **"JUnit Jira reporter"**, **"Maven test results to Jira"**, or **"GitHub Actions JUnit Jira integration"**, this repo is the implementation template.

## Why this example is useful

- **Maven-based flow**: Uses standard Surefire XML output.
- **CI-safe upload pattern**: Keeps upload step running even when tests fail.
- **Minimal wiring**: Reporter auto-detects most CI metadata.
- **Real triage demo**: Intentional failing tests show Jira-side failure handling.

## What is Testream?

[Testream](https://testream.app) is an automated test management and reporting platform for Jira teams. It ingests test results from JUnit and many other frameworks, then provides failure diagnostics, trends, and release-level quality views in Jira.

If this sample repository is not the framework you need, browse all native reporters in the Testream docs: <https://docs.testream.app/>.

### Watch Testream in action

Click to see how Testream turns raw CI test results into actionable Jira insights (failures, trends, and release visibility):  
[![Watch the video](https://img.youtube.com/vi/5sDao2Q8k1k/maxresdefault.jpg)](https://www.youtube.com/watch?v=5sDao2Q8k1k)

Install **[Testream Automated Test Management and Reporting for Jira](https://marketplace.atlassian.com/apps/3048460704/testream-automated-test-management-and-reporting-for-jira)** in your Jira workspace to view uploaded runs.

## Project structure

```text
src/
  main/java/com/example/shoppingcart/
    Cart.java
    CartItem.java
    Product.java
    Discount.java
  test/java/com/example/shoppingcart/
    CartTest.java     - Passing + intentional failure
    ProductTest.java  - Passing + intentional failure
    DiscountTest.java - Passing + intentional failure
pom.xml
.github/workflows/junit.yml
.env.example
```

The intentional failures help you verify how failed JUnit tests appear in Testream/Jira.

## Quick start: JUnit to Jira reporting

### 1. Create your Testream project and API key

1. Sign in at [testream.app](https://testream.app).
2. Create a project.
3. Copy your API key.

### 2. Install dependencies

```bash
mvn test -DskipTests
```

Requires Java 17+ and Maven 3.8+.

### 3. Set API key

For local uploads:

```bash
export TESTREAM_API_KEY=<your key>
```

For CI uploads, add it as a GitHub Actions secret.

### 4. Run JUnit tests

```bash
mvn test
```

Surefire writes XML reports to `target/surefire-reports/`.

### 5. Upload results locally

```bash
npx @testream/junit-reporter \
  --api-key "$TESTREAM_API_KEY" \
  --app-name junit-jira-reporter-example \
  --test-environment local \
  --test-type unit \
  --fail-on-error
```

## Reporter configuration (`@testream/junit-reporter`)

This repo uses the CLI uploader, which reads JUnit XML and uploads to Testream.

Key behavior:

- Default XML path matches Surefire output (`target/surefire-reports/TEST-*.xml`).
- You can override report path with `--junit-path`.
- `--fail-on-error` is enabled in CI upload step.
- CI metadata (`branch`, `commit-sha`, `repository-url`, `build-number`, `build-url`) is auto-detected.

Reporter docs: <https://docs.testream.app/reporters/junit>

## GitHub Actions setup

The workflow at `.github/workflows/junit.yml` runs on pushes, pull requests, and manual dispatch.

Add this repository secret:

**Settings -> Secrets and variables -> Actions -> New repository secret**

| Name | Value |
|---|---|
| `TESTREAM_API_KEY` | Your Testream API key |

Important CI pattern used in this repo:

- Maven runs with `-Dmaven.test.failure.ignore=true` so upload still executes even if tests fail.

## How results appear in Jira

After connecting Testream to Jira, you get:

- Dashboard summaries for run health
- Failure diagnostics with stack traces and assertion details
- Trend analytics over time
- Jira issue creation directly from failed tests

## Troubleshooting

### No uploads after test execution

- Ensure `TESTREAM_API_KEY` is set in env/secrets.
- Confirm JUnit XML exists in `target/surefire-reports/`.

### Custom report path not found

- Pass explicit glob with `--junit-path`, for example:
  `--junit-path "./build/test-results/**/*.xml"`

### CI run has tests but no Jira data

- Verify Testream project is connected to the correct Jira workspace.

## FAQ

### Is this a production setup?

It is an example repository with production-style CI and upload wiring intended for adaptation.

### Why are tests intentionally failing?

To demonstrate end-to-end failure triage in Jira.

### Can I use Gradle instead of Maven?

Yes, as long as you produce JUnit XML and point `@testream/junit-reporter` at that path.

## JUnit Jira reporting alternatives (quick view)

| Approach | Benefit | Tradeoff |
|---|---|---|
| Store Surefire XML as artifact | Easy baseline | Limited Jira-native analysis |
| Custom parser/uploader | Full control | More maintenance burden |
| Testream JUnit reporter (this repo) | Native Jira-focused reporting with low setup overhead | Requires Testream setup |

## Related links

- Testream app: <https://testream.app>
- Testream Automated Test Management and Reporting for Jira: <https://marketplace.atlassian.com/apps/3048460704/testream-automated-test-management-and-reporting-for-jira>
- JUnit reporter docs: <https://docs.testream.app/reporters/junit>
- JUnit docs: <https://junit.org/junit5/>
