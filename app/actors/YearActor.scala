package actors

import org.apache.pekko.actor.{Actor, ActorLogging, Props}
import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.trace.{Span, StatusCode, Tracer}
import io.opentelemetry.context.Context
import scala.util.Random

object YearActor {
  def props(): Props = Props(new YearActor())
  
  // Pass context with the message
  case class GetYear(context: Context)
  case class YearResponse(year: String)
}

class YearActor extends Actor with ActorLogging {
  import YearActor._
  
  private val YEARS = Array("2015", "2016", "2017", "2018", "2019", "2020", "2021", "2022", "2023", "2024")
  private val generator = new Random()
  private val tracer: Tracer = GlobalOpenTelemetry.getTracer("year-actor")

  override def receive: Receive = {
    case GetYear(parentContext) =>
      // Create a span as a child of the parent context
      val span = tracer.spanBuilder("actor-get-year")
         .setParent(parentContext)
        .setSpanKind(io.opentelemetry.api.trace.SpanKind.INTERNAL)
        .startSpan()
      
      try {
        val scope = span.makeCurrent()
        try {
          log.info("YearActor processing GetYear request")
          
          Thread.sleep(generator.nextInt(100) + 50)
          
          val selectedYear = YEARS(generator.nextInt(YEARS.length))
          
          span.setAttribute("actor.message", "GetYear")
          span.setAttribute("selected.year", selectedYear)
          span.setAttribute("actor.name", self.path.name)
          
          sender() ! YearResponse(selectedYear)
        } finally {
          scope.close()
        }
      } catch {
        case e: Exception =>
          span.recordException(e)
          span.setStatus(StatusCode.ERROR, "Failed to process year request")
          throw e
      } finally {
        span.end()
      }
  }
}