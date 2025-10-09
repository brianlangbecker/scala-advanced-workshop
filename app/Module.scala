import com.google.inject.AbstractModule
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.{Materializer, SystemMaterializer}
import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter
import javax.inject.{Inject, Provider, Singleton}
import scala.concurrent.ExecutionContext

class Module extends AbstractModule {
  override def configure(): Unit = {
    // Initialize OpenTelemetry on startup if not using the OTEL Java agent
    //initializeOpenTelemetry()
    
    // Bind Pekko ActorSystem and Materializer
    bind(classOf[ActorSystem]).toProvider(classOf[ActorSystemProvider]).asEagerSingleton()
    bind(classOf[Materializer]).toProvider(classOf[MaterializerProvider]).asEagerSingleton()
  }
  // Not needed if using the OTEL Java agent
  private def initializeOpenTelemetry(): Unit = {
    val resource = Resource.create(
      Attributes.builder()
        .put("service.name", "play-pekko-year-service")
        .build()
    )

    val spanExporter = OtlpGrpcSpanExporter.builder()
      .setEndpoint("https://api.honeycomb.io:443") // Use Honeycomb OTLP endpoint
      .addHeader("x-honeycomb-team", sys.env.getOrElse("HONEYCOMB_API_KEY", ""))
      .build()

    val tracerProvider = SdkTracerProvider.builder()
      .addSpanProcessor(BatchSpanProcessor.builder(spanExporter).build())
      .setResource(resource)
      .build()

    val openTelemetry = OpenTelemetrySdk.builder()
      .setTracerProvider(tracerProvider)
      .buildAndRegisterGlobal()

    println("OpenTelemetry initialized and registered globally")
  }
}

@Singleton
class ActorSystemProvider @Inject()(implicit ec: ExecutionContext) extends Provider[ActorSystem] {
  lazy val get: ActorSystem = ActorSystem("play-pekko-system")
}

@Singleton
class MaterializerProvider @Inject()(actorSystem: ActorSystem) extends Provider[Materializer] {
  lazy val get: Materializer = SystemMaterializer(actorSystem).materializer
}