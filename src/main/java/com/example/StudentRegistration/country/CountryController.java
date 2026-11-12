package com.example.StudentRegistration.country;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Gives the frontend the list of countries for the nationality dropdown. */
@RestController
@RequestMapping("/api/countries")
public class CountryController {

    @GetMapping
    public List<String> countries() {
        return Countries.names();
    }
}
