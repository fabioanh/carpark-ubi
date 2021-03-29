package com.ubitricity.carparkubi.controllers;

import com.ubitricity.carparkubi.services.CarparkUbi;
import com.ubitricity.carparkubi.services.ChargingPointNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(CarparksController.CARPARKS)
public class CarparksController {
    public static final String CARPARKS = "carparks";
    private static final String CHARGING_POINTS = "chargingPoints";

    private final CarparkUbi carparkUbi;

    public CarparksController(CarparkUbi carparkUbi) {
        this.carparkUbi = carparkUbi;
    }

    @PutMapping(path = "/{carparkName}/" + CHARGING_POINTS + "/{chargingPointId}")
    public ChargingPointDTO updateChargingPoint(@PathVariable String carparkName,
                                                @PathVariable String chargingPointId,
                                                @RequestBody ChargingPointDTO chargingPoint) {
        if (CarparkUbi.NAME.equals(carparkName)) {
            try {
                if (chargingPoint.getConnected()) {
                    return new ChargingPointDTO(carparkUbi.connect(chargingPointId));
                }
                return new ChargingPointDTO(carparkUbi.disconnect(chargingPointId));
            } catch (ChargingPointNotFoundException e) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Charging point not found", null);
            }
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Carpark not found", null);
    }

    @GetMapping(path = "/{carparkName}/" + CHARGING_POINTS)
    public List<ChargingPointDTO> getChargingPointsReport(@PathVariable String carparkName) {
        if (carparkName.equals(CarparkUbi.NAME)) {
            return carparkUbi.describe()
                    .stream()
                    .map(ChargingPointDTO::new)
                    .collect(Collectors.toList());
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Carpark not found", null);
    }

}
