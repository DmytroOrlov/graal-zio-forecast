package zioapp

import app.Config
import zio._

package object config {

  trait HasConfig {
    def config: Config
  }

  val host = ZIO.access { c: HasConfig ⇒ c.config.host }
  val port = ZIO.access { c: HasConfig ⇒ c.config.port }
}
