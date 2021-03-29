package com.ubitricity.carparkubi.services;

import com.ubitricity.carparkubi.model.ChargingPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        ChargingPoint connectedChargingPoint = carparkUbi.connect("CP6");
        // then
        assertThat(connectedChargingPoint.getConnected()).isTrue();
        assertThat(connectedChargingPoint.getCurrent()).isEqualTo(20);
        List<ChargingPoint> report = carparkUbi.describe();
        report.stream()
                .filter(cp -> cp.getIdentifier().equals("CP6"))
                .findFirst()
                .ifPresent(cp -> {
                    assertThat(cp.getConnected()).isTrue();
                    assertThat(cp.getCurrent()).isEqualTo(20);
                });
    }

    @Test
    public void connect_fiveChargingPointsConnected_twentyAmpsForEachChargingPoint() {
        // given
        connectChargingPoints(4);
        // when
        ChargingPoint connectedChargingPoint = carparkUbi.connect("CP8");
        // then
        assertThat(connectedChargingPoint.getConnected()).isTrue();
        assertThat(connectedChargingPoint.getCurrent()).isEqualTo(20);
        List<ChargingPoint> report = carparkUbi.describe();
        report.stream()
                .filter(cp -> cp.getIdentifier().equals("CP8")
                        || cp.getIdentifier().equals("CP9")
                        || cp.getIdentifier().equals("CP10")
                        || cp.getIdentifier().equals("CP5")
                        || cp.getIdentifier().equals("CP3"))
                .forEach(cp -> {
                    assertThat(cp.getConnected()).isTrue();
                    assertThat(cp.getCurrent()).isEqualTo(20);
                });
    }

    @Test
    public void connect_sixChargingPointsConnected_properCurrentDistribution() {
        // given
        connectChargingPoints(5);
        // when
        ChargingPoint connectedChargingPoint = carparkUbi.connect("CP6");
        // then
        assertThat(connectedChargingPoint.getConnected()).isTrue();
        assertThat(connectedChargingPoint.getCurrent()).isEqualTo(20);
        List<ChargingPoint> report = carparkUbi.describe();
        report.stream()
                .filter(cp -> cp.getIdentifier().equals("CP2") || cp.getIdentifier().equals("CP9"))
                .forEach(cp -> {
                    assertThat(cp.getConnected()).isTrue();
                    assertThat(cp.getCurrent()).isEqualTo(10);
                });
        report.stream()
                .filter(cp -> cp.getIdentifier().equals("CP6")
                        || cp.getIdentifier().equals("CP3")
                        || cp.getIdentifier().equals("CP5")
                        || cp.getIdentifier().equals("CP10"))
                .forEach(cp -> {
                    assertThat(cp.getConnected()).isTrue();
                    assertThat(cp.getCurrent()).isEqualTo(20);
                });
    }

    @Test
    public void connect_nineChargingPointsConnected_properCurrentDistribution() {
        // given
        connectChargingPoints(8);
        // when
        ChargingPoint connectedChargingPoint = carparkUbi.connect("CP8");
        // then
        assertThat(connectedChargingPoint.getConnected()).isTrue();
        assertThat(connectedChargingPoint.getCurrent()).isEqualTo(20);
        List<ChargingPoint> report = carparkUbi.describe();
        report.stream()
                .filter(cp -> cp.getIdentifier().equals("CP8"))
                .findFirst()
                .ifPresent(cp -> {
                    assertThat(cp.getConnected()).isTrue();
                    assertThat(cp.getCurrent()).isEqualTo(20);
                });
        report.stream()
                .filter(cp -> !(cp.getIdentifier().equals("CP8")
                        || cp.getIdentifier().equals("CP6")))
                .forEach(cp -> {
                    assertThat(cp.getConnected()).isTrue();
                    assertThat(cp.getCurrent()).isEqualTo(10);
                });
    }

    @Test
    public void connect_tenChargingPointsConnected_properCurrentDistribution() {
        // given
        // when
        connectChargingPoints(10);
        // then
        List<ChargingPoint> report = carparkUbi.describe();
        report.forEach(cp -> {
            assertThat(cp.getConnected()).isTrue();
            assertThat(cp.getCurrent()).isEqualTo(10);
        });
    }

    @Test
    public void connect_multiThreadSamePointConnected_onlyOneConnectionAllowed() throws InterruptedException, NoSuchFieldException, IllegalAccessException {
        // given
        ExecutorService service = Executors.newFixedThreadPool(30);
        // when
        IntStream.range(0, 30)
                .forEach(count -> service.submit(() -> carparkUbi.connect("CP3")));
        service.awaitTermination(1000, TimeUnit.MILLISECONDS);
        // then
        Field chargingQueueField = carparkUbi.getClass().getDeclaredField("chargingQueue");
        chargingQueueField.setAccessible(true);
        List<ChargingPoint> chargingQueue = (List<ChargingPoint>) chargingQueueField.get(carparkUbi);
        assertThat(chargingQueue).hasSize(1);
    }

    @Test
    public void connect_connectAlreadyConnectedChargingPoint_exception() {
        // given
        connectChargingPoints(1);
        // when
        ChargingPoint connectedChargingPoint = carparkUbi.connect("CP3");
        // then
        assertThat(connectedChargingPoint.getConnected()).isTrue();
        assertThat(connectedChargingPoint.getCurrent()).isEqualTo(20);
        List<ChargingPoint> report = carparkUbi.describe();
        report.stream()
                .filter(cp -> cp.getIdentifier().equals("CP3"))
                .findFirst()
                .ifPresent(cp -> {
                    assertThat(cp.getConnected()).isTrue();
                    assertThat(cp.getCurrent()).isEqualTo(20);
                });
    }

    @Test
    public void connect_connectNonExistingChargingPoint_exception() {
        // given
        connectChargingPoints(1);
        // when
        // then
        assertThrows(ChargingPointNotFoundException.class, () -> carparkUbi.connect("CP42"));
    }

    @Test
    public void disconnect_singleChargingPointDisconnected_allChargingPointsDisconnected() {
        // given
        connectChargingPoints(1);
        // when
        ChargingPoint disconnectedChargingPoint = carparkUbi.disconnect("CP3");
        // then
        assertThat(disconnectedChargingPoint.getCurrent()).isEqualTo(0);
        assertThat(disconnectedChargingPoint.getConnected()).isFalse();
        List<ChargingPoint> report = carparkUbi.describe();
        report.forEach(cp -> {
            assertThat(cp.getConnected()).isFalse();
            assertThat(cp.getCurrent()).isEqualTo(0);
        });
    }

    @Test
    public void disconnect_threeCarsLeftConnected_properCurrentDistribution() {
        // given
        connectChargingPoints(4);
        // when
        ChargingPoint disconnectedChargingPoint = carparkUbi.disconnect("CP9");
        // then
        assertThat(disconnectedChargingPoint.getCurrent()).isEqualTo(0);
        assertThat(disconnectedChargingPoint.getConnected()).isFalse();
        List<ChargingPoint> report = carparkUbi.describe();
        report.stream()
                .filter(cp -> cp.getIdentifier().equals("CP9")
                        || cp.getIdentifier().equals("CP7")
                        || cp.getIdentifier().equals("CP1")
                        || cp.getIdentifier().equals("CP4")
                        || cp.getIdentifier().equals("CP6")
                        || cp.getIdentifier().equals("CP2")
                        || cp.getIdentifier().equals("CP8"))
                .forEach(cp -> {
                    assertThat(cp.getConnected()).isFalse();
                    assertThat(cp.getCurrent()).isEqualTo(0);
                });
        report.stream()
                .filter(cp -> cp.getIdentifier().equals("CP3")
                        || cp.getIdentifier().equals("CP5")
                        || cp.getIdentifier().equals("CP10"))
                .forEach(cp -> {
                    assertThat(cp.getConnected()).isTrue();
                    assertThat(cp.getCurrent()).isEqualTo(20);
                });
    }

    @Test
    public void disconnect_fiveCarsLeftConnected_properCurrentDistribution() {
        // given
        connectChargingPoints(6);
        // when
        ChargingPoint disconnectedChargingPoint = carparkUbi.disconnect("CP9");
        // then
        assertThat(disconnectedChargingPoint.getCurrent()).isEqualTo(0);
        assertThat(disconnectedChargingPoint.getConnected()).isFalse();
        List<ChargingPoint> report = carparkUbi.describe();
        report.stream()
                .filter(cp -> cp.getIdentifier().equals("CP9")
                        || cp.getIdentifier().equals("CP1")
                        || cp.getIdentifier().equals("CP4")
                        || cp.getIdentifier().equals("CP6")
                        || cp.getIdentifier().equals("CP8"))
                .forEach(cp -> {
                    assertThat(cp.getConnected()).isFalse();
                    assertThat(cp.getCurrent()).isEqualTo(0);
                });
        report.stream()
                .filter(cp -> cp.getIdentifier().equals("CP3")
                        || cp.getIdentifier().equals("CP5")
                        || cp.getIdentifier().equals("CP10")
                        || cp.getIdentifier().equals("CP2")
                        || cp.getIdentifier().equals("CP7"))
                .forEach(cp -> {
                    assertThat(cp.getConnected()).isTrue();
                    assertThat(cp.getCurrent()).isEqualTo(20);
                });
    }

    @Test
    public void disconnect_nineCarsLeftConnected_properCurrentDistribution() {
        // given
        connectChargingPoints(10);
        // when
        ChargingPoint disconnectedChargingPoint = carparkUbi.disconnect("CP2");
        // then
        assertThat(disconnectedChargingPoint.getCurrent()).isEqualTo(0);
        assertThat(disconnectedChargingPoint.getConnected()).isFalse();
        List<ChargingPoint> report = carparkUbi.describe();
        report.stream()
                .filter(cp -> cp.getIdentifier().equals("CP2"))
                .findFirst()
                .ifPresent(cp -> {
                    assertThat(cp.getConnected()).isFalse();
                    assertThat(cp.getCurrent()).isEqualTo(0);
                });
        report.stream()
                .filter(cp -> cp.getIdentifier().equals("CP3"))
                .findFirst()
                .ifPresent(cp -> {
                    assertThat(cp.getConnected()).isTrue();
                    assertThat(cp.getCurrent()).isEqualTo(20);
                });
        report.stream()
                .filter(cp -> !(cp.getIdentifier().equals("CP3") || cp.getIdentifier().equals("CP2")))
                .forEach(cp -> {
                    assertThat(cp.getConnected()).isTrue();
                    assertThat(cp.getCurrent()).isEqualTo(10);
                });
    }

    @Test()
    public void disconnect_disconnectNonConnectedChargingPoint_exception() {
        // given
        connectChargingPoints(1);
        // when
        ChargingPoint disconnectedChargingPoint = carparkUbi.disconnect("CP5");
        // then
        assertThat(disconnectedChargingPoint.getCurrent()).isEqualTo(0);
        assertThat(disconnectedChargingPoint.getConnected()).isFalse();
    }

    @Test()
    public void disconnect_disconnectNonExistingChargingPoint_exception() {
        // given
        connectChargingPoints(1);
        // when
        // then
        assertThrows(ChargingPointNotFoundException.class, () -> carparkUbi.disconnect("CP42"));
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
                .ifPresent(cp -> {
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
                .ifPresent(cp -> {
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