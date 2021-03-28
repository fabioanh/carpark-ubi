package com.ubitricity.carparkubi.controllers;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(CarparksController.CARPARKS)
public class CarparksController {
    public static final String CARPARKS = "carparks";
    private static final String CHARGING_POINTS = "chargingPoints";

    @PutMapping(path ="/{carparkName}/" + CHARGING_POINTS + "{chargingPointId}")
    public void updateChargingPoint(@PathVariable String carparkName,
                              @PathVariable String chargingPointId,
                              @RequestBody ChargingPointDTO chargingPoint) {
    }

    @GetMapping(path = "/{carparkName}/"+CHARGING_POINTS)
    public void getChargingPointsReport(@PathVariable String carparkName){

    }

}
