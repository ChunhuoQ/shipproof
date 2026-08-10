package to.shipproof.service;

import to.shipproof.model.ScanFinding;
import to.shipproof.model.ScanReport;
import to.shipproof.model.SubmissionRequest;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;

@Service
public class ShipProofService {
    private final ProjectProbeService probes;

    public ShipProofService(ProjectProbeService probes) {
        this.probes = probes;
    }

    public ScanReport scan(SubmissionRequest request) {
        long started = System.currentTimeMillis();
        Map<String, Object> repository = probes.probeGithub(request.getRepositoryUrl());
        Map<String, Object> demo = probes.probeDemo(request.getDemoUrl());
        String readme = string(repository.get("readme"));
        String corpus = (readme + "\n" + request.getPitch()).toLowerCase(Locale.ROOT);

        List<ScanFinding> findings = new ArrayList<ScanFinding>();
        Map<String, Integer> scorecard = new LinkedHashMap<String, Integer>();
        int hardFailures = 0;

        boolean repoLive = bool(repository.get("reachable"));
        add(findings, "PUBLIC_REPO", repoLive ? "PASS" : "BLOCKER", "Public repository",
                repoLive ? "GitHub returned a public repository on the default branch."
                        : string(repository.get("error"), "The repository could not be verified as public."),
                "Publish the repository and verify the URL in a signed-out browser.", repoLive ? 20 : 0, 20, repoLive);
        if (!repoLive) hardFailures++;

        boolean demoLive = bool(demo.get("reachable"));
        int observed = number(demo.get("bytesObserved"));
        boolean substantiveDemo = demoLive && observed >= 300;
        add(findings, "LIVE_DEMO", substantiveDemo ? "PASS" : "BLOCKER", "Anonymous live demo",
                substantiveDemo ? "HTTP " + demo.get("status") + " returned " + observed + " observed characters in " + demo.get("latencyMs") + " ms."
                        : (demoLive ? "The URL responded but returned too little content to verify a usable demo."
                        : string(demo.get("error"), "The demo URL was not reachable.")),
                "Deploy a public, permission-free demo and test the main path in an incognito window.", substantiveDemo ? 20 : 0, 20, substantiveDemo);
        if (!substantiveDemo) hardFailures++;

        boolean hasRunbook = containsAny(corpus, "quickstart", "quick start", "getting started", "install", "run locally", "docker run", "npm run", "mvn ");
        boolean hasLicense = !string(repository.get("license")).isEmpty() || containsAny(corpus, "## license", "# license", "mit license", "apache license");
        int reproducibility = (hasRunbook ? 9 : 0) + (hasLicense ? 6 : 0);
        boolean reproducible = hasRunbook && hasLicense;
        add(findings, "REPRODUCIBLE", reproducible ? "PASS" : "HIGH", "Reproducible build",
                "Run instructions: " + yesNo(hasRunbook) + " · license: " + yesNo(hasLicense) + ".",
                "Add exact local setup commands, required environment variables, expected output, and an OSI license.",
                reproducibility, 15, reproducible);
        if (!hasRunbook) hardFailures++;

        int evidenceSignals = countMatches(corpus, "screenshot", "demo video", "architecture", "api", "test", "benchmark", "transaction", "contract address", "limitations");
        List<?> evidenceLinks = repository.get("evidenceLinks") instanceof List ? (List<?>) repository.get("evidenceLinks") : Collections.emptyList();
        int brokenEvidenceLinks = brokenLinks(evidenceLinks);
        int evidencePoints = evidenceSignals >= 4 ? 15 : evidenceSignals >= 2 ? 10 : evidenceSignals == 1 ? 5 : 0;
        if (!evidenceLinks.isEmpty() && brokenEvidenceLinks == 0) evidencePoints = Math.min(15, evidencePoints + 5);
        if (brokenEvidenceLinks > 0) evidencePoints = Math.max(0, evidencePoints - 5);
        boolean evidenceRich = evidenceSignals >= 2 && brokenEvidenceLinks == 0;
        add(findings, "CLAIM_EVIDENCE", evidenceRich ? "PASS" : "HIGH", "Evidence-backed claims",
                evidenceSignals + " evidence marker" + (evidenceSignals == 1 ? "" : "s") + " found · "
                        + evidenceLinks.size() + " key link" + (evidenceLinks.size() == 1 ? "" : "s")
                        + " checked · " + brokenEvidenceLinks + " broken.",
                "Connect every major claim to a screenshot, test, benchmark, transaction, architecture note, or live endpoint.",
                evidencePoints, 15, evidenceRich);

        String pitch = request.getPitch().trim();
        boolean specificPitch = pitch.length() >= 180 && containsAny(pitch.toLowerCase(Locale.ROOT),
                "for ", "teams", "builders", "founders", "users", "agents", "developer");
        int pitchPoints = pitch.length() >= 180 ? 6 : pitch.length() >= 100 ? 3 : 0;
        if (specificPitch) pitchPoints = 10;
        add(findings, "PITCH_CLARITY", specificPitch ? "PASS" : "MEDIUM", "Specific problem and user",
                "Pitch length: " + pitch.length() + " characters · explicit user signal: " + yesNo(specificPitch) + ".",
                "State who has the problem, what fails today, the measurable outcome, and why this implementation is different.",
                pitchPoints, 10, specificPitch);

        boolean safety = containsAny(corpus, "security", "privacy", "limitation", "threat", "human approval", "human-in-the-loop", "fail closed", "failure", "ssrf", "permission");
        add(findings, "SAFETY_BOUNDARY", safety ? "PASS" : "MEDIUM", "Safety and failure boundaries",
                safety ? "The submission documents at least one explicit safety, privacy, or failure boundary."
                        : "No explicit safety, privacy, or failure-boundary section was detected.",
                "Document rejected inputs, network boundaries, permissions, data retention, and one honest limitation.",
                safety ? 10 : 0, 10, safety);

        boolean social = request.getSocialUrl() != null && request.getSocialUrl().trim().matches("https://.+");
        boolean track = request.getTrack() != null && !request.getTrack().trim().isEmpty();
        int completionPoints = (social ? 5 : 0) + (track ? 5 : 0);
        boolean complete = social && track;
        add(findings, "SUBMISSION_PACK", complete ? "PASS" : "MEDIUM", "Submission pack completeness",
                "Public launch post: " + yesNo(social) + " · primary track: " + yesNo(track) + ".",
                "Add a public launch post and name one primary track before the deadline.",
                completionPoints, 10, complete);

        scorecard.put("demo", substantiveDemo ? 20 : 0);
        scorecard.put("repository", repoLive ? 20 : 0);
        scorecard.put("reproducibility", reproducibility);
        scorecard.put("evidence", evidencePoints);
        scorecard.put("pitch", pitchPoints);
        scorecard.put("safety", safety ? 10 : 0);
        scorecard.put("submission", completionPoints);

        int score = sum(scorecard.values());
        int passed = 0;
        for (ScanFinding finding : findings) if (finding.isPassed()) passed++;
        String verdict = hardFailures == 0 && score >= 80 ? "READY_TO_SUBMIT"
                : hardFailures > 0 || score < 55 ? "BLOCKED" : "NEEDS_WORK";

        Map<String, Object> signals = new LinkedHashMap<String, Object>();
        signals.put("repository", withoutReadme(repository));
        signals.put("demo", demo);
        signals.put("readmeCharacters", readme.length());
        signals.put("evidenceMarkers", evidenceSignals);
        signals.put("evidenceLinksChecked", evidenceLinks.size());
        signals.put("brokenEvidenceLinks", brokenEvidenceLinks);
        signals.put("primaryTrack", string(request.getTrack(), "not supplied"));

        String fingerprint = request.getProjectName() + "|" + request.getRepositoryUrl() + "|"
                + request.getDemoUrl() + "|" + request.getPitch() + "|" + score;
        String reportId = "spr_" + sha256(fingerprint).substring(0, 16);
        return new ScanReport(reportId, request.getProjectName().trim(), Instant.now(),
                System.currentTimeMillis() - started, score, verdict, passed, findings.size(),
                hardFailures, scorecard, signals, findings);
    }

