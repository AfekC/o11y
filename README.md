# o11y

`o11y` is an observability library for Java that integrates Metrics, Traces, and Logs into a unified setup. It automatically pushes traces to Grafana Alloy via gRPC, exposes Prometheus metrics, and enforces structured JSON logging with trace context correlation.

## Features

- **Metrics**:
    - Exposes a Prometheus scraping endpoint on `/metrics`.
    - Automatically registers the `target_info` metric with `environment`, `service.name`, and `system` labels sourced from environment variables.
    - Provides a function to register custom metrics.
- **Tracing**:
    - Automatically batches and sends spans to **Alloy** via gRPC.
    - Provides function annotations `@Span` for easy span creation.
- **Logging**:
    - Formats all logs as **JSON**.
    - Format the timestamp format into `yyyy-MM-dd'T'HH:mm:ss.SSS'Z'`
    - Automatically injects `SpanContext` (TraceID, SpanID) into every log entry.
    - Automatically injects  `attributes` (service.name, environment, system) into every log entry.