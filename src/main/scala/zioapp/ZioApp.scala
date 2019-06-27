package zioapp

import java.io.IOException

import app.{City, Config, Requests, Temperature, WeatherClient}
import cats.implicits._
import zio._
import capture.Capture
import zioapp.config._
import zioapp.console._
import zioapp.error.WeatherErr
import zioapp.weather._

object ZioApp extends App {
  def askFetchJudge(reqs: Ref[Requests], client: WeatherClient) =
    for {
      cityName <- askCity
      city <- cityByName(cityName)
      f <- fetchForecast(client, city, reqs)
      _ <- printLn(s"Forecast for $city is ${f.temperature}")
      hottest <- hottestCity(reqs)
      _ <- printLn(s"Hottest city found so far is $hottest")
    } yield ()

  def app(requests: Requests) = {
    val makeLoop = for {
      reqsRef <- Ref.make(requests)
      host <- host
      port <- port
      weatherClient = new WeatherClient(host, port)
      _ <- askFetchJudge(reqsRef, weatherClient).forever
    } yield ()

    def program = for {
      _ <- printLn(typestr(makeLoop))
      host <- host
      port <- port
      _ <- printLn(s"Using weather service at http://$host:$port")
      _ <- makeLoop
    } yield ()

    program.foldM(
      e ⇒ printLn(e.continue(new WeatherErr[String] with ConsoleErr[String] {
        def weatherClient(error: IOException): String = s"weather client $error"

        def unknownCity(cityName: String): String = s"unknown city $cityName"

        def consoleRead(error: IOException): String = s"console read $error"
      }))
        .const(1),
      _ ⇒ IO
        .succeed(0)
    )
  }

  def hottestCity(requests: Ref[Requests]): UIO[(City, Temperature)] =
    for (reqs <- requests.get) yield Requests.hottest(reqs).map(_.temperature)

  def fetchForecast(client: WeatherClient, city: City, requests: Ref[Requests]) =
    for {
      reqs <- requests.get
      maybeForecast = reqs.get(city)
      forecast <- maybeForecast.fold(forecast(client, city))(IO.succeed)
      _ <- requests.update(_ + (city -> forecast))
    } yield forecast

  def cityByName(cityName: String): IO[Capture[WeatherErr], City] = {
    def succeed = IO.succeed(City(cityName))

    cityName match {
      case "Wroclaw" ⇒ succeed
      case "Cadiz" ⇒ succeed
      case "Hamburg" ⇒ succeed
      case _ ⇒ IO.fail(WeatherErr.unknownCity(cityName))
    }
  }

  val askCity = printLn("What is the next city?") *> readLn

  def run(args: List[String]): UIO[Int] = {
    val conf = Config("localhost", 8080)
    val requests = Requests.empty

    app(requests).provide(
      new HasConfig with Weather.Live with Console.Live {
        val config = conf
      })
  }

  import scala.util.matching.Regex
  import scala.reflect.runtime.universe._

  def typestr[A: WeakTypeTag](x: => A): String = weakTypeOf[A].dealias.widen.toString
}
