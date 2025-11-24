package com.chc.pes.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/app")
public class AppController {

    @Value("${app.version}")
    private String appVersion;

    @GetMapping("/version")
    public Map<String, String> getVersion() {
        return Collections.singletonMap("version", appVersion);
    }
}
