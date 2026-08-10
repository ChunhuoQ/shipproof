#!/usr/bin/env sh
set -eu

: "${SHIPPROOF_BASE_URL:?Set SHIPPROOF_BASE_URL to the deployed ShipProof origin}"
: "${SHIPPROOF_REPO_URL:?Set SHIPPROOF_REPO_URL to the public GitHub repository}"
: "${SHIPPROOF_DEMO_URL:?Set SHIPPROOF_DEMO_URL to the public demo}"
: "${SHIPPROOF_SOCIAL_URL:?Set SHIPPROOF_SOCIAL_URL to the public launch post}"

payload=$(jq -n \
  --arg repositoryUrl "$SHIPPROOF_REPO_URL" \
  --arg demoUrl "$SHIPPROOF_DEMO_URL" \
  --arg socialUrl "$SHIPPROOF_SOCIAL_URL" \
  '{
    projectName: "ShipProof",
    repositoryUrl: $repositoryUrl,
    demoUrl: $demoUrl,
    socialUrl: $socialUrl,
    track: "OPC / Super Individuals",
    pitch: "ShipProof helps hackathon builders and autonomous agent teams find broken demos, missing evidence, unsafe boundaries, and incomplete submission packs before judges review them. It collects public evidence from GitHub, README links, and the deployed product, applies deterministic hard gates, returns a prioritized repair queue, and produces a verifiable SHA-256 linked report receipt."
  }')

curl --fail-with-body --silent --show-error \
  --request POST "$SHIPPROOF_BASE_URL/api/v1/scan" \
  --header 'Content-Type: application/json' \
  --data "$payload" \
  | tee shipproof-self-audit.json \
  | jq '{reportId, score, verdict, hardGateFailures, passedChecks, totalChecks, proof: .proof.sha256}'
