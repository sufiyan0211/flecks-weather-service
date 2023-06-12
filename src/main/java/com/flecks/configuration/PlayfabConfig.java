package com.flecks.configuration;

import com.playfab.PlayFabAdminAPI;
import com.playfab.PlayFabClientAPI;
import com.playfab.PlayFabSettings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PlayfabConfig {

    @Value("${playFab.developer.secretKey}")
    private String playFabDeveloperSecretKey;

    @Value("${playFab.titleId}")
    private String playFabTitleId;

    @Bean
    public PlayFabClientAPI playFabClient() {
        PlayFabSettings.DeveloperSecretKey = playFabDeveloperSecretKey;
        PlayFabSettings.TitleId = playFabTitleId;
        return new PlayFabClientAPI();
    }

}
