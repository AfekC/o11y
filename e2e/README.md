# Local e2e telemetry stack

Tempo + Alloy + Grafana for manually testing `o11y-sample-app` end to end.

```
sample app (host :8081) --OTLP gRPC :4317--> Alloy --> Tempo (:3200) <-- Grafana (:3000)
```

| Service | URL | Purpose |
|---|---|---|
| Grafana | http://localhost:3000 | UI (anonymous admin, Tempo datasource pre-provisioned) |
| Alloy   | http://localhost:12345 | Pipeline graph + live debugging of incoming spans |
| Alloy OTLP | `localhost:4317` (gRPC), `localhost:4318` (HTTP) | Where the app sends spans |
| Tempo   | http://localhost:3200 | Trace storage / query API |

## 1. Start the stack

```sh

```

## 2. Run the sample app against it

Build once from the repo root (JDK 25): `mvn clean install`

PowerShell:
```powershell
$env:MANAGEMENT_OPENTELEMETRY_TRACING_EXPORT_OTLP_ENDPOINT = "http://localhost:4317"
$env:SERVICE_NAME = "sample-service"
$env:ENVIRONMENT  = "DRILL"
$env:SYSTEM       = "e2e"
java -jar o11y-sample-app/target/o11y-sample-app-1.0.0.jar
```

bash:
```sh
MANAGEMENT_OPENTELEMETRY_TRACING_EXPORT_OTLP_ENDPOINT=http://localhost:4317 \
SERVICE_NAME=sample-service ENVIRONMENT=DRILL SYSTEM=e2e \
java -jar o11y-sample-app/target/o11y-sample-app-1.0.0.jar
```

> Spring Boot does not read `src/main/resources/.env` on its own, so set the variables in the shell
> (or in your IntelliJ run configuration).

## 3. Manual test checklist

1. **Generate traffic:** `curl "localhost:8081/hello?name=e2e"` (a few times).
2. **Traces:** Grafana → Explore → Tempo → Search, Service Name = `sample-service`
   (or TraceQL `{ resource.service.name = "sample-service" }`).
   New traces can take ~30s to appear in **search**; lookup by trace ID works sooner.
   A `/hello` trace should contain the nested `span_in_span` span with the
   `nice_attribute=7` attribute and the `complex_work_finished` event.
   The `will_not_work` span should **not** appear (self-invocation bypasses the aspect).
3. **Resource attributes:** on any span, check `environment`, `system`, `service.name`.
4. **Log ↔ trace correlation:** copy a `spanContext.traceId` from the app's JSON console logs and
   paste it into Grafana → Explore → Tempo → TraceQL / Trace ID. It should open the same trace.
5. **Metrics:** `curl -i localhost:8081/metrics` → `308` redirect to `/actuator/prometheus`,
   which lists `target_info`, `my_test_metric`, `test_counter_total`.
6. **Pipeline:** Alloy UI → `otelcol.receiver.otlp.default` → Live debugging to watch spans arrive.

## Stop

```sh
docker compose -f e2e/docker-compose.yml down      # keep stored traces
docker compose -f e2e/docker-compose.yml down -v   # also delete Tempo data
```
