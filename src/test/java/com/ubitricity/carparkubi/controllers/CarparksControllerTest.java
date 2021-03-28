package com.ubitricity.carparkubi.controllers;

import com.ubitricity.carparkubi.model.ChargingPoint;
import com.ubitricity.carparkubi.services.CarparkUbi;
import com.ubitricity.carparkubi.services.ChargingPointNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@WebMvcTest(CarparksController.class)
class CarparksControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CarparkUbi carparkUbi;

    @BeforeEach
    public void setUp(WebApplicationContext webApplicationContext,
                      RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(documentationConfiguration(restDocumentation))
                .build();
    }

    @Test
    public void updateChargingPoint_regularConnectRequest_successfulResponse() throws Exception {
        // given
        var postBody = """
                {
                    "id": "CP3",
                    "connected": true
                }
                """;
        var expectedJson = """
                {
                    "id": "CP3",
                    "connected": true,
                    "current": 20
                }
                """;
        when(carparkUbi.connect("CP3"))
                .thenReturn(new ChargingPoint("CP3", 20, true));
        // when
        ResultActions response = this.mockMvc.perform(put("/carparks/ubi/chargingPoints/CP3")
                .content(postBody)
                .contentType("application/json")
                .characterEncoding("utf-8"));
        // then
        response.andExpect(status().isOk())
                .andExpect(content().json(expectedJson));
    }

    @Test
    public void updateChargingPoint_attemptConnectingConnectedDevice_errorResponse() throws Exception {
        // given
        var postBody = """
                {
                    "id": "CP3",
                    "connected": true
                }
                """;
        when(carparkUbi.connect("CP3"))
                .thenThrow(new IllegalStateException("Charging Point already connected"));
        // when
        ResultActions response = this.mockMvc.perform(put("/carparks/ubi/chargingPoints/CP3")
                .content(postBody)
                .contentType("application/json")
                .characterEncoding("utf-8"));
        // then
        response.andExpect(status().isConflict());
    }

    @Test
    public void updateChargingPoint_attemptConnectingNonExistentDevice_errorResponse() throws Exception {
        // given
        var postBody = """
                {
                    "id": "CP42",
                    "connected": true
                }
                """;
        when(carparkUbi.connect("CP42"))
                .thenThrow(new ChargingPointNotFoundException());
        // when
        ResultActions response = this.mockMvc.perform(put("/carparks/ubi/chargingPoints/CP42")
                .content(postBody)
                .contentType("application/json")
                .characterEncoding("utf-8"));
        // then
        response.andExpect(status().isNotFound());
    }

    @Test
    public void updateChargingPoint_regularDisconnectRequest_successfulResponse() throws Exception {
        // given
        var postBody = """
                {
                    "id": "CP3",
                    "connected": false
                }
                """;
        var expectedJson = """
                {
                    "id": "CP3",
                    "connected": false,
                    "current": 0
                }
                """;
        when(carparkUbi.disconnect("CP3"))
                .thenReturn(new ChargingPoint("CP3", 0, false));
        // when
        ResultActions response = this.mockMvc.perform(put("/carparks/ubi/chargingPoints/CP3")
                .content(postBody)
                .contentType("application/json")
                .characterEncoding("utf-8"));
        // then
        response.andExpect(status().isOk())
                .andExpect(content().json(expectedJson));
    }

    @Test
    public void updateChargingPoint_attemptDisconnectingDisconnectedDevice_errorResponse() throws Exception {
        // given
        var postBody = """
                {
                    "id": "CP3",
                    "connected": false
                }
                """;
        when(carparkUbi.disconnect("CP3"))
                .thenThrow(new IllegalStateException("Charging Point already connected"));
        // when
        ResultActions response = this.mockMvc.perform(put("/carparks/ubi/chargingPoints/CP3")
                .content(postBody)
                .contentType("application/json")
                .characterEncoding("utf-8"));
        // then
        response.andExpect(status().isConflict())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void updateChargingPoint_attemptDisconnectingNonExistentDevice_errorResponse() throws Exception {
        // given
        var postBody = """
                {
                    "id": "CP42",
                    "connected": false
                }
                """;
        when(carparkUbi.disconnect("CP42"))
                .thenThrow(new ChargingPointNotFoundException());
        // when
        ResultActions response = this.mockMvc.perform(put("/carparks/ubi/chargingPoints/CP42")
                .content(postBody)
                .contentType("application/json")
                .characterEncoding("utf-8"));
        // then
        response.andExpect(status().isNotFound())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void updateChargingPoint_nonExistentCarpark_notFoundErrorResponse() throws Exception {
        // given
        var postBody = """
                {
                    "id": "CP3",
                    "connected": true
                }
                """;
        // when
        ResultActions response = this.mockMvc.perform(put("/carparks/non_existent_carpark/chargingPoints/CP3")
                .content(postBody)
                .contentType("application/json")
                .characterEncoding("utf-8"));
        // then
        response.andExpect(status().isNotFound());
    }

    @Test
    public void getChargingPointsReport_regularRequest_successfulResponse() throws Exception {
        // given
        List<ChargingPoint> report = new ArrayList<>();
        report.add(new ChargingPoint("CP1", 20, true));
        IntStream.rangeClosed(2, 10)
                .forEach(n -> report.add(new ChargingPoint("CP" + n, 0, false)));
        when(carparkUbi.describe())
                .thenReturn(report);

        var expectedJson = """
                [
                    {
                        "id": "CP1",
                        "connected": true,
                        "current": 20
                    },
                    {
                        "id": "CP2",
                        "connected": false,
                        "current": 0
                    },
                    {
                        "id": "CP3",
                        "connected": false,
                        "current": 0
                    },
                    {
                        "id": "CP4",
                        "connected": false,
                        "current": 0
                    },
                    {
                        "id": "CP5",
                        "connected": false,
                        "current": 0
                    },
                    {
                        "id": "CP6",
                        "connected": false,
                        "current": 0
                    },
                    {
                        "id": "CP7",
                        "connected": false,
                        "current": 0
                    },
                    {
                        "id": "CP8",
                        "connected": false,
                        "current": 0
                    },
                    {
                        "id": "CP9",
                        "connected": false,
                        "current": 0
                    },
                    {
                        "id": "CP10",
                        "connected": false,
                        "current": 0
                    }
                ]
                """;
        // when
        ResultActions response = this.mockMvc.perform(get("/carparks/ubi/chargingPoints")
                .contentType("application/json")
                .characterEncoding("utf-8"));
        // then
        response.andExpect(status().isOk())
                .andExpect(content().json(expectedJson));
    }

    @Test
    public void getChargingPointsReport_nonExistentCarpark_notFoundErrorResponse() throws Exception {
        // given
        // when
        ResultActions response = this.mockMvc.perform(get("/carparks/non_existent_carpark/chargingPoints")
                .contentType("application/json")
                .characterEncoding("utf-8"));
        // then
        response.andExpect(status().isNotFound());
    }
}