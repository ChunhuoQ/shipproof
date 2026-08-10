# ShipProof

> Prove it before you pitch it.

ShipProof is an evidence-first preflight agent for hackathon submissions. Give it a
public GitHub repository, live demo, pitch, launch post, and primary track. It cold-opens
the project like a skeptical judge, applies seven deterministic checks, and returns a
score, hard-gate verdict, prioritized repair queue, and tamper-evident report receipt.

Built for **BUIDL_QUESTS 2026**, primarily for the **OPC / Super Individuals** and
**Autonomous Agents** tracks.

ShipProof is maintained as an independent hackathon project. Run commands from the
repository root, or from `shipproof/` when using the multi-project workspace; sibling
projects have separate dependencies and submission histories.

## The problem

Hackathon teams know their own project too well. Local credentials are already present,
broken setup steps are filled in from memory, and product claims sound obvious because
the builders wrote them. Judges see a different product: a cold browser, a short review
window, and no reason to trust an unsupported claim.

ShipProof makes that external view reproducible before submission.

## What the agent checks

| Check | Weight | Hard gate | Public evidence |
|---|---:|:---:|---|
| Anonymous live demo | 20 | Yes | HTTP response, body size, latency, content type |
| Public GitHub repository | 20 | Yes | GitHub API, default branch, recency, metadata |
| Reproducible build | 15 | Yes | Runbook and open-source license |
| Claim evidence | 15 | No | Tests, screenshots, benchmarks, architecture, and live README evidence links |
| Pitch clarity | 10 | No | Named user, current failure, specific outcome |
| Safety boundaries | 10 | No | Permissions, privacy, failures, limitations |
| Submission pack | 10 | No | One primary track and a public launch post |

The verdict is deliberately fail-closed:

- `READY_TO_SUBMIT`: score ≥ 80 and every hard gate passes.
- `NEEDS_WORK`: the public evidence exists but the package is not yet judge-ready.
- `BLOCKED`: a hard gate failed or the score is below 55.

## 60-second demo

1. Open ShipProof and load the public sample.
2. Run the seven-check audit.
3. Watch ShipProof reach the demo and GitHub from a clean server-side environment.
4. Show the weighted score and move directly to the failed checks.
5. Open one finding to show observed evidence and its concrete fix.
6. Download the JSON report and show its SHA-256 linked receipt.
7. Replace the demo URL with `http://localhost:3000` and show ShipProof fail closed.

## Architecture

```text
Browser
  │  POST /api/v1/scan
  ▼
Submission contract + validation
  │
  ├── SafeUrlService ── DNS resolution + private-network rejection
  │
  ├── Demo probe ────── reachability / status / body / latency
  │
  └── GitHub probe ──── repo metadata / README / license / recency
             │
             ▼
      Seven deterministic rules
             │
       score + hard gates
             │
             ▼
 repair queue + report fingerprint + SHA-256 linked audit receipt
```

ShipProof is deterministic first: a language model cannot silently change a hard gate or
invent missing evidence. AI can later explain or prioritize the observed findings, but
the pass/fail foundation remains inspectable.

## API

```bash
curl -X POST http://localhost:8080/api/v1/scan \
  -H 'Content-Type: application/json' \
  -d '{
    "projectName": "My Agent",
    "repositoryUrl": "https://github.com/owner/repository",
    "demoUrl": "https://demo.example.com",
    "pitch": "A specific explanation of the user, problem, product and evidence...",
    "socialUrl": "https://x.com/owner/status/123",
    "track": "Autonomous Agents"
  }'
```

Other endpoints:

```text
GET /api/v1/capabilities
GET /api/v1/reports/{reportId}
GET /api/v1/audit
GET /api/v1/audit/verify
```

## Run locally

Requirements: JDK 8+ and Maven 3.6+.

```bash
mvn clean test
mvn spring-boot:run
```

Open <http://localhost:8080>.

### Docker

```bash
docker build -t shipproof .
docker run --rm -p 8080:8080 shipproof
```

### Render

In the standalone ShipProof repository, create a Blueprint using `render.yaml`. From the
multi-project workspace, use `.render/shipproof.yaml`; it sets `rootDir: shipproof` so
Render builds only this project. The health path is `/api/v1/capabilities`.

## Security and privacy

Public URL scanners are easy to turn into SSRF tools. ShipProof resolves every submitted
host before access and rejects loopback, link-local, site-local, multicast, `.local`, and
`localhost` targets. Only HTTP and HTTPS are accepted. Repository scanning is restricted
to public GitHub URLs.

ShipProof does not request repository credentials, clone source code, execute project
code, or retain user secrets. The in-memory audit log records only the report identifier.

## Tests

```bash
mvn clean test
```

The suite covers ready and blocked verdicts, scoring, hard gates, local/private URL
rejection, and audit-chain verification.

## Self-audit before submission

After the repository, demo, and launch post are public, ShipProof can audit itself:

```bash
export SHIPPROOF_BASE_URL=https://your-deployment.example
export SHIPPROOF_REPO_URL=https://github.com/ChunhuoQ/shipproof
export SHIPPROOF_DEMO_URL=https://your-deployment.example
export SHIPPROOF_SOCIAL_URL=https://x.com/your-account/status/123
./scripts/self-audit.sh
```

The full report is saved as `shipproof-self-audit.json`; commit or attach it only after
checking that every URL is intended to be public.

## Honest limitations

- GitHub's unauthenticated API limit applies to the deployment IP.
- A reachable HTML page is not proof that every interaction works; browser-journey checks
  are a planned next step.
- Keyword evidence checks prove that a claim is documented, not that the claim is true.
- The linked receipt is a tamper-evident application audit chain, not an on-chain proof.
- Reports are shareable by URL during the current server lifetime; durable cross-restart
  history requires an external datastore.

These limitations are shown explicitly because ShipProof should meet its own standard.

## License

MIT
