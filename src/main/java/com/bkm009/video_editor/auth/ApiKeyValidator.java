package com.bkm009.video_editor.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ApiKeyValidator {

    @Value("${security.api.key}")
    private String API_KEY;

    public boolean validateApiKey(String key) {
        return API_KEY.equals(key);
    }
}
