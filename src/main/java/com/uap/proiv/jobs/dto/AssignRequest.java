package com.uap.proiv.jobs.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class AssignRequest {
    @NotNull
    @JsonProperty("requestNumber")
    private Integer requestNumber;
    @NotEmpty
    @JsonProperty("clientName")
    private String clientName;

    public Integer getRequestNumber() {
        return requestNumber;
    }

    public void setRequestNumber(Integer requestNumber) {
        this.requestNumber = requestNumber;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    @Override
    public String toString() {
        return "AssignRequest{" +
                "requestNumber=" + requestNumber +
                ", clientName='" + clientName + '\'' +
                '}';
    }
}
