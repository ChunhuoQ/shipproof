# Security Policy

## Supported version

Security fixes are applied to the latest version on `main`.

## Reporting a vulnerability

Do not open a public issue for a vulnerability that could expose a private network or
enable request forgery. Send a private report to the repository owner with:

- the affected endpoint and input;
- the expected and observed behavior;
- a minimal reproduction that does not access third-party systems;
- the potential impact and any known mitigation.

## Scanner trust boundary

ShipProof accepts untrusted public URLs. It therefore:

- accepts only HTTP and HTTPS;
- resolves hosts and rejects loopback, link-local, site-local, multicast, `.local`, and
  `localhost` destinations;
- restricts repository inspection to public GitHub repositories;
- never sends repository credentials or user-provided authorization headers;
- never clones or executes scanned source code;
- retains only an in-memory report and a report identifier in the audit chain.

DNS is resolved again by the HTTP client after validation, so production operators should
also enforce an outbound network policy that blocks private address ranges. Application
validation is one layer, not a replacement for infrastructure egress controls.

## Known non-security guarantees

A reachable page is not proof that its complete user journey works. A README keyword is
not proof that a product claim is true. ShipProof reports these as evidence signals and
does not represent them as formal verification.
