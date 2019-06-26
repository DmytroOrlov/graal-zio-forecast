package zioapp

import zio._
import capture.Capture

package object console extends Console.Service[Console] {
  def printLn(line: String): ZIO[Console, Nothing, Unit] =
    ZIO.accessM(_.console printLn line)

  val readLn: ZIO[Console, Capture[ConsoleErr], String] =
    ZIO.accessM(_.console.readLn)
}
