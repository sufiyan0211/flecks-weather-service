package com.flecks.model.responseType;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class TimeWeather {
    private String time;
    private String temperature;
}
