# BUIDL_QUESTS 2026 Submission Pack

## Project

**ShipProof — Prove it before you pitch it**

## Primary track

**OPC / Super Individuals**

Secondary fit: Autonomous Agents.

## One-liner

ShipProof cold-opens a hackathon project like a skeptical judge and turns public evidence
into a hard-gate verdict, prioritized repair queue, and verifiable report receipt.

## Short description

Builders experience their project with local context, saved credentials, and weeks of
background knowledge. Judges get a cold browser and a few minutes. ShipProof closes that
gap. It probes the public demo and GitHub repository from the server, checks
reproducibility, claim evidence, pitch clarity, safety boundaries, and submission
completeness, then produces an inspectable 100-point score. Missing demos, private repos,
and absent runbooks fail closed. Every audit receives a SHA-256 linked receipt.

## Why this is an autonomous agent

ShipProof accepts a goal—determine whether this submission is externally judge-ready—then
collects evidence across independent public systems, normalizes the observations, applies
bounded decision rules, and returns the next actions in priority order. It is autonomous
where evidence collection is safe and deterministic, and explicitly refuses private
network targets or unverifiable claims.

## Why OPC

One builder can use ShipProof as the missing release, QA, security, and submission-review
team. The product itself was designed as a focused Agentic Service: one input contract,
one valuable result, no consulting call required.

## Differentiation

- Not another generic README reviewer: it makes real network and GitHub API observations.
- Deterministic hard gates cannot be negotiated by a persuasive model response.
- Each finding contains the observed evidence and an actionable repair.
- The product protects itself against localhost/private-network SSRF.
- Every report leaves a verifiable SHA-256 linked receipt.

## Demo script

**0–10s:** “Most hackathon failures are invisible to the builder and obvious to the
judge. ShipProof reproduces the judge's cold open.”

**10–30s:** Load a public sample and run the scan. Point out live GitHub metadata and demo
latency.

**30–45s:** Show the verdict, weighted score, hard-gate failures, and repair queue.

**45–55s:** Download the evidence report and show its linked SHA-256 receipt.

**55–60s:** Enter `localhost`; ShipProof refuses the target. “An autonomous reviewer must
also have boundaries.”

## Links to complete

- Live demo: `https://YOUR-SHIPPROOF-DEPLOYMENT`
- Repository: `https://github.com/ChunhuoQ/shipproof`
- Demo video: `https://...`
- X launch post: `https://x.com/YOUR-ACCOUNT/status/...`
- OpenArena project: `https://openarena.to/...`

## Launch post draft

> Hackathon teams see their project with saved credentials and weeks of context. Judges
> see a cold browser and broken links. I built ShipProof: an autonomous preflight agent
> that verifies the public demo, repo, reproducibility, evidence, safety, and pitch—then
> returns a hard-gate verdict and signed audit receipt. Built for BUIDL_QUESTS 2026.
> Demo: [LINK] Repo: [LINK] #AutonomousAgents #OPC #BUIDLQUESTS

## Final checklist

- [x] Working scan API
- [x] Real demo and GitHub probes
- [x] Seven weighted checks and hard gates
- [x] SSRF/private-network protection
- [x] Downloadable JSON report
- [x] Tamper-evident audit receipt
- [x] Responsive web experience
- [x] Automated tests
- [x] Docker and Render deployment config
- [ ] Public GitHub repository URL
- [ ] Public deployment URL
- [ ] 60-second video URL
- [ ] Public X post URL
- [ ] OpenArena submission URL
