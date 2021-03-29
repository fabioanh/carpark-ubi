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
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
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
        ResultActions response = this.mockMvc.perform(RestDocumentationRequestBuilders.put("/carparks/{carparkName}/chargingPoints/{chargingPointId}", "ubi", "CP3")
                .content(postBody)
                .contentType("application/json")
                .characterEncoding("utf-8"));
        // then
        response.andExpect(status().isOk())
                .andExpect(content().json(expectedJson))
                .andDo(document("connect-charging-point",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("carparkName").description("Identifier of the carpark. For now only the value **'ubi'** is allowed"),
                                parameterWithName("chargingPointId").description("Identifier of the charging point to be connected/disconnected.")
                        ),
                        requestFields(
                                fieldWithPath("connected").type("Boolean").description("The `true` value indicates the charging point should be connected")
                        ),
                        responseFields(
                                fieldWithPath("id").type("String").description("Identifier of the affected charging point"),
                                fieldWithPath("current").type("Number").description("Current assigned to the charging point"),
                                fieldWithPath("connected").type("Boolean").description("Value should be `true` as a connect operation was done")
                        )
                ));
    }

    @Test
    public void updateChargingPoint_attemptConnectingNonExistentDevice_errorResponse() throws Exception {
        // given
        var postBody = """
                {
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
        response.andExpect(status().isNotFound())
                .andDo(document("charging-point-not-found",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                        )
                );
    }

    @Test
    public void updateChargingPoint_regularDisconnectRequest_successfulResponse() throws Exception {
        // given
        var postBody = """
                {
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
        ResultActions response = this.mockMvc.perform(RestDocumentationRequestBuilders.put("/carparks/{carparkName}/chargingPoints/{chargingPointId}", "ubi", "CP3")
                .content(postBody)
                .contentType("application/json")
                .characterEncoding("utf-8"));
        // then
        response.andExpect(status().isOk())
                .andExpect(content().json(expectedJson))
                .andDo(document("disconnect-charging-point",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("carparkName").description("Identifier of the carpark. For now only the value **'ubi'** is allowed"),
                                parameterWithName("chargingPointId").description("Identifier of the charging point to be connected/disconnected.")
                        ),
                        requestFields(
                                fieldWithPath("connected").type("Boolean").description("The `false` value indicates the charging point should be disconnected")
                        ),
                        responseFields(
                                fieldWithPath("id").type("String").description("Identifier of the affected charging point"),
                                fieldWithPath("current").type("Number").description("Current assigned to the charging point should be `0` as the charging point was disconnected"),
                                fieldWithPath("connected").type("Boolean").description("Value should be `false` as it was disconnected")
                        ))
                );
    }

    @Test
    public void updateChargingPoint_attemptDisconnectingNonExistentDevice_errorResponse() throws Exception {
        // given
        var postBody = """
                {
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
        response.andExpect(status().isNotFound());
    }

    @Test
    public void updateChargingPoint_nonExistentCarpark_notFoundErrorResponse() throws Exception {
        // given
        var postBody = """
                {
                    "connected": true
                }
                """;
        // when
        ResultActions response = this.mockMvc.perform(RestDocumentationRequestBuilders.put("/carparks/{carparkName}/chargingPoints/{chargingPointId}", "non_existent_carpark", "CP3")
                .content(postBody)
                .contentType("application/json")
                .characterEncoding("utf-8"));
        // then
        response.andExpect(status().isNotFound())
                .andDo(document("carpark-not-found",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                        )
                );
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
        ResultActions response = this.mockMvc.perform(RestDocumentationRequestBuilders.get("/carparks/{carparkName}/chargingPoints", "ubi")
                .contentType("application/json")
                .characterEncoding("utf-8"));
        // then
        response.andExpect(status().isOk())
                .andExpect(content().json(expectedJson))
                .andDo(document("get-charging-points",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("carparkName").description("Identifier of the carpark. For now only the value **'ubi'** is allowed")
                        ),
                        responseFields(
                                fieldWithPath("[].id").type("String").description("Identifier of the charging point"),
                                fieldWithPath("[].current").type("Number").description("Current assigned to the charging point"),
                                fieldWithPath("[].connected").type("Boolean").description("Indicates whether the charging point is connected or disconnected")
                        ))
                );
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