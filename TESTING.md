# EnthusiaDonorNPCs testing guide

This repository keeps its handwritten tests with the plugin source. Sentinel Sim may later run the built plugin for compatibility/lifecycle evidence, but normal unit/regression tests belong here and must pass independently.

## What the current test-hardening PR adds

The owner-directed test branch establishes the repository's first normal JUnit 5 test harness and adds:

- `src/test/java/com/enthusiasmpvp/donornpcs/UuidUtilTest.java`
- `src/test/java/com/enthusiasmpvp/donornpcs/PlaceholderNameUtilTest.java`
- `src/test/java/com/enthusiasmpvp/donornpcs/FacingDirectionTest.java`

`pom.xml` is updated only as needed to make those tests part of the normal Maven lifecycle.

No runtime/plugin behavior is intentionally changed by this test PR.

## What is covered

### UUID behavior

The UUID suite covers:

- standard dashed UUIDs;
- compact/undashed UUIDs;
- surrounding whitespace;
- malformed values;
- compact UUID round-trip behavior.

This protects player/NPC identity parsing from silent formatting regressions.

### Placeholder-derived names

The placeholder utility suite covers:

- `null` results;
- blank results;
- placeholder echo/unresolved values;
- sentinel/invalid values;
- trimming/cleaning;
- fallback display names;
- UUID-derived cleanup behavior.

This is important because PlaceholderAPI/provider output is external input and should not leak unresolved placeholder text or unsafe/meaningless values into NPC identity/display behavior.

### Facing direction

The facing suite covers:

- every supported configuration direction;
- invalid/default fallback;
- yaw selection;
- target-offset geometry.

This protects deterministic NPC orientation math without requiring a live Paper server.

## How to run the tests

Run the complete unit suite:

```bash
mvn test
```

Run the full Maven verification lifecycle used by the artifact workflow:

```bash
mvn clean verify
```

Run one focused class while editing, for example:

```bash
mvn -Dtest=UuidUtilTest test
mvn -Dtest=PlaceholderNameUtilTest test
mvn -Dtest=FacingDirectionTest test
```

## Where results are written

Maven Surefire writes test evidence under:

- `target/surefire-reports/`

GitHub Actions is the durable exact-head source for reviewed CI/artifact evidence. The existing Sentinel artifact workflow runs `mvn clean verify`, so these repository-local tests execute before an exact-head artifact is accepted.

A passing run from an older commit is stale after the branch changes.

## How to interpret failures

### UUID test failure

Treat it as a change to identity parsing/format assumptions. Verify existing saved/configured UUID forms before changing expected behavior.

### Placeholder test failure

Determine whether provider output sanitization/fallback semantics changed intentionally. Do not weaken null/blank/unresolved-placeholder handling simply to satisfy a new provider response.

### Facing test failure

Verify the supported config contract and coordinate/yaw math. These tests are deterministic and should not depend on server timing.

### Maven/test setup failure

A dependency-resolution, compiler, or Surefire setup failure is a harness/build failure, not proof that plugin behavior failed. Fix the harness/build configuration and rerun the same exact head.

## What is not covered yet

These first tests intentionally focus on deterministic pure logic. They do not by themselves prove:

- actual Citizens/FancyNpcs/FancyHolograms integration;
- PlaceholderAPI registration/runtime lifecycle;
- Paper enable/disable/reload behavior;
- world/entity spawning behavior;
- plugin-provider absence/degraded behavior beyond pure helper logic;
- real server/client rendering.

Those areas should receive additional repository integration/MockBukkit tests when truthful, or Sentinel/real-Paper acceptance when the behavior requires actual plugin dependencies or server runtime.

## Adding tests for a new feature

When product behavior changes:

1. add focused repository-local behavioral tests first;
2. include invalid/provider-missing inputs where relevant;
3. keep tests deterministic and independent of production services;
4. run `mvn test` while iterating;
5. run `mvn clean verify` before handoff;
6. verify the final GitHub Actions run belongs to the exact reviewed head;
7. update this guide if test layout, commands, providers, or known limits change;
8. update/re-run the Sentinel profile separately if the built-plugin runtime surface changed.

Do not move these JUnit tests into Sentinel Sim. Sentinel should consume the already-tested artifact for higher-level compatibility/lifecycle evidence.

## Reviewer checklist

- [ ] Assertions prove behavior rather than merely code execution.
- [ ] Invalid/null/unresolved external inputs are covered.
- [ ] No production credentials/data/network dependency is introduced.
- [ ] Runtime code was not weakened just to make a test green.
- [ ] `mvn clean verify` passes on the exact final head.
- [ ] Artifact workflow evidence corresponds to that same head.
- [ ] Any untested provider/Paper behavior is stated honestly rather than implied by unit tests.
