package to.shipproof.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import to.shipproof.service.ProjectProbeService;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ApiControllerTest {
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;
    @MockBean private ProjectProbeService probes;

    @BeforeEach
    void publicEvidence() {
        Map<String, Object> repository = new LinkedHashMap<String, Object>();
        repository.put("reachable", true);
        repository.put("license", "MIT");
        repository.put("readme", "Quick start: mvn test. Architecture API screenshot benchmark security limitations.");
        Map<String, Object> demo = new LinkedHashMap<String, Object>();
        demo.put("reachable", true);
        demo.put("status", 200);
        demo.put("bytesObserved", 4000);
        demo.put("latencyMs", 40);
        when(probes.probeGithub(anyString())).thenReturn(repository);
        when(probes.probeDemo(anyString())).thenReturn(demo);
    }

    @Test
    void createsAndReloadsShareableReport() throws Exception {
        String response = mvc.perform(post("/api/v1/scan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verdict").value("READY_TO_SUBMIT"))
                .andExpect(jsonPath("$.proof.sha256").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        JsonNode body = mapper.readTree(response);
        String reportId = body.path("reportId").asText();
        mvc.perform(get("/api/v1/reports/{id}", reportId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportId").value(reportId))
                .andExpect(jsonPath("$.proof.algorithm").value("SHA-256 linked receipt"));
    }

    @Test
    void rejectsIncompleteContract() throws Exception {
        mvc.perform(post("/api/v1/scan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"projectName\":\"Only a name\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"));
    }

    private String validRequest() throws Exception {
        Map<String, Object> request = new LinkedHashMap<String, Object>();
        request.put("projectName", "ShipProof");
        request.put("repositoryUrl", "https://github.com/acme/shipproof");
        request.put("demoUrl", "https://shipproof.example");
        request.put("track", "OPC / Super Individuals");
        request.put("socialUrl", "https://x.com/acme/status/123");
        request.put("pitch", "ShipProof helps hackathon builders and autonomous agent teams detect broken demos, weak evidence, unsafe boundaries, and missing runbooks before judges review them. It performs deterministic checks and returns a prioritized repair queue with a verifiable audit receipt.");
        return mapper.writeValueAsString(request);
    }
}
