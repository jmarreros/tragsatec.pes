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
    @Value("${app.name}")
    private String appName;

    @Value("${app.version}")
    private String appVersion;

    @Value("${app.year}")
    private String appYear;

    @Value("${app.footer}")
    private String appFooter;

    @GetMapping("/info")
    public Map<String, String> getAppInfo() {
        return Collections.unmodifiableMap(
                Map.of(
                        "name", appName,
                        "version", appVersion,
                        "year", appYear,
                        "footer", appFooter
                )
        );
    }
}
