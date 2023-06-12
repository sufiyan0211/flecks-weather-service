package com.flecks.service;

import com.flecks.model.Player;
import com.flecks.model.WeatherData;
import com.flecks.model.responseType.TimeWeather;
import com.flecks.model.responseType.WeatherInfo;
import com.playfab.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author sofiyan
 */
@Service
public class WeatherService {
    private Player player = new Player();
    private final PlayFabClientAPI playFabClient;
    private final RestTemplate restTemplate;

    private final String DEVELOPER_NAME = "Mohammed Abdul Sofiyan";

    @Value("${weather.api.key}")
    private String WEATHER_API_KEY;


    public WeatherService(PlayFabClientAPI playFabClient, RestTemplate restTemplate) {
        this.playFabClient = playFabClient;
        this.restTemplate = restTemplate;
    }

    public PlayFabErrors.PlayFabResult performLogin(Player newPlayer) {
        PlayFabClientModels.LoginWithEmailAddressRequest request = new PlayFabClientModels.LoginWithEmailAddressRequest();

        request.Email = newPlayer.getEmail();
        request.Password = newPlayer.getPassword();

        PlayFabErrors.PlayFabResult playFabResult = playFabClient.LoginWithEmailAddress(request);

        String id = ((PlayFabClientModels.LoginResult) playFabResult.Result).PlayFabId;

        player.setId(id);
        player.setEmail(newPlayer.getEmail());
        player.setPassword(newPlayer.getPassword());

        return playFabResult;
    }

    public PlayFabErrors.PlayFabResult performSignup(Player newPlayer) {
        PlayFabClientModels.RegisterPlayFabUserRequest request = new PlayFabClientModels.RegisterPlayFabUserRequest();

        request.Username = newPlayer.getUsername();
        request.Email = newPlayer.getEmail();
        request.Password = newPlayer.getPassword();

        PlayFabErrors.PlayFabResult playFabResult = playFabClient.RegisterPlayFabUser(request);


        if (playFabResult.Error != null) {
            return playFabResult;
        }

        String id = ((PlayFabClientModels.RegisterPlayFabUserResult) playFabResult.Result).PlayFabId;

        player.setId(id);
        player.setEmail(newPlayer.getEmail());
        player.setPassword(newPlayer.getPassword());


        PlayFabClientModels.UpdateUserDataRequest userDataRequest = new PlayFabClientModels.UpdateUserDataRequest();
        Map<String, String> DeveloperName = new HashMap<>();
        DeveloperName.put("DeveloperName", DEVELOPER_NAME);
        userDataRequest.Data = DeveloperName;

        PlayFabErrors.PlayFabResult playFabResultUserData = playFabClient.UpdateUserData(userDataRequest);

        return playFabResultUserData;
    }

    public WeatherInfo findWeather() {
        WeatherInfo weatherInfo = new WeatherInfo();
        PlayFabClientModels.UpdateUserDataRequest request = new PlayFabClientModels.UpdateUserDataRequest();

        PlayFabClientModels.PlayerProfileViewConstraints profileConstraints = new PlayFabClientModels.PlayerProfileViewConstraints();
        profileConstraints.ShowLocations = true;

        PlayFabClientModels.GetPlayerProfileRequest playerProfileRequest = new PlayFabClientModels.GetPlayerProfileRequest();
        playerProfileRequest.PlayFabId = player.getId();
        playerProfileRequest.ProfileConstraints = profileConstraints;

        PlayFabErrors.PlayFabResult<PlayFabClientModels.GetPlayerProfileResult> playerProfile = playFabClient.GetPlayerProfile(playerProfileRequest);

        // if there is no error fetching Player's profile
        if (playerProfile.Error == null) {
            weatherInfo = fetchWeatherInformation(weatherInfo, playerProfile, request);
        }
        return weatherInfo;
    }


    public WeatherInfo fetchWeatherInformation(WeatherInfo weatherInfo,
                                               PlayFabErrors.PlayFabResult<PlayFabClientModels.GetPlayerProfileResult> playerProfile,
                                               PlayFabClientModels.UpdateUserDataRequest request) {
        ArrayList<PlayFabClientModels.LocationModel> listOfLocations = playerProfile.Result.PlayerProfile.Locations;
        if (!listOfLocations.isEmpty()) {
            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
            PlayFabClientModels.LocationModel location = listOfLocations.get(listOfLocations.size() - 1);


            WeatherData weatherData =
                    restTemplate.getForObject("https://api.openweathermap.org/data/2.5/weather?lat=" + location.Latitude
                                    + "&lon=" + location.Longitude
                                    + "&appid=" + WEATHER_API_KEY + "&units=imperial"
                            , WeatherData.class);

            weatherInfo.setCity(location.City);
            weatherInfo.setCountry(weatherData.getSys().getCountry());
            weatherInfo.setTemperatureUnit("F"); // as we passed `&units=imperial`
            weatherInfo.setTemperature(String.valueOf(weatherData.getMain().getTemp()));
            weatherInfo.setWeatherDescription(weatherData.getWeather().get(0).getDescription());
            weatherInfo.setHumidityPercentage(String.valueOf(weatherData.getMain().getHumidity()));
            weatherInfo.setWindSpeed(String.valueOf(weatherData.getWind().getSpeed()));

            Map<String, String> timeTemperatureMap = new HashMap<>();
            timeTemperatureMap.put(currentTime.toString(), weatherInfo.getTemperature());
            request.Data = timeTemperatureMap;
            PlayFabErrors.PlayFabResult playFabResult = playFabClient.UpdateUserData(request);
        }
        return weatherInfo;
    }

    public List<TimeWeather> getTemperatureHistory() {
        PlayFabClientModels.GetUserDataRequest request = new PlayFabClientModels.GetUserDataRequest();
        request.PlayFabId = player.getId();

        PlayFabErrors.PlayFabResult<PlayFabClientModels.GetUserDataResult> userData = playFabClient.GetUserData(request);

        // if error not occurs
        List<TimeWeather> timeWeatherList = new ArrayList<>();
        if (userData.Error == null) {
            Map<String, PlayFabClientModels.UserDataRecord> userDataMap = userData.Result.Data;
            for (Map.Entry<String, PlayFabClientModels.UserDataRecord> userDataEntry : userDataMap.entrySet()) {
                if(userDataEntry.getKey().equals("DeveloperName")) continue;
                TimeWeather timeWeather = new TimeWeather();
                timeWeather.setTime(userDataEntry.getKey());
                timeWeather.setTemperature(userDataEntry.getValue().Value);
                timeWeatherList.add(timeWeather);
            }
        }

        return timeWeatherList;
    }

    public String getDeveloperName() {
        PlayFabClientModels.GetUserDataRequest request = new PlayFabClientModels.GetUserDataRequest();
        request.PlayFabId = player.getId();

        PlayFabErrors.PlayFabResult<PlayFabClientModels.GetUserDataResult> userData = playFabClient.GetUserData(request);

        String name = "";
        if (userData.Error == null) {
            Map<String, PlayFabClientModels.UserDataRecord> userDataMap = userData.Result.Data;

            name = userDataMap.get("DeveloperName").Value;
        }
        return name;
    }

}
