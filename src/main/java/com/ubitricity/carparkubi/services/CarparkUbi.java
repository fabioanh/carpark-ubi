package com.ubitricity.carparkubi.services;

import com.ubitricity.carparkubi.model.ChargingPoint;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Business logic to handle the Carpark-Ubi
 */
@Service
public class CarparkUbi {
    public static final int NUM_CHARGE_POINTS = 10;
    public static final int TOTAL_POWER = 100;
    public static final int MIN_ASSIGNABLE_CHARGE = TOTAL_POWER / NUM_CHARGE_POINTS;
    private final List<ChargingPoint> chargingQueue = new LinkedList<>();
    private final Set<String> chargingPointIds;

    public CarparkUbi() {
        chargingPointIds = fixedChargePointIds();
    }

    /**
     * Connect to a charging point in the Carpark Ubi redistributing the charge among the connected points.
     *
     * @param chargingPointId Identifier of the charging point to be connected
     * @return Connected charging point with its charge value updated
     */
    public synchronized ChargingPoint connect(String chargingPointId) {
        if (!chargingPointIds.contains(chargingPointId)) {
            throw new ChargingPointNotFoundException();
        }
        if (chargingQueue.stream().map(ChargingPoint::getIdentifier).anyMatch(id -> id.equals(chargingPointId))) {
            throw new IllegalStateException("Charging point is already in use");
        }
        ChargingPoint chargingPoint = new ChargingPoint(chargingPointId, 0, true);
        chargingQueue.add(0, chargingPoint);
        redistributeCharge();
        return chargingPoint;
    }

    /**
     * Disconnect to a charging point in the Carpark Ubi redistributing the charge among the connected points.
     *
     * @param chargingPointId Identifier of the charging point to be disconnected
     * @return Disconnected charging point with its disconnected values
     */
    public synchronized ChargingPoint disconnect(String chargingPointId) {
        if (!chargingPointIds.contains(chargingPointId)) {
            throw new IllegalStateException("Charging point not recognised in the Carpark-Ubi site");
        }
        if (chargingQueue.stream().map(ChargingPoint::getIdentifier).noneMatch(id -> id.equals(chargingPointId))) {
            throw new IllegalStateException("Charging point is already disconnected");
        }
        ChargingPoint chargingPoint = new ChargingPoint(chargingPointId, 0, false);
        chargingQueue.remove(chargingPoint);
        redistributeCharge();
        return chargingPoint;
    }

    /**
     * Check the connected Charging Points and re-distribute the charge between them accordingly
     */
    private void redistributeCharge() {
        int rest = (NUM_CHARGE_POINTS - chargingQueue.size()) * MIN_ASSIGNABLE_CHARGE;
        for (ChargingPoint cp : chargingQueue) {
            var additionalCharge = 0;
            if (rest > 0) {
                additionalCharge = MIN_ASSIGNABLE_CHARGE;
            }
            rest -= MIN_ASSIGNABLE_CHARGE;
            cp.setCurrent(MIN_ASSIGNABLE_CHARGE + additionalCharge);
        }
    }

    /**
     * @return List of all information for all Charging Points in this carpark station
     */
    public List<ChargingPoint> describe() {
        return chargingPointIds.stream()
                .map(id -> chargingQueue.stream()
                        .filter(cp -> cp.getIdentifier().equals(id))
                        .findFirst()
                        .orElse(new ChargingPoint(id, 0, false))
                ).collect(Collectors.toList());
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
