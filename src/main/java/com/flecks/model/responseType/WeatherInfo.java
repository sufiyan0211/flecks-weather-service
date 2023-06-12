package com.flecks.model.responseType;

import lombok.Data;

@Data
public class WeatherInfo {
    private String weatherDescription;
    private String temperature;
    private String temperatureUnit;
    private String humidityPercentage;
    private String windSpeed;
    private String city;
    private String country;
}
