package com.ubitricity.carparkubi.controllers;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CarparksController {
    private static final String CARPARKS = "carparks";

    @PutMapping(path = CARPARKS+"/{name}")
    public void updateCarpark(@PathVariable String carparkName, @RequestBody Carpark carpark){
    }
}
