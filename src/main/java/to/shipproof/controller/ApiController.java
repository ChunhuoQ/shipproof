package to.shipproof.controller;

import to.shipproof.model.AuditEntry;
import to.shipproof.model.ScanReport;
import to.shipproof.model.SubmissionRequest;
import to.shipproof.service.AuditService;
import to.shipproof.service.ReportStoreService;
import to.shipproof.service.ShipProofService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class ApiController {
    private final ShipProofService shipProof;
    private final AuditService audit;
    private final ReportStoreService reports;

    public ApiController(ShipProofService shipProof, AuditService audit, ReportStoreService reports) {
        this.shipProof = shipProof;
        this.audit = audit;
        this.reports = reports;
    }

    @PostMapping("/scan")
    public ScanReport scan(@Valid @RequestBody SubmissionRequest request) {
        try {
            ScanReport report = shipProof.scan(request);
            AuditEntry entry = audit.record("submission.scan", report.getReportId(), true);
            Map<String, Object> proof = new LinkedHashMap<String, Object>();
            proof.put("auditSequence", entry.getSequence());
            proof.put("sha256", entry.getHash());
            proof.put("previousHash", entry.getPreviousHash());
            proof.put("algorithm", "SHA-256 linked receipt");
            report.setProof(proof);
            reports.put(report);
            return report;
        } catch (RuntimeException e) {
            audit.record("submission.scan", request.getProjectName(), false);
            throw e;
        }
    }

    @GetMapping("/reports/{reportId}")
    public ScanReport report(@PathVariable String reportId) {
        if (!reportId.matches("^spr_[a-f0-9]{16}$")) throw new IllegalArgumentException("Invalid report identifier");
        return reports.get(reportId);
    }

    @GetMapping("/audit")
    public List<AuditEntry> audit() { return audit.list(); }

    @GetMapping("/audit/verify")
    public Map<String, Object> verifyAudit() {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("valid", audit.verify());
        result.put("entries", audit.list().size());
        result.put("algorithm", "SHA-256 hash chain");
        return result;
    }

    @GetMapping("/capabilities")
    public Map<String, Object> capabilities() {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("name", "ShipProof");
        result.put("version", "1.0.0");
        result.put("runtime", "Java 8 compatible");
        result.put("action", "submission.scan");
        result.put("endpoint", "POST /api/v1/scan");
        result.put("checks", new String[]{"public repository", "anonymous demo", "reproducibility",
                "claim evidence", "pitch clarity", "safety boundaries", "submission pack"});
        return result;
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class, MethodArgumentNotValidException.class})
    public ResponseEntity<Map<String, Object>> handle(Exception exception) {
        boolean badRequest = exception instanceof IllegalArgumentException || exception instanceof MethodArgumentNotValidException;
        Map<String, Object> error = new LinkedHashMap<String, Object>();
        error.put("error", badRequest ? "BAD_REQUEST" : "SCAN_ERROR");
        error.put("message", exception instanceof MethodArgumentNotValidException
                ? "Project name, repository URL, demo URL, and pitch are required"
                : exception.getMessage());
        error.put("timestamp", Instant.now());
        return ResponseEntity.status(badRequest ? 400 : 502).body(error);
    }
}
