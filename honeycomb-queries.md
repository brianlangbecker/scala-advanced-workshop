# Honeycomb Queries

## Rate Limited vs Successful Requests

Compare rate limited vs successful requests for the frontend-proxy service:

```
Compare Rate Limited vs Successful
WHERE service.name = "frontend-proxy"
  AND span.kind = "server"
CALCULATE success = COUNT_IF(http.status_code = 200)
CALCULATE limited = COUNT_IF(http.status_code = 429)
VISUALIZE success, limited
GROUP BY time(1m)
```

### Description

This query compares successful (HTTP 200) vs rate-limited (HTTP 429) requests for the frontend-proxy service, grouped by 1-minute intervals.
