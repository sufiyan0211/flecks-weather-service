package com.flecks.controller;

import com.flecks.model.Player;
import com.flecks.model.responseType.TimeWeather;
import com.flecks.model.responseType.WeatherInfo;
import com.flecks.service.WeatherService;
import com.playfab.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

/**
 *
 * @author sofiyan
 */
@Controller
public class WeatherController {

    private final PlayFabClientAPI playFabClient;
    private final WeatherService weatherService;

    public WeatherController(PlayFabClientAPI playFabClient, WeatherService weatherService) {
        this.playFabClient = playFabClient;
        this.weatherService = weatherService;
    }

    @GetMapping("/")
    public String login(Model model) {
        model.addAttribute("player", new Player());
        return "login";
    }

    @GetMapping("/signup")
    public String signup(Model model) {
        model.addAttribute("player", new Player());
        return "signup";
    }


    /**
     * <p>
     * <a href="https://learn.microsoft.com/en-us/rest/api/playfab/client/authentication/register-playfab-user?view=playfab-rest">Documentation</a>
     * </p>
     *
     * @param player
     * @param model
     * @return
     */
    @PostMapping("/AfterSignup")
    public String afterSignup(@ModelAttribute Player player, Model model) {

        PlayFabErrors.PlayFabResult playFabResult = weatherService.performSignup(player);

        // if error occurs while signup then retry signup
        if (playFabResult.Error != null) {
            model.addAttribute("error", true);
            model.addAttribute("errorMessage", playFabResult.Error.errorMessage);
            model.addAttribute("player", new Player());
            return "signup";
        }

        return "login";
    }

    /**
     * <p>
     *     <a href="https://learn.microsoft.com/en-us/rest/api/playfab/client/authentication/login-with-playfab?view=playfab-rest">Documentation</a>
     * </p>
     * @param player
     * @param model
     * @return
     */
    @PostMapping("/AfterLogin")
    public String afterLogin(@ModelAttribute Player player, Model model) {

        PlayFabErrors.PlayFabResult playFabResult = weatherService.performLogin(player);

        // if error occurs while login then retry login
        if (playFabResult.Error != null) {
            model.addAttribute("error", true);
            model.addAttribute("errorMessage", playFabResult.Error.errorMessage);
            model.addAttribute("player", new Player());
            return "login";
        }

        return "index";
    }

    /**
     * <b>Description:</b> This method displays weather information of the player
     * @param model
     * @return
     */
    @PostMapping("/findWeather")
    public String findWeather(Model model) {
        if (PlayFabSettings.ClientSessionTicket == null) {
            model.addAttribute("error", true);
            model.addAttribute("errorMessage", "User must be logged in to see the page");
            model.addAttribute("player", new Player());
            return "login";
        }

        WeatherInfo weatherInfo = weatherService.findWeather();

        // if error occurs while login then retry login
        if ("".equals(weatherInfo.getCity()) || weatherInfo.getCity() == null) {
            model.addAttribute("error", true);
            model.addAttribute("errorMessage", "Error Fetching Weather Information");
            return "index";
        }
        model.addAttribute("weatherInformation", weatherInfo);
        return "showWeather";
    }


    /**
     * <p>
     *     <b>Description:</b> This fetches temperature History of the Player
     * </p>
     * @param model
     * @return
     */
    @PostMapping("/temperatureHistory")
    public String temperatureHistory(Model model) {
        if (PlayFabSettings.ClientSessionTicket == null) {
            model.addAttribute("error", true);
            model.addAttribute("errorMessage", "User must be logged in to see the page");
            model.addAttribute("player", new Player());
            return "login";
        }

        List<TimeWeather> temperatureHistory = weatherService.getTemperatureHistory();

        model.addAttribute("temperatureHistory", temperatureHistory);
        return "temperatureHistory";
    }

    /**
     * <p>
     *     <b>Description:</b> This fetches developers name from PlayFab
     * @param model
     * @return
     */
    @PostMapping("/aboutDeveloper")
    public String aboutDeveloper(Model model) {
        if (PlayFabSettings.ClientSessionTicket == null) {
            model.addAttribute("error", true);
            model.addAttribute("errorMessage", "User must be logged in to see the page");
            model.addAttribute("player", new Player());
            return "login";
        }

        String name = weatherService.getDeveloperName();

        model.addAttribute("aboutDeveloper", true);
        model.addAttribute("name", name);
        return "index";
    }

    /**
     * <p>
     *     <b>Description:</b> Logout functionality
     * </p>
     * @param model
     * @return
     */
    @PostMapping("/logout")
    public String logout(Model model) {
        if (PlayFabSettings.ClientSessionTicket == null) {
            model.addAttribute("error", true);
            model.addAttribute("errorMessage", "User must be logged in to see the page");
            model.addAttribute("player", new Player());
            return "login";
        }

        PlayFabSettings.ClientSessionTicket = null;
        model.addAttribute("player", new Player());
        return "login";
    }

}
