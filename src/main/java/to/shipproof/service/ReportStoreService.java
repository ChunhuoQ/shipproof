package to.shipproof.service;

import org.springframework.stereotype.Service;
import to.shipproof.model.ScanReport;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ReportStoreService {
    private static final int MAX_REPORTS = 100;
    private final Map<String, ScanReport> reports = new ConcurrentHashMap<String, ScanReport>();
    private final Deque<String> insertionOrder = new ArrayDeque<String>();

    public synchronized void put(ScanReport report) {
        if (!reports.containsKey(report.getReportId())) insertionOrder.addLast(report.getReportId());
        reports.put(report.getReportId(), report);
        while (insertionOrder.size() > MAX_REPORTS) {
            reports.remove(insertionOrder.removeFirst());
        }
    }

    public ScanReport get(String reportId) {
        ScanReport report = reports.get(reportId);
        if (report == null) throw new IllegalArgumentException("Report not found or expired");
        return report;
    }
}
