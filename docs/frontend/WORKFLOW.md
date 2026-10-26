# ClassicStore Frontend Workflow & Contribution Guidelines

This document outlines branch naming conventions, PR requirements, backend sync cadence, and rules for handling contract gaps.

---

## 1. Branch Naming & Merge Cadence

1. **Target Branch**:
   - All frontend work branches off `front-end-Development` and targets `front-end-Development`.
   - Branch names follow task IDs: `feat/t02-scaffold`, `docs/t01-foundation`, `fix/t08-auth-error`.

2. **Upstream Backend Synchronization**:
   - At the beginning of every task, merge `origin/main` into your feature branch (`git merge origin/main --no-edit`).
   - Use **merge commits**, NOT rebase, to preserve commit history and contract traceability.

---

## 2. Pull Request Template & Requirements

Every Pull Request targeting `front-end-Development` MUST include the following template sections filled out completely:

```markdown
## Summary
Brief explanation of changes implemented in this task.

## Backend Verification
- **Endpoint(s) Used**: `METHOD /path/to/endpoint`
- **Status on main**: [IMPLEMENTED / PARTIAL / STUB / SPEC-ONLY]
  - *Evidence File Path*: `backend/src/main/java/com/classicstore/...`
- **OpenAPI Compliance**: [YES / NO / N/A] (Validated against `docs/api/openapi.yaml`)
- **Mismatches / Gaps**: [None / Logged GAP-XX in `docs/frontend/contract-gaps.md`]

## Decisions Needing Owner Approval
- List any proposed decisions (e.g., ADR-004) or product ambiguities requiring owner sign-off.

## Verification Checklist
- [ ] TypeScript check passes (`npm run typecheck`)
- [ ] Unit & Component tests pass (`npm run test`)
- [ ] E2E Playwright tests pass for both **Arabic (RTL)** and **English (LTR)**
- [ ] Accessibility checks pass (`axe-core`)
- [ ] No files edited outside allowed paths (`/frontend`, `/docs/frontend/`)
```

---

## 3. Commit Message Convention

Commits must follow Conventional Commits standard:
- `feat(catalog): add product card component (T12)`
- `docs(adr): document auth BFF strategy in ADR-004 (T01)`
- `fix(cart): preserve string money format on subtotal display (T15)`

---

## 4. Contract Gap & Decision Protocol

1. **NEVER Modify Backend or OpenAPI Files**:
   - Frontend branches MUST NOT edit files in `/backend` or `docs/api/openapi.yaml`.
2. **Contract Gap Logging**:
   - If a backend API mismatch, missing endpoint, or broken DTO field is discovered, log it immediately in `docs/frontend/contract-gaps.md` with an assigned `GAP-xx` ID.
3. **Open Product Decisions**:
   - Flag unresolved business/design decisions in code with a `// OPEN-xx` comment and add a corresponding entry to `docs/frontend/open-decisions.md`.
