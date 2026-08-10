package to.shipproof.service;

import to.shipproof.model.ScanReport;
import to.shipproof.model.SubmissionRequest;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ShipProofServiceTest {
    @Test
    void awardsReadyVerdictOnlyWhenHardGatesAndEvidencePass() {
        ProjectProbeService probes = mock(ProjectProbeService.class);
        Map<String, Object> repository = new LinkedHashMap<String, Object>();
        repository.put("reachable", true);
        repository.put("license", "MIT");
        repository.put("readme", "# Product\n## Quick start\nmvn test\n## Architecture\nAPI screenshot benchmark test security limitations");
        Map<String, Object> demo = new LinkedHashMap<String, Object>();
        demo.put("reachable", true);
        demo.put("status", 200);
        demo.put("bytesObserved", 5000);
        demo.put("latencyMs", 80);
        when(probes.probeGithub(anyString())).thenReturn(repository);
        when(probes.probeDemo(anyString())).thenReturn(demo);

        SubmissionRequest request = request();
        ScanReport report = new ShipProofService(probes).scan(request);

        assertEquals("READY_TO_SUBMIT", report.getVerdict());
        assertEquals(100, report.getScore());
        assertEquals(0, report.getHardGateFailures());
        assertEquals(7, report.getPassedChecks());
        assertTrue(report.getReportId().startsWith("spr_"));
    }

    @Test
    void blocksSubmissionWhenDemoCannotBeVerified() {
        ProjectProbeService probes = mock(ProjectProbeService.class);
        Map<String, Object> repository = new LinkedHashMap<String, Object>();
        repository.put("reachable", true);
        repository.put("license", "MIT");
        repository.put("readme", "Quick start install architecture API tests security limitations");
        Map<String, Object> demo = new LinkedHashMap<String, Object>();
        demo.put("reachable", false);
        demo.put("status", 404);
        demo.put("error", "Remote server returned HTTP 404");
        when(probes.probeGithub(anyString())).thenReturn(repository);
        when(probes.probeDemo(anyString())).thenReturn(demo);

        ScanReport report = new ShipProofService(probes).scan(request());

        assertEquals("BLOCKED", report.getVerdict());
        assertTrue(report.getHardGateFailures() >= 1);
        assertFalse(report.getFindings().get(1).isPassed());
    }

    @Test
    void penalizesBrokenKeyEvidenceLink() {
        ProjectProbeService probes = mock(ProjectProbeService.class);
        Map<String, Object> brokenLink = new LinkedHashMap<String, Object>();
        brokenLink.put("label", "Live demo video");
        brokenLink.put("url", "https://example.com/missing");
        brokenLink.put("reachable", false);
        Map<String, Object> repository = new LinkedHashMap<String, Object>();
        repository.put("reachable", true);
        repository.put("license", "MIT");
        repository.put("readme", "Quick start install architecture API screenshot benchmark test security limitations");
        repository.put("evidenceLinks", Collections.singletonList(brokenLink));
        Map<String, Object> demo = new LinkedHashMap<String, Object>();
        demo.put("reachable", true);
        demo.put("status", 200);
        demo.put("bytesObserved", 5000);
        demo.put("latencyMs", 80);
        when(probes.probeGithub(anyString())).thenReturn(repository);
        when(probes.probeDemo(anyString())).thenReturn(demo);

        ScanReport report = new ShipProofService(probes).scan(request());

        assertEquals(1, report.getSignals().get("brokenEvidenceLinks"));
        assertEquals(10, report.getScorecard().get("evidence"));
        assertFalse(report.getFindings().get(3).isPassed());
    }

    private SubmissionRequest request() {
        SubmissionRequest request = new SubmissionRequest();
        request.setProjectName("ShipProof");
        request.setRepositoryUrl("https://github.com/acme/shipproof");
        request.setDemoUrl("https://shipproof.example");
        request.setTrack("Autonomous Agents");
        request.setSocialUrl("https://x.com/acme/status/123");
        request.setPitch("ShipProof helps hackathon builders and autonomous agent teams find broken demos, missing evidence, and unsafe claims before judges review a submission. It performs deterministic public checks, returns a prioritized repair queue, and produces a verifiable receipt for every scan.");
        return request;
    }
}
