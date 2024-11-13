package com.bkm009.video_editor.controllers;

import com.bkm009.video_editor.constants.ApplicationConstants;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApplicationConstants.BASE_APP_URL)
public class HealthController {

    @GetMapping("/ping")
    public String ping(){
        return "Video Editor Application";
    }
}
