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

    public ChargingPoint connect(ChargingPoint chargingPoint) {
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
                .collect(Collectors.toSet());
    }

}
