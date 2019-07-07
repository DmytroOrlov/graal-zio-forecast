package object app {

  type Requests = Map[City, Forecast]

  object Requests {
    def empty: Requests = Map.empty

    def hottest(request: Requests): (City, Forecast) = request.maxBy {
      case (_, Forecast(Temperature(f, Fahren))) ⇒ (f - 32).toDouble * 5 / 9
      case (_, Forecast(Temperature(c, Celcius))) ⇒ c
    }
  }

  /*
  import cats.implicits._
  import cats.data.Reader
  import cats.data.State
  import cats.data.StateT
  import monix.eval.Task

  def host: Reader[Config, String] =
    Reader(_.host)

  def port: Reader[Config, Int] =
    Reader(_.port)

  def printLn(line: String): Task[Unit] =
    Task.delay(println(line))

  def readLn: Task[String] =
    Task.delay(scala.io.StdIn.readLine)

  def cityByName(cityName: String): Either[Error, City] =
    cityName match {
      case "Wroclaw" ⇒ City(cityName).asRight
      case "Cadiz" ⇒ City(cityName).asRight
      case "Hamburg" ⇒ City(cityName).asRight
      case _ ⇒ UnknownCity(cityName).asLeft
    }

  def hottestCity: State[Requests, (City, Temperature)] =
    State { reqs ⇒
      val t = Requests.hottest(reqs).map(_.temperature)
      (reqs, t)
    }

  def askCity: Task[String] = for {
    _ <- printLn("What is the next city?")
    cityName <- readLn
  } yield cityName

  def weather(city: City)(
    host: String,
    port: Int
  ): Task[Forecast] = Task.delay {
    new WeatherClient(host, port).forecast(city)
  }

  def fetchForecast(city: City)(host: String, port: Int) =
    for {
      maybeForecast <- StateT { reqs: Requests ⇒
        Task.now((reqs, reqs.get(city)))
      }
      forecast <- StateT { reqs: Requests ⇒
        maybeForecast
          .fold(weather(city)(host, port))(_.pure[Task])
          .map(forecast ⇒ (reqs, forecast))
      }
      _ <- StateT { reqs: Requests ⇒
        Task.now((reqs + (city -> forecast), ()))
      }
    } yield forecast

  def askFetchJudge = for {
    cityName <- askCity
    city <- cityByName(cityName)
    h <- host
    p <- port
    forecast <- fetchForecast(city)(h, p)
    _ <- printLn(s"Forecast for $city is ${forecast.temperature}")
    hottest <- hottestCity
    _ <- s"Hottest city found so far is $hottest"
  } yield ()

  def program = for {
    h <- host
    p <- port
    _ <- printLn(s"Using weather service at http://$h:$p")
    _ <- askFetchJudge.forever
  } yield ()
  */
}
