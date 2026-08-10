package to.shipproof.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class ScanReport {
    private final String reportId;
    private final String projectName;
    private final Instant generatedAt;
    private final long durationMs;
    private final int score;
    private final String verdict;
    private final int passedChecks;
    private final int totalChecks;
    private final int hardGateFailures;
    private final Map<String, Integer> scorecard;
    private final Map<String, Object> signals;
    private final List<ScanFinding> findings;
    private Map<String, Object> proof;

    public ScanReport(String reportId, String projectName, Instant generatedAt, long durationMs,
                      int score, String verdict, int passedChecks, int totalChecks,
                      int hardGateFailures, Map<String, Integer> scorecard,
                      Map<String, Object> signals, List<ScanFinding> findings) {
        this.reportId = reportId;
        this.projectName = projectName;
        this.generatedAt = generatedAt;
        this.durationMs = durationMs;
        this.score = score;
        this.verdict = verdict;
        this.passedChecks = passedChecks;
        this.totalChecks = totalChecks;
        this.hardGateFailures = hardGateFailures;
        this.scorecard = scorecard;
        this.signals = signals;
        this.findings = findings;
    }

    public String getReportId() { return reportId; }
    public String getProjectName() { return projectName; }
    public Instant getGeneratedAt() { return generatedAt; }
    public long getDurationMs() { return durationMs; }
    public int getScore() { return score; }
    public String getVerdict() { return verdict; }
    public int getPassedChecks() { return passedChecks; }
    public int getTotalChecks() { return totalChecks; }
    public int getHardGateFailures() { return hardGateFailures; }
    public Map<String, Integer> getScorecard() { return scorecard; }
    public Map<String, Object> getSignals() { return signals; }
    public List<ScanFinding> getFindings() { return findings; }
    public Map<String, Object> getProof() { return proof; }
    public void setProof(Map<String, Object> proof) { this.proof = proof; }
}
