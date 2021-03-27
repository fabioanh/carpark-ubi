package com.ubitricity.carparkubi.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ChargingPoint {
    @EqualsAndHashCode.Include
    private String identifier;
    private Integer current;
    private Boolean connected;
}