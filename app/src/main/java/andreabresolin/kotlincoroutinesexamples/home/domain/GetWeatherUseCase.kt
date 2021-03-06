/*
 *  Copyright 2018 Andrea Bresolin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package andreabresolin.kotlincoroutinesexamples.home.domain

import andreabresolin.kotlincoroutinesexamples.app.domain.BaseUseCase
import andreabresolin.kotlincoroutinesexamples.app.model.CityWeather
import andreabresolin.kotlincoroutinesexamples.app.model.LoadedCityWeather
import andreabresolin.kotlincoroutinesexamples.app.network.model.CurrentWeather
import andreabresolin.kotlincoroutinesexamples.app.repository.WeatherRepository
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.delay
import java.util.*

class GetWeatherUseCase constructor(
        private val weatherRepository: WeatherRepository) : BaseUseCase() {

    class GetWeatherException constructor(val cityAndCountry: String) : RuntimeException()

    suspend fun execute(cityAndCountry: String): CityWeather {
       val weather: CurrentWeather? = asyncAwait {
            simulateSlowNetwork()
            weatherRepository.getCurrentWeather(cityAndCountry)
        }

        return mapCurrentWeatherToCityWeather(weather, cityAndCountry)
    }

    suspend fun execute(citiesAndCountries: List<String>): List<CityWeather> {
        return citiesAndCountries
                .map { getCityWeather(it) }
                .mapIndexed { index, deferred ->
                    mapCurrentWeatherToCityWeather(deferred.await(), citiesAndCountries[index])
                }
    }

    private suspend fun getCityWeather(cityAndCountry: String): Deferred<CurrentWeather?> {
        return async {
            simulateSlowNetwork()
            weatherRepository.getCurrentWeather(cityAndCountry)
        }
    }

    private suspend fun simulateSlowNetwork() {
        // Random delay used to simulate a slow network connection
        delay(1000 + Random().nextInt(4000).toLong())
    }

    private fun mapCurrentWeatherToCityWeather(weather: CurrentWeather?, cityAndCountry: String): CityWeather {
        return LoadedCityWeather(
                weather?.name ?: throw GetWeatherException(cityAndCountry),
                weather.weather?.get(0)?.description ?: throw GetWeatherException(cityAndCountry),
                weather.main?.temp ?: throw GetWeatherException(cityAndCountry),
                weather.weather.get(0)?.icon)
    }
}