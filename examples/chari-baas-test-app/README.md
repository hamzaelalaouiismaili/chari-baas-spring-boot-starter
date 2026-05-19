# Chari BaaS Test App

Small Spring Boot app for testing `chari-baas-spring-boot-starter`.

## Prerequisite

For JitPack resolution, keep the `jitpack.io` repository in `pom.xml` and use version `v1.0.1`.

For local development, install the starter from the SDK root:

```bash
mvn clean install
```

Then change the dependency version in this app to `1.0.1`.

## Run

```bash
cd examples/chari-baas-test-app
CHARI_API_KEY=your-sandbox-key \
mvn spring-boot:run
```

The app runs on port `8081`.

## Smoke Tests

```bash
curl "http://localhost:8081/test/chari/status?phoneNumber=+2126xxxxxxxx"
curl "http://localhost:8081/test/chari/default-wallet?phoneNumber=+2126xxxxxxxx"
curl "http://localhost:8081/test/chari/balance?phoneNumber=+2126xxxxxxxx"
curl "http://localhost:8081/test/chari/info?phoneNumber=+2126xxxxxxxx"
```

Register:

```bash
curl -X POST "http://localhost:8081/test/chari/customers/register" \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "+2126xxxxxxxx",
    "firstName": "Mohammed",
    "lastName": "Chairi",
    "cin": "K000000",
    "walletType": "P"
  }'
```

Transfer preview:

```bash
curl -X POST "http://localhost:8081/test/chari/transfer/preview" \
  -H "Content-Type: application/json" \
  -d '{
    "customerPhoneNumber": "+2126xxxxxxxx",
    "recipientPhoneNumber": "+2127xxxxxxxx",
    "amount": 10,
    "reason": "test preview"
  }'
```

Sandbox card cash-in:

```bash
curl -X POST "http://localhost:8081/test/chari/card/cashin/execute?phoneNumber=+2126xxxxxxxx" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Mohammed",
    "lastName": "Chairi",
    "cvv": "123",
    "amount": 100,
    "pan": "4918914107195005",
    "expiryDate": "08/26",
    "keepAlive": true,
    "cardName": "sandbox_card"
  }'
```

Open the returned `redirectionURL` and use 3DS code `555`.
