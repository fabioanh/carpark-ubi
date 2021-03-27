package com.ubitricity.carparkubi.services;

import com.ubitricity.carparkubi.model.ChargingPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;

class CarparkUbiTest {

    private CarparkUbi carparkUbi;

    @BeforeEach
    public void setUp() {
        carparkUbi = new CarparkUbi();
    }

    @Test
    public void connect_oneChargingPointConnected_twentyAmpsForChargingPoint() {
        // given
        // when
        // then
    }

    @Test
    public void connect_fiveChargingPointsConnected_twentyAmpsForEachChargingPoint() {
        // given
        // when
        // then
    }

    @Test
    public void connect_sixChargingPointsConnected_properCurrentDistribution() {
        // given
        // when
        // then
    }

    @Test
    public void connect_nineChargingPointsConnected_properCurrentDistribution() {
        // given
        // when
        // then
    }

    @Test
    public void connect_tenChargingPointsConnected_properCurrentDistribution() {
        // given
        // when
        // then
    }

    @Test
    public void disconnect_singleChargingPointDisconnected_allChargingPointsDisconnected() {
        // given
        // when
        // then
    }

    @Test
    public void disconnect_threeCarsLeftConnected_properCurrentDistribution() {
        // given
        // when
        // then
    }

    @Test
    public void disconnect_fiveCarsLeftConnected_properCurrentDistribution() {
        // given
        // when
        // then
    }

    @Test
    public void disconnect_nineCarsLeftConnected_properCurrentDistribution() {
        // given
        // when
        // then
    }

    @Test
    public void describe_initialState_allChargingPointsListed() {
        // given
        // when
        List<ChargingPoint> report = carparkUbi.describe();
        // then
        assertThat(report).hasSize(CarparkUbi.NUM_CHARGE_POINTS);
        report.forEach(cp -> {
            assertThat(cp.getConnected()).isFalse();
            assertThat(cp.getIdentifier()).isNotEmpty();
            assertThat(cp.getCurrent()).isEqualTo(0);
        });
    }

    @Test
    public void describe_oneChargingPointConnected_allChargingPointsListed() {
        // given
        connectChargingPoints(1);
        // when
        List<ChargingPoint> report = carparkUbi.describe();
        // then
        assertThat(report).hasSize(CarparkUbi.NUM_CHARGE_POINTS);
        assertThat(report).contains(new ChargingPoint("CP3", 20, true));
        report.stream()
                .filter(cp -> cp.getIdentifier().equals("CP3"))
                .findFirst()
                .ifPresent((cp) -> {
                    assertThat(cp.getConnected()).isTrue();
                    assertThat(cp.getCurrent()).isEqualTo(20);
                });
        report.stream()
                .filter(cp -> !cp.getIdentifier().equals("CP3"))
                .forEach(cp -> {
                    assertThat(cp.getConnected()).isFalse();
                    assertThat(cp.getIdentifier()).isNotEmpty();
                    assertThat(cp.getCurrent()).isEqualTo(0);
                });

    }

    @Test
    public void describe_allChargingPointsConnected_allChargingPointsListed() {
        // given
        connectChargingPoints(10);
        // when
        List<ChargingPoint> report = carparkUbi.describe();
        // then
        assertThat(report).hasSize(CarparkUbi.NUM_CHARGE_POINTS);
        report.forEach(cp -> {
            assertThat(cp.getConnected()).isTrue();
            assertThat(cp.getIdentifier()).isNotEmpty();
        });
    }

    @Test
    public void describe_chargingPointDisconnected_allChargingPointsPresent() {
        // given
        connectChargingPoints(7);
        carparkUbi.disconnect("CP10");
        // when
        List<ChargingPoint> report = carparkUbi.describe();
        // then
        assertThat(report).hasSize(CarparkUbi.NUM_CHARGE_POINTS);
        report.stream()
                .filter(cp -> cp.getIdentifier().equals("CP10"))
                .findFirst()
                .ifPresent((cp) -> {
                    assertThat(cp.getConnected()).isFalse();
                    assertThat(cp.getCurrent()).isEqualTo(0);
                });
    }

    /**
     * Connect as many charging points as instructed by the input parameter.
     * Order of connection from most recent to oldest: 3-5-10-9-2-7-1-4-6-8
     *
     * @param n number of charging points to connect
     */
    private void connectChargingPoints(int n) {
        switch (n) {
            case 10:
                carparkUbi.connect("CP8");
            case 9:
                carparkUbi.connect("CP6");
            case 8:
                carparkUbi.connect("CP4");
            case 7:
                carparkUbi.connect("CP1");
            case 6:
                carparkUbi.connect("CP7");
            case 5:
                carparkUbi.connect("CP2");
            case 4:
                carparkUbi.connect("CP9");
            case 3:
                carparkUbi.connect("CP10");
            case 2:
                carparkUbi.connect("CP5");
            case 1:
                carparkUbi.connect("CP3");
                return;
            default:
                throw new IllegalStateException("Unsupported number of connected charging points");
        }
    }
}