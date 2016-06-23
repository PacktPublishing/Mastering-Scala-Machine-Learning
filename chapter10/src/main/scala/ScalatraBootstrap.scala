import org.akozlov.examples._

import org.scalatra._
import javax.servlet.ServletContext

import org.scalatra.metrics.MetricsSupportExtensions._
import org.scalatra.metrics._

class ScalatraBootstrap extends LifeCycle with MetricsBootstrap {
  override def init(context: ServletContext) = {
    context.mount(new ServletWithMetrics, "/")
    context.mountMetricsAdminServlet("/admin")
    context.mountHealthCheckServlet("/health")
    context.installInstrumentedFilter("/*")
  }
}
