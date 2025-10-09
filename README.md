# Play Framework + Pekko OpenTelemetry Workshop

This workshop demonstrates OpenTelemetry instrumentation in a Scala application using Play Framework and Apache Pekko.

## What You'll Learn

1. **Manual span creation** - Creating spans programmatically
2. **Automatic instrumentation** - Using `@WithSpan` annotations
3. **Span attributes** - Adding metadata to traces
4. **Error handling** - Recording exceptions and error status
5. **Async instrumentation** - Tracing Pekko actors
6. **Stream instrumentation** - Tracing Pekko Streams

## Quick Start in Codespaces

### 1. Open in Codespaces

Click the "Code" button and select "Create codespace on main"

### 2. Configure to Send to Honeycomb

From [https://docs.honeycomb.io/send-data/java/opentelemetry-agent/#add-automatic-instrumentation](https://docs.honeycomb.io/send-data/java/opentelemetry-agent/#add-automatic-instrumentation). 

Set the following environment variables in the Codespaces terminal:

```bash
export OTEL_EXPORTER_OTLP_ENDPOINT="https://api.honeycomb.io:443"
export OTEL_EXPORTER_OTLP_HEADERS="x-honeycomb-team=YOUR_API_KEY"
export OTEL_SERVICE_NAME="scala-advanced-workshop"
export OTEL_TRACES_EXPORTER="otlp"
```

### 3. Run the Application

```bash
sbt run
```

Wait for "Server started" message, then the app will be at http://localhost:9000

### 4. Generate Traces

Open another terminal and run:

```bash
# Simple synchronous endpoint
curl http://localhost:9000/year

# Actor-based endpoint
curl http://localhost:9000/year/actor

# Stream-based endpoint
curl http://localhost:9000/year/stream

# Health check
curl http://localhost:9000/health
```

## Architecture

```
┌─────────────────────────────────────────────────┐
│  Play Framework Controller                      │
│  - Manual span creation                         │
│  - @WithSpan annotations                        │
│  - Span attributes & status                     │
└────────────┬────────────────────────────────────┘
             │
             ├─────────────────────────────────────┐
             │                                     │
             ▼                                     ▼
    ┌────────────────┐                  ┌──────────────────┐
    │  Pekko Actor   │                  │  Pekko Streams   │
    │  - Async trace │                  │  - Flow tracing  │
    │  - Context     │                  │  - Bulk ops      │
    └────────────────┘                  └──────────────────┘
```

## Endpoints

- `GET /year` - Simple endpoint with manual instrumentation
- `GET /year/actor` - Uses Pekko Actor (async)
- `GET /year/stream` - Uses Pekko Streams (batch processing)
- `GET /health` - Health check (uninstrumented)

## Key Concepts Demonstrated

### 1. Manual Span Creation

```scala
val span = tracer.spanBuilder("operation-name")
  .setSpanKind(SpanKind.SERVER)
  .startSpan()
try {
  val scope = span.makeCurrent()
  // ... your code ...
} finally {
  span.end()
}
```

### 2. Span Attributes

```scala
span.setAttribute("key", "value")
span.setAttribute("count", 42)
```

### 3. Error Recording

```scala
catch {
  case e: Exception =>
    span.recordException(e)
    span.setStatus(StatusCode.ERROR, "Failed")
}
```

### 4. Async Context Propagation

The example shows how spans propagate through:
- Futures (Play controllers)
- Actor messages (Pekko)
- Stream operations (Pekko Streams)

## Workshop Exercises

### Exercise 1: Add Custom Attributes
Add a custom attribute to track the execution time of the `getYear()` method.

### Exercise 2: Create a New Actor
Create a new actor that validates years and add instrumentation.

### Exercise 3: Add Error Scenarios
Modify the code to randomly throw exceptions and observe error traces.

### Exercise 4: Instrument a New Stream
Create a new stream that filters years and add proper instrumentation.

## Troubleshooting

**App won't start:**
```bash
# Clean and rebuild
sbt clean compile
```


## Next Steps

- Add custom metrics
- Implement distributed tracing across services
- Add baggage for cross-cutting concerns

## Resources

- [OpenTelemetry Scala](https://opentelemetry.io/docs/instrumentation/java/)
- [Play Framework](https://www.playframework.com/)
- [Apache Pekko](https://pekko.apache.org/)
- [Honeycomb Docs](https://docs.honeycomb.io/)
```

---

## Usage Instructions

1. **Create a new GitHub repository** with these files
2. **Open in Codespaces** - GitHub will automatically use the devcontainer config
4. **Run** `sbt run` to start the Play app
5. **Generate traces** by hitting the endpoints
6. **View traces** at https://ui.honeycomb.io