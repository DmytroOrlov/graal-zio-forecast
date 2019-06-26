package zioapp

import app.{City, Forecast, WeatherClient}
import zio._
import capture.Capture
import zioapp.error.WeatherErr

package object weather extends Weather.Service[Weather] {
  def forecast(client: WeatherClient, city: City): ZIO[Weather, Capture[WeatherErr], Forecast] =
    ZIO.accessM(_.weather.forecast(client, city))
}
