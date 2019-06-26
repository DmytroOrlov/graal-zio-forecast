package zioapp.console

import java.io.IOException

import zio._
import capture.Capture
import capture.Capture.Constructors

import scala.io.StdIn

trait Console {
  def console: Console.Service[Any]
}

object Console extends {

  trait Service[R] {
    def printLn(line: String): ZIO[R, Nothing, Unit]

    val readLn: ZIO[R, Capture[ConsoleErr], String]
  }

  trait Live extends Console {
    val console = new Service[Any] {
      def printLn(line: String): UIO[Unit] =
        IO.effectTotal(println(line))

      val readLn: IO[Capture[ConsoleErr], String] =
        IO.effect(StdIn.readLine())
          .refineOrDie {
            case er: IOException ⇒ ConsoleErr.consoleRead(er)
          }
    }
  }

}

trait ConsoleErr[+A] {
  def consoleRead(error: IOException): A
}

//this probably should be generated by some macro
object ConsoleErr extends Constructors[ConsoleErr] {
  def consoleRead(error: IOException) = Capture[ConsoleErr](_.consoleRead(error))
}