    private Map<String, Object> withoutReadme(Map<String, Object> source) {
        Map<String, Object> result = new LinkedHashMap<String, Object>(source);
        result.remove("readme");
        return result;
    }

    private void add(List<ScanFinding> findings, String id, String severity, String title,
                     String evidence, String remediation, int points, int max, boolean passed) {
        findings.add(new ScanFinding(id, severity, title, evidence, remediation, points, max, passed));
    }

    private boolean containsAny(String value, String... needles) {
        for (String needle : needles) if (value.contains(needle)) return true;
        return false;
    }

    private int countMatches(String value, String... needles) {
        int count = 0;
        for (String needle : needles) if (value.contains(needle)) count++;
        return count;
    }

    private int brokenLinks(List<?> links) {
        int broken = 0;
        for (Object item : links) {
            if (!(item instanceof Map) || !Boolean.TRUE.equals(((Map<?, ?>) item).get("reachable"))) broken++;
        }
        return broken;
    }

    private int sum(Collection<Integer> values) {
        int sum = 0;
        for (Integer value : values) sum += value == null ? 0 : value;
        return sum;
    }

    private boolean bool(Object value) { return Boolean.TRUE.equals(value); }
    private int number(Object value) { return value instanceof Number ? ((Number) value).intValue() : 0; }
    private String string(Object value) { return value == null ? "" : String.valueOf(value); }
    private String string(Object value, String fallback) {
        String result = string(value);
        return result.trim().isEmpty() ? fallback : result;
    }
    private String yesNo(boolean value) { return value ? "yes" : "no"; }

    private String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            for (byte b : digest) result.append(String.format("%02x", b));
            return result.toString();
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
