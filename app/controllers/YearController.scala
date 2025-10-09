package controllers

import actors.YearActor
import actors.YearActor.{GetYear, YearResponse}
import streams.YearStreamProcessor
import javax.inject._
import org.apache.pekko.actor.{ActorSystem, Props}
import org.apache.pekko.pattern.ask
import org.apache.pekko.stream.Materializer
import org.apache.pekko.util.Timeout
import play.api.mvc._
import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.trace.{Span, StatusCode, Tracer}
import io.opentelemetry.context.Context
import io.opentelemetry.instrumentation.annotations.WithSpan;
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.Random

@Singleton
class YearController @Inject()(
  val controllerComponents: ControllerComponents,
  actorSystem: ActorSystem
)(implicit ec: ExecutionContext, mat: Materializer) extends BaseController {

  private val YEARS = Array("2015", "2016", "2017", "2018", "2019", "2020", "2021", "2022", "2023", "2024")
  private val generator = new Random()
  private val tracer: Tracer = GlobalOpenTelemetry.getTracer("year-controller")
  
  private val yearActor = actorSystem.actorOf(YearActor.props(), "year-actor")
  private implicit val timeout: Timeout = Timeout(5.seconds)
  
  private val streamProcessor = new YearStreamProcessor()

  def year: Action[AnyContent] = Action { request =>
    val span = tracer.spanBuilder("GET /year")
      .setSpanKind(io.opentelemetry.api.trace.SpanKind.SERVER)
      .startSpan()
    
    try {
      val scope = span.makeCurrent()
      try {
        span.setAttribute("http.method", "GET")
        span.setAttribute("http.route", "/year")
        span.setAttribute("foo", "bar")
        
        Thread.sleep(generator.nextInt(250))
        
        doSomeWork()
        
        val selectedYear = getYear()
        
        span.setAttribute("response.year", selectedYear)
        span.setStatus(StatusCode.OK)
        
        Ok(s"Selected year: $selectedYear")
      } finally {
        scope.close()
      }
    } catch {
      case e: Exception =>
        span.recordException(e)
        span.setStatus(StatusCode.ERROR, "Request failed")
        InternalServerError("Error processing request")
    } finally {
      span.end()
    }
  }

def yearFromActor: Action[AnyContent] = Action.async { request =>
  val span = tracer.spanBuilder("GET /year/actor")
    .setSpanKind(io.opentelemetry.api.trace.SpanKind.SERVER)
    .startSpan()
  
  val scope = span.makeCurrent()
  
  // Capture the context before going async
  val context = Context.current()
  
  span.setAttribute("http.method", "GET")
  span.setAttribute("http.route", "/year/actor")
  span.setAttribute("processing.type", "actor")
  
  // Pass context with the message
  val futureYear = (yearActor ? GetYear(context)).mapTo[YearResponse]
  
  futureYear.map { response =>
    val callbackScope = context.makeCurrent()
    try {
      span.setAttribute("response.year", response.year)
      span.setStatus(StatusCode.OK)
      
      Ok(s"Year from actor: ${response.year}")
    } finally {
      callbackScope.close()
      scope.close()
      span.end()
    }
  }.recover {
    case e: Exception =>
      val callbackScope = context.makeCurrent()
      try {
        span.recordException(e)
        span.setStatus(StatusCode.ERROR, "Actor request failed")
        
        InternalServerError("Error communicating with actor")
      } finally {
        callbackScope.close()
        scope.close()
        span.end()
      }
  }
}

  def yearFromStream: Action[AnyContent] = Action.async { request =>
    val span = tracer.spanBuilder("GET /year/stream")
      .setSpanKind(io.opentelemetry.api.trace.SpanKind.SERVER)
      .startSpan()
    
    val scope = span.makeCurrent()
    
    // Capture the context before going async
    val context = Context.current()
    
    span.setAttribute("http.method", "GET")
    span.setAttribute("http.route", "/year/stream")
    span.setAttribute("processing.type", "stream")
    span.setAttribute("stream.count", 5)
    
    val futureYears = streamProcessor.processYearStream(5)
      .runFold(List.empty[String])((acc, year) => acc :+ year)
    
    futureYears.map { years =>
      // Restore context in the Future callback
      val callbackScope = context.makeCurrent()
      try {
        span.setAttribute("response.years", years.mkString(", "))
        span.setStatus(StatusCode.OK)
        
        Ok(s"Years from stream: ${years.mkString(", ")}")
      } finally {
        callbackScope.close()
        scope.close()
        span.end()
      }
    }.recover {
      case e: Exception =>
        val callbackScope = context.makeCurrent()
        try {
          span.recordException(e)
          span.setStatus(StatusCode.ERROR, "Stream processing failed")
          
          InternalServerError("Error processing stream")
        } finally {
          callbackScope.close()
          scope.close()
          span.end()
        }
    }
  }

  def health: Action[AnyContent] = Action {
    Ok("OK")
  }

  private def getYear(): String = {
    val span = tracer.spanBuilder("get-random-year")
      .setSpanKind(io.opentelemetry.api.trace.SpanKind.INTERNAL)
      .startSpan()
    
    try {
      val scope = span.makeCurrent()
      try {
        val rnd = generator.nextInt(YEARS.length)
        
        span.setAttribute("random-index", rnd)
        span.setAttribute("method", "getYear")
        
        Thread.sleep(generator.nextInt(250))
        
        YEARS(rnd)
      } finally {
        scope.close()
      }
    } catch {
      case e: InterruptedException =>
        span.setStatus(StatusCode.ERROR, "Sleep interrupted")
        throw e
    } finally {
      span.end()
    }
  }

  @WithSpan
  private def doSomeWork(): Unit = {
    val span = tracer.spanBuilder("some-work")
      .setSpanKind(io.opentelemetry.api.trace.SpanKind.INTERNAL)
      .startSpan()
    
    try {
      val scope = span.makeCurrent()
      try {
        span.setAttribute("otel", "rocks")
        span.setAttribute("framework", "play+pekko")
        
        Thread.sleep(generator.nextInt(250))
      } finally {
        scope.close()
      }
    } catch {
      case e: Throwable =>
        span.recordException(e)
        span.setStatus(StatusCode.ERROR, "Work failed")
    } finally {
      span.end()
    }
  }
}