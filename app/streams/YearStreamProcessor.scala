package streams

import org.apache.pekko.NotUsed
import org.apache.pekko.stream.scaladsl.{Flow, Source}
import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.trace.{Span, StatusCode, Tracer}
import io.opentelemetry.context.Context
import scala.util.Random

class YearStreamProcessor {
  
  private val YEARS = Array("2015", "2016", "2017", "2018", "2019", "2020", "2021", "2022", "2023", "2024")
  private val generator = new Random()
  private val tracer: Tracer = GlobalOpenTelemetry.getTracer("year-stream")

  /**
   * Process a stream of year requests with OpenTelemetry instrumentation
   */
  def processYearStream(count: Int): Source[String, NotUsed] = {
    // Capture the context at stream creation time
    val parentContext = Context.current()
    
    Source(1 to count)
      .via(instrumentedYearFlow(parentContext))
  }

  private def instrumentedYearFlow(parentContext: Context): Flow[Int, String, NotUsed] = {
    Flow[Int].map { index =>
      // Create a span as a child of the parent context
      val span = tracer.spanBuilder("stream-process-year")
        .setParent(parentContext)
        .setSpanKind(io.opentelemetry.api.trace.SpanKind.INTERNAL)
        .startSpan()
      
      try {
        val scope = span.makeCurrent()
        try {
          // Simulate processing
          Thread.sleep(generator.nextInt(50) + 10)
          
          val selectedYear = YEARS(generator.nextInt(YEARS.length))
          
          // Add attributes
          span.setAttribute("stream.index", index)
          span.setAttribute("selected.year", selectedYear)
          span.setAttribute("processing.type", "stream")
          
          selectedYear
        } finally {
          scope.close()
        }
      } catch {
        case e: Exception =>
          span.recordException(e)
          span.setStatus(StatusCode.ERROR, "Stream processing failed")
          throw e
      } finally {
        span.end()
      }
    }
  }
}