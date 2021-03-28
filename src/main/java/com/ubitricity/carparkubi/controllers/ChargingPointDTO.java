package com.ubitricity.carparkubi.controllers;

import com.ubitricity.carparkubi.model.ChargingPoint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChargingPointDTO {
    String id;
    Integer current;
    Boolean connected;

    public ChargingPointDTO(ChargingPoint chargingPoint) {
        this(chargingPoint.getIdentifier(), chargingPoint.getCurrent(), chargingPoint.getConnected());
    }
}
