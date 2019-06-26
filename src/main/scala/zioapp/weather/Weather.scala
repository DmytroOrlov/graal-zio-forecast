package zioapp.weather

import java.io.IOException

import app.{City, Forecast, WeatherClient}
import zio._
import capture.Capture
import zioapp.error.WeatherErr

trait Weather {
  def weather: Weather.Service[Any]
}

object Weather {

  trait Service[R] {
    def forecast(client: WeatherClient, city: City): ZIO[R, Capture[WeatherErr], Forecast]
  }

  trait Live extends Weather {
    val weather = new Service[Any] {
      def forecast(client: WeatherClient, city: City): IO[Capture[WeatherErr], Forecast] =
        IO.effect(client.forecast(city))
          .refineOrDie {
            case er: IOException ⇒ WeatherErr.weatherClient(er)
          }
    }
  }

}
