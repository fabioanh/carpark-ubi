package com.ubitricity.carparkubi.services;

import com.ubitricity.carparkubi.model.ChargingPoint;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class CarparkUbi {
    public static final int NUM_CHARGE_POINTS = 10;
    private final List<ChargingPoint> chargingQueue = Collections.synchronizedList(new LinkedList<>());
    private final Set<String> chargingPointIds;

    public CarparkUbi() {
        chargingPointIds = fixedChargePointIds();
    }

    public ChargingPoint connect(String chargingPointId) {
        return null;
    }

    public ChargingPoint disconnect(String chargingPointId) {
        return null;
    }

    /**
     * Check the connected Charging Points and re-distribute the charge between them accordingly
     */
    private void redistributeCharge() {

    }

    /**
     * @return List of all information for all Charging Points in this carpark station
     */
    public List<ChargingPoint> describe() {
        return null;
    }

    /**
     * Creates a set of ids for the charging points available in the current Car-Park
     *
     * @return Set of fixed charging point ids to be used by the car-park
     */
    private Set<String> fixedChargePointIds() {
        return IntStream.rangeClosed(1, NUM_CHARGE_POINTS)
                .mapToObj(String::valueOf)
                .map(n -> "CP" + n)
                .collect(Collectors.toSet());
    }

}
