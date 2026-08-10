package to.shipproof.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ProjectProbeService {
    private final RestTemplate restTemplate;
    private final SafeUrlService safeUrls;

    public ProjectProbeService(RestTemplate restTemplate, SafeUrlService safeUrls) {
        this.restTemplate = restTemplate;
        this.safeUrls = safeUrls;
    }

    public Map<String, Object> probeDemo(String rawUrl) {
        URI uri = safeUrls.requirePublicHttpUrl(rawUrl, "Demo URL");
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("url", uri.toString());
        long started = System.currentTimeMillis();
        try {
            HttpHeaders headers = requestHeaders();
            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET,
                    new HttpEntity<String>(headers), String.class);
            int status = response.getStatusCodeValue();
            String body = response.getBody() == null ? "" : response.getBody();
            result.put("reachable", status >= 200 && status < 400);
            result.put("status", status);
            result.put("contentType", response.getHeaders().getContentType() == null ? "unknown"
                    : response.getHeaders().getContentType().toString());
            result.put("bytesObserved", body.length());
        } catch (HttpStatusCodeException e) {
            result.put("reachable", false);
            result.put("status", e.getRawStatusCode());
            result.put("error", "Remote server returned HTTP " + e.getRawStatusCode());
        } catch (RuntimeException e) {
            result.put("reachable", false);
            result.put("status", 0);
            result.put("error", concise(e.getMessage()));
        }
        result.put("latencyMs", System.currentTimeMillis() - started);
        return result;
    }

    public Map<String, Object> probeGithub(String rawUrl) {
        URI uri = safeUrls.requirePublicHttpUrl(rawUrl, "Repository URL");
        String host = uri.getHost().toLowerCase(Locale.ROOT);
        if (!("github.com".equals(host) || "www.github.com".equals(host))) {
            throw new IllegalArgumentException("Repository URL must be a public GitHub repository");
        }
        String[] segments = uri.getPath().replaceAll("^/|/$", "").split("/");
        if (segments.length < 2) throw new IllegalArgumentException("Repository URL must include owner and repository");
        String owner = segments[0];
        String repo = segments[1].replaceAll("\\.git$", "");
        URI api = safeUrls.requirePublicHttpUrl("https://api.github.com/repos/" + owner + "/" + repo, "GitHub API URL");
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("url", "https://github.com/" + owner + "/" + repo);
        result.put("owner", owner);
        result.put("repository", repo);
        long started = System.currentTimeMillis();
        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(api, HttpMethod.GET,
                    new HttpEntity<String>(requestHeaders()), JsonNode.class);
            JsonNode data = response.getBody();
            boolean exists = data != null && !data.path("private").asBoolean(true);
            result.put("reachable", exists);
            result.put("status", response.getStatusCodeValue());
            result.put("description", data == null ? "" : data.path("description").asText(""));
            result.put("stars", data == null ? 0 : data.path("stargazers_count").asInt(0));
            result.put("forks", data == null ? 0 : data.path("forks_count").asInt(0));
            result.put("openIssues", data == null ? 0 : data.path("open_issues_count").asInt(0));
            result.put("defaultBranch", data == null ? "" : data.path("default_branch").asText(""));
            result.put("license", data == null ? "" : data.path("license").path("spdx_id").asText(""));
            result.put("updatedAt", data == null ? "" : data.path("pushed_at").asText(""));
            String readme = fetchReadme(owner, repo);
            result.put("readme", readme);
            result.put("evidenceLinks", inspectEvidenceLinks(readme));
        } catch (HttpStatusCodeException e) {
            result.put("reachable", false);
            result.put("status", e.getRawStatusCode());
            result.put("error", "GitHub returned HTTP " + e.getRawStatusCode());
            result.put("readme", "");
        } catch (RuntimeException e) {
            result.put("reachable", false);
            result.put("status", 0);
            result.put("error", concise(e.getMessage()));
            result.put("readme", "");
        }
        result.put("latencyMs", System.currentTimeMillis() - started);
        return result;
    }

    private String fetchReadme(String owner, String repo) {
        try {
            URI uri = safeUrls.requirePublicHttpUrl("https://api.github.com/repos/" + owner + "/" + repo + "/readme", "GitHub README URL");
            HttpHeaders headers = requestHeaders();
            headers.setAccept(java.util.Collections.singletonList(MediaType.parseMediaType("application/vnd.github.raw+json")));
            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET,
                    new HttpEntity<String>(headers), String.class);
            String body = response.getBody() == null ? "" : response.getBody();
            return body.length() > 120000 ? body.substring(0, 120000) : body;
        } catch (RuntimeException e) {
            return "";
        }
    }

    private List<Map<String, Object>> inspectEvidenceLinks(String readme) {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        Pattern markdownLink = Pattern.compile("\\[([^\\]]{1,80})\\]\\((https?://[^\\s)]+)\\)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = markdownLink.matcher(readme == null ? "" : readme);
        while (matcher.find() && results.size() < 3) {
            String label = matcher.group(1).trim();
            String lower = label.toLowerCase(Locale.ROOT);
            if (!(lower.contains("demo") || lower.contains("live") || lower.contains("video")
                    || lower.contains("docs") || lower.contains("website") || lower.contains("app"))) continue;
            String url = matcher.group(2).replace("&amp;", "&");
            Map<String, Object> evidence = new LinkedHashMap<String, Object>();
            evidence.put("label", label);
            evidence.put("url", url);
            try {
                URI uri = safeUrls.requirePublicHttpUrl(url, "README evidence link");
                ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.HEAD,
                        new HttpEntity<String>(requestHeaders()), String.class);
                evidence.put("reachable", response.getStatusCodeValue() >= 200 && response.getStatusCodeValue() < 400);
                evidence.put("status", response.getStatusCodeValue());
            } catch (HttpStatusCodeException e) {
                evidence.put("reachable", false);
                evidence.put("status", e.getRawStatusCode());
            } catch (RuntimeException e) {
                evidence.put("reachable", false);
                evidence.put("status", 0);
                evidence.put("error", concise(e.getMessage()));
            }
            results.add(evidence);
        }
        return results;
    }

    private HttpHeaders requestHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "ShipProof/1.0 (+https://github.com/shipproof)");
        headers.setAccept(java.util.Collections.singletonList(MediaType.ALL));
        return headers;
    }

    private String concise(String value) {
        if (value == null || value.trim().isEmpty()) return "Connection failed";
        int colon = value.indexOf(':');
        String result = colon > 0 ? value.substring(0, colon) : value;
        return result.length() > 140 ? result.substring(0, 140) : result;
    }
}
