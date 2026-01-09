package com.thordata.sdk;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * Strongly-typed response object for SERP API requests.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SerpResponse {
    @JsonProperty("code")
    public int code;

    @JsonProperty("status")
    public String status;

    @JsonProperty("organic")
    public List<Map<String, Object>> organicResults;

    @JsonProperty("local_results")
    public List<Map<String, Object>> localResults;

    @JsonProperty("search_metadata")
    public Map<String, Object> searchMetadata;

    // Google Shopping / Flights / etc specific fields can be accessed via this catch-all map
    // or you can add specific fields later.
    // For now, let's keep it simple but structured.

    @Override
    public String toString() {
        return "SerpResponse{code=" + code + ", status='" + status + "'}";
    }
}