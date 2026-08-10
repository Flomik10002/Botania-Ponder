#!/usr/bin/env bash
#
# publish-curseforge.sh — publish a release's git tag(s) to CurseForge.
#
# The jar is never built or searched for locally — it's downloaded from the
# GitHub Release that matches each tag (attach the build artifact to the
# release first, e.g. via `gh release upload <tag> <jar>`). The GitHub
# release is the single source of truth for both the artifact and its tag.
#
# All per-tag metadata (Minecraft version, mod loader(s), mod name, changelog)
# is read from that tag's own committed files via `git show <tag>:<path>` —
# never from whatever happens to be checked out in the working tree. This
# means the script gives correct results regardless of which branch you
# currently have checked out, as long as the tag exists locally (it runs
# `git fetch --tags` up front to make sure of that).
#
# By default (no --tag/--release) it publishes every tag belonging to the
# newest release across all loaders/branches: e.g. if "0.7.2" was tagged as
# both "v0.7.2" (NeoForge branch) and "v0.7.2-forge-1.20.1" (Forge branch),
# one run uploads both files to CurseForge, each with its own correct game
# version and mod loader. Pass --tag to publish exactly one tag instead, or
# --release <version> to target an older release group.
#
# Portable: drop this file plus a "curseforge.publish.conf" into the root of
# any Gradle mod project and it will work, as long as the config is filled in
# and version tags follow "<prefix><version>" / "<prefix><version>-<suffix>".
#
# Idempotent: it remembers what it already uploaded (per project id) in a
# local state file and refuses to re-upload the same tag unless --force is
# passed, so re-running it after a mistake never spams the CurseForge page
# with duplicate files.
#
# Requires: bash 4+, curl, jq, git, gh (authenticated: `gh auth status`).
# Auth: set CURSEFORGE_API_TOKEN in your environment, or put it in a
#       ".env.curseforge" file (gitignored) next to the config — never commit
#       the token itself.

set -euo pipefail

# ---------------------------------------------------------------------------
# Defaults (overridden by the config file, then by CLI flags)
# ---------------------------------------------------------------------------

CF_PROJECT_ID=""
CF_PROJECT_SLUG=""
CF_GITHUB_REPO=""   # empty = auto-detect "owner/repo" from the 'origin' git remote
CF_MC_VERSIONS=""   # empty = auto-detect per tag from that tag's gradle.properties
CF_MOD_LOADERS=""   # empty = auto-detect per tag from that tag's build.gradle file(s)
CF_ENVIRONMENTS=""  # CurseForge "Environment" tag(s), e.g. "Client", "Server", "Client Server" — required
CF_EXTRA_VERSIONS=""   # anything else to add to gameVersionNames, e.g. "Java 17"
CF_RELEASE_TYPE="release"
CF_TAG_PREFIX="v"
CF_JAR_EXCLUDE_PATTERN="-(sources|dev|shadow|api)\.jar$"   # release assets to ignore when picking the jar
CF_CHANGELOG_FILE="CHANGELOG.md"
CF_CHANGELOG_TYPE="markdown"
CF_DISPLAY_NAME_TEMPLATE="{mod_name} {version}"
CF_MOD_NAME=""
CF_STATE_FILE=".curseforge-published.json"

CLEANUP_PATHS=()
cleanup() { local p; for p in "${CLEANUP_PATHS[@]:-}"; do [[ -n "$p" ]] && rm -rf "$p"; done; }
trap cleanup EXIT

DRY_RUN=0
ASSUME_YES=0
FORCE=0
TARGET_TAG=""
RELEASE_VERSION=""
CONFIG_PATH=""
CLI_RELEASE_TYPE=""
LIST_TAGS_ONLY=0

# ---------------------------------------------------------------------------
# Output helpers
# ---------------------------------------------------------------------------

if [[ -t 1 ]]; then
  C_RESET=$'\033[0m'; C_RED=$'\033[31m'; C_GREEN=$'\033[32m'
  C_YELLOW=$'\033[33m'; C_BLUE=$'\033[34m'; C_BOLD=$'\033[1m'
else
  C_RESET=""; C_RED=""; C_GREEN=""; C_YELLOW=""; C_BLUE=""; C_BOLD=""
fi

log()   { printf '%s\n' "$*" >&2; }
info()  { log "${C_BLUE}==>${C_RESET} $*"; }
ok()    { log "${C_GREEN}==>${C_RESET} $*"; }
warn()  { log "${C_YELLOW}warning:${C_RESET} $*"; }
die()   { log "${C_RED}error:${C_RESET} $*"; exit 1; }

# ---------------------------------------------------------------------------
# Usage
# ---------------------------------------------------------------------------

usage() {
  cat <<'EOF'
Usage: publish-curseforge.sh [options]

Publishes a release's build artifact(s) to CurseForge, using
curseforge.publish.conf for project-specific settings.

Options:
  --tag <tag>            Publish only this single tag.
  --release <version>    Publish every tag for this release across all
                          loaders/branches (e.g. "0.7.2" matches "v0.7.2",
                          "v0.7.2-forge-1.20.1", ...). Default: auto-detect
                          the newest release group from git tags.
  --release-type <type>  Override release type (release|beta|alpha).
  --config <path>        Use a config file other than curseforge.publish.conf.
  --force                Re-upload even if a tag was already published.
  --yes                  Skip the confirmation prompt.
  --dry-run              Show what would be uploaded without calling the API.
  --list-tags            List git tags, newest first, and exit.
  -h, --help             Show this help.

Environment:
  CURSEFORGE_API_TOKEN   Required. Your CurseForge upload API token.
                          (https://legacy.curseforge.com/account/api-tokens)

First-time setup: copy curseforge.publish.conf.example to
curseforge.publish.conf next to this script (or in the repo root) and fill
in CF_PROJECT_ID at minimum.
EOF
}

# ---------------------------------------------------------------------------
# Argument parsing
# ---------------------------------------------------------------------------

while [[ $# -gt 0 ]]; do
  case "$1" in
    --tag) TARGET_TAG="${2:?--tag requires a value}"; shift 2 ;;
    --release) RELEASE_VERSION="${2:?--release requires a value}"; shift 2 ;;
    --release-type) CLI_RELEASE_TYPE="${2:?--release-type requires a value}"; shift 2 ;;
    --config) CONFIG_PATH="${2:?--config requires a value}"; shift 2 ;;
    --force) FORCE=1; shift ;;
    --yes) ASSUME_YES=1; shift ;;
    --dry-run) DRY_RUN=1; shift ;;
    --list-tags) LIST_TAGS_ONLY=1; shift ;;
    -h|--help) usage; exit 0 ;;
    *) die "Unknown option: $1 (see --help)" ;;
  esac
done

[[ -z "$TARGET_TAG" || -z "$RELEASE_VERSION" ]] || die "--tag and --release are mutually exclusive."

# ---------------------------------------------------------------------------
# Dependencies
# ---------------------------------------------------------------------------

for bin in git curl jq gh; do
  command -v "$bin" >/dev/null 2>&1 || die "'$bin' is required but not installed."
done

# ---------------------------------------------------------------------------
# Locate repo root, config, and optional local env file
# ---------------------------------------------------------------------------

REPO_ROOT="$(git rev-parse --show-toplevel 2>/dev/null)" || die "Not inside a git repository."
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$REPO_ROOT"

if [[ -n "$CONFIG_PATH" ]]; then
  [[ -f "$CONFIG_PATH" ]] || die "Config file not found: $CONFIG_PATH"
else
  if [[ -f "$REPO_ROOT/curseforge.publish.conf" ]]; then
    CONFIG_PATH="$REPO_ROOT/curseforge.publish.conf"
  elif [[ -f "$SCRIPT_DIR/curseforge.publish.conf" ]]; then
    CONFIG_PATH="$SCRIPT_DIR/curseforge.publish.conf"
  else
    die "No config found. Copy curseforge.publish.conf.example to curseforge.publish.conf and fill it in."
  fi
fi

info "Using config: $CONFIG_PATH"
# shellcheck source=/dev/null
source "$CONFIG_PATH"

for envfile in "$REPO_ROOT/.env.curseforge" "$SCRIPT_DIR/.env.curseforge"; do
  if [[ -z "${CURSEFORGE_API_TOKEN:-}" && -f "$envfile" ]]; then
    # shellcheck source=/dev/null
    source "$envfile"
    break
  fi
done

[[ -n "${CLI_RELEASE_TYPE}" ]] && CF_RELEASE_TYPE="$CLI_RELEASE_TYPE"

# ---------------------------------------------------------------------------
# --list-tags shortcut (no project id / token required)
# ---------------------------------------------------------------------------

if [[ "$LIST_TAGS_ONLY" == "1" ]]; then
  git tag --sort=-creatordate
  exit 0
fi

[[ -n "$CF_PROJECT_ID" ]] || die "CF_PROJECT_ID is not set in $CONFIG_PATH."
[[ -n "$CF_ENVIRONMENTS" ]] || die "CF_ENVIRONMENTS is not set in $CONFIG_PATH (CurseForge requires an Environment tag, e.g. \"Client\", \"Server\", or \"Client Server\")."
[[ -n "${CURSEFORGE_API_TOKEN:-}" ]] || die "CURSEFORGE_API_TOKEN is not set (env var or .env.curseforge)."
case "$CF_RELEASE_TYPE" in
  release|beta|alpha) ;;
  *) die "CF_RELEASE_TYPE must be one of release|beta|alpha, got: $CF_RELEASE_TYPE" ;;
esac

STATE_FILE_PATH="$REPO_ROOT/$CF_STATE_FILE"
[[ -f "$STATE_FILE_PATH" ]] || echo '{}' > "$STATE_FILE_PATH"

if [[ -z "$CF_GITHUB_REPO" ]]; then
  ORIGIN_URL="$(git -C "$REPO_ROOT" remote get-url origin 2>/dev/null || true)"
  CF_GITHUB_REPO="$(printf '%s' "$ORIGIN_URL" | sed -E 's#^(git@github\.com:|https://github\.com/)##; s#\.git$##')"
  [[ -n "$CF_GITHUB_REPO" ]] || die "Could not auto-detect the GitHub repo from the 'origin' remote. Set CF_GITHUB_REPO in $CONFIG_PATH."
  info "Auto-detected GitHub repo: $CF_GITHUB_REPO (from git remote)"
fi

# ---------------------------------------------------------------------------
# Make sure local tags are in sync with the remote before resolving anything
# ---------------------------------------------------------------------------

if ! git fetch --tags --quiet origin 2>/dev/null; then
  warn "Could not run 'git fetch --tags origin' (offline?). Using local tags as-is — they may be stale."
fi

# ---------------------------------------------------------------------------
# Per-tag metadata readers — everything comes from the tag's own tree, never
# from the currently checked-out working copy, so results don't depend on
# which branch you happen to have checked out.
# ---------------------------------------------------------------------------

tag_show() { git show "${1}:${2}" 2>/dev/null || true; }

base_version_of_tag() {
  local v="$1"
  if [[ -n "$CF_TAG_PREFIX" && "$v" == "$CF_TAG_PREFIX"* ]]; then
    v="${v#"$CF_TAG_PREFIX"}"
  fi
  printf '%s\n' "${v%%-*}"
}

detect_mc_version_for_tag() {
  local tag="$1" content
  content="$(tag_show "$tag" "gradle.properties")"
  [[ -n "$content" ]] || return 1
  local v
  v="$(printf '%s\n' "$content" | sed -n 's/^minecraft_version=//p' | head -n1)"
  [[ -n "$v" ]] || return 1
  printf '%s\n' "$v"
}

detect_loaders_for_tag() {
  local tag="$1"
  local -a paths=()
  while IFS= read -r p; do
    [[ -n "$p" ]] && paths+=("$p")
  done < <(git ls-tree -r --name-only "$tag" -- . 2>/dev/null \
    | grep -E '(^|/)build\.gradle$' \
    | grep -Ev '(^|/)(build|\.gradle|references|node_modules)/' \
    | awk -F/ 'NF<=3')

  local -a detected=()
  local p content
  for p in "${paths[@]}"; do
    content="$(tag_show "$tag" "$p")"
    [[ -z "$content" ]] && continue
    grep -q 'net\.minecraftforge\.gradle' <<<"$content" && detected+=("Forge")
    grep -q 'net\.neoforged' <<<"$content" && detected+=("NeoForge")
    grep -q 'fabric-loom' <<<"$content" && detected+=("Fabric")
    grep -q 'org\.quiltmc\.loom' <<<"$content" && detected+=("Quilt")
  done
  [[ ${#detected[@]} -gt 0 ]] || return 1
  printf '%s\n' "${detected[@]}" | awk '!seen[$0]++' | tr '\n' ' ' | sed 's/ $//'
}

detect_mod_name_for_tag() {
  local tag="$1"
  tag_show "$tag" "gradle.properties" | sed -n 's/^mod_name=//p' | head -n1
}

build_changelog_for_tag() {
  local tag="$1" version="$2" content changelog ver_escaped
  content="$(tag_show "$tag" "$CF_CHANGELOG_FILE")"
  changelog=""
  if [[ -n "$content" ]]; then
    ver_escaped="$(printf '%s' "$version" | sed 's/[.[\*^$/]/\\&/g')"
    changelog="$(printf '%s\n' "$content" | awk -v ver="$ver_escaped" '
      /^## / {
        if (found) exit
        if ($0 ~ "^## \\[?" ver "\\]?([ (—-]|$)") { found=1; next }
        next
      }
      found { print }
    ')"
    changelog="$(printf '%s\n' "$changelog" | sed -e '/./,$!d' -e ':a' -e '/^\n*$/{$d;N;ba' -e '}')"
  fi
  if [[ -z "$changelog" ]]; then
    warn "No changelog section found for '$version' in $CF_CHANGELOG_FILE (tag $tag). Falling back to the tag/commit message."
    changelog="$(git tag -l --format='%(contents)' "$tag")"
    [[ -n "$changelog" ]] || changelog="$(git log -1 --format='%B' "$(git rev-list -n1 "$tag")")"
  fi
  printf '%s' "$changelog"
}

# ---------------------------------------------------------------------------
# Resolve the set of tags to publish
# ---------------------------------------------------------------------------

declare -a TARGET_TAGS=()

if [[ -n "$TARGET_TAG" ]]; then
  git rev-parse -q --verify "refs/tags/$TARGET_TAG" >/dev/null || die "Tag not found: $TARGET_TAG"
  TARGET_TAGS=("$TARGET_TAG")
  info "Publishing single tag: $TARGET_TAG"
else
  if [[ -z "$RELEASE_VERSION" ]]; then
    ALL_TAGS="$(git tag)"
    [[ -n "$ALL_TAGS" ]] || die "No git tags found. Create one first, or pass --tag."
    RELEASE_VERSION="$(
      while IFS= read -r t; do base_version_of_tag "$t"; done <<<"$ALL_TAGS" \
        | sort -Vu | tail -n1
    )"
    [[ -n "$RELEASE_VERSION" ]] || die "Could not determine a release version from git tags."
    info "Auto-detected newest release: $RELEASE_VERSION"
  fi
  while IFS= read -r t; do
    [[ -n "$t" ]] && TARGET_TAGS+=("$t")
  done < <(git tag -l "${CF_TAG_PREFIX}${RELEASE_VERSION}" "${CF_TAG_PREFIX}${RELEASE_VERSION}-*" | sort)
  [[ ${#TARGET_TAGS[@]} -gt 0 ]] || die "No tags found for release '$RELEASE_VERSION' (looked for '${CF_TAG_PREFIX}${RELEASE_VERSION}' and '${CF_TAG_PREFIX}${RELEASE_VERSION}-*')."
  info "Release '$RELEASE_VERSION' -> ${#TARGET_TAGS[@]} tag(s): ${TARGET_TAGS[*]}"
fi

# ---------------------------------------------------------------------------
# Build the upload plan: one entry per tag that isn't already published
# ---------------------------------------------------------------------------

DOWNLOAD_DIR="$(mktemp -d)"
CLEANUP_PATHS+=("$DOWNLOAD_DIR")

declare -a PLAN_TAG=() PLAN_VERSION=() PLAN_JAR_NAME=() PLAN_JAR_PATH=()
declare -a PLAN_DISPLAY_NAME=() PLAN_CHANGELOG=() PLAN_MC_VERSIONS=() PLAN_LOADERS=()

for tag in "${TARGET_TAGS[@]}"; do
  version="$tag"
  if [[ -n "$CF_TAG_PREFIX" && "$version" == "$CF_TAG_PREFIX"* ]]; then
    version="${version#"$CF_TAG_PREFIX"}"
  fi

  previous_file_id="$(jq -r --arg pid "$CF_PROJECT_ID" --arg tag "$tag" \
    '.[$pid][$tag].fileId // empty' "$STATE_FILE_PATH")"
  if [[ -n "$previous_file_id" && "$FORCE" -ne 1 ]]; then
    ok "Tag '$tag' was already published as CurseForge file #$previous_file_id. Skipping (use --force to re-upload)."
    continue
  fi

  mc_versions="$CF_MC_VERSIONS"
  if [[ -z "$mc_versions" ]]; then
    mc_versions="$(detect_mc_version_for_tag "$tag")" \
      || die "Could not auto-detect the Minecraft version for tag '$tag' (no minecraft_version= in its gradle.properties). Set CF_MC_VERSIONS in $CONFIG_PATH, or fix the tag."
  fi

  loaders="$CF_MOD_LOADERS"
  if [[ -z "$loaders" ]]; then
    loaders="$(detect_loaders_for_tag "$tag")" \
      || die "Could not auto-detect the mod loader for tag '$tag' from its build.gradle. Set CF_MOD_LOADERS in $CONFIG_PATH, or fix the tag."
  fi

  info "Looking up GitHub release '$tag' in $CF_GITHUB_REPO..."
  asset_names="$(gh release view "$tag" --repo "$CF_GITHUB_REPO" --json assets --jq '.assets[].name' 2>&1)" \
    || die "No GitHub release found for tag '$tag' in $CF_GITHUB_REPO. Create it first (gh release create $tag)."

  declare -a candidates=()
  while IFS= read -r name; do
    [[ -z "$name" ]] && continue
    [[ "$name" == *.jar ]] || continue
    if [[ -n "$CF_JAR_EXCLUDE_PATTERN" && "$name" =~ $CF_JAR_EXCLUDE_PATTERN ]]; then
      continue
    fi
    candidates+=("$name")
  done <<<"$asset_names"

  if [[ ${#candidates[@]} -eq 0 ]]; then
    die "Release '$tag' has no .jar asset attached. Attach the build first: gh release upload $tag <path-to-jar> --repo $CF_GITHUB_REPO"
  elif [[ ${#candidates[@]} -gt 1 ]]; then
    warn "Multiple .jar assets on release '$tag':"
    printf '  %s\n' "${candidates[@]}" >&2
    die "Narrow CF_JAR_EXCLUDE_PATTERN so exactly one asset matches."
  fi

  jar_name="${candidates[0]}"
  gh release download "$tag" --repo "$CF_GITHUB_REPO" --pattern "$jar_name" --dir "$DOWNLOAD_DIR" --clobber \
    || die "Failed to download '$jar_name' from the GitHub release '$tag'."
  jar_path="$DOWNLOAD_DIR/$jar_name"
  info "Artifact: $jar_name ($(du -h "$jar_path" | cut -f1)) — from GitHub release $tag"

  changelog="$(build_changelog_for_tag "$tag" "$version")"

  mod_name="$CF_MOD_NAME"
  [[ -n "$mod_name" ]] || mod_name="$(detect_mod_name_for_tag "$tag")"
  [[ -n "$mod_name" ]] || mod_name="$(basename "$REPO_ROOT")"

  display_name="${CF_DISPLAY_NAME_TEMPLATE//\{mod_name\}/$mod_name}"
  display_name="${display_name//\{version\}/$version}"
  display_name="${display_name//\{tag\}/$tag}"

  PLAN_TAG+=("$tag")
  PLAN_VERSION+=("$version")
  PLAN_JAR_NAME+=("$jar_name")
  PLAN_JAR_PATH+=("$jar_path")
  PLAN_DISPLAY_NAME+=("$display_name")
  PLAN_CHANGELOG+=("$changelog")
  PLAN_MC_VERSIONS+=("$mc_versions")
  PLAN_LOADERS+=("$loaders")
done

if [[ ${#PLAN_TAG[@]} -eq 0 ]]; then
  ok "Nothing to publish — every matched tag was already uploaded."
  exit 0
fi

# ---------------------------------------------------------------------------
# Summary + confirmation
# ---------------------------------------------------------------------------

info "${C_BOLD}About to publish ${#PLAN_TAG[@]} file(s):${C_RESET}"
for i in "${!PLAN_TAG[@]}"; do
  log ""
  log "  ${C_BOLD}Tag${C_RESET}             : ${PLAN_TAG[$i]}"
  log "  GitHub repo     : $CF_GITHUB_REPO"
  log "  Project ID      : $CF_PROJECT_ID"
  log "  Display name    : ${PLAN_DISPLAY_NAME[$i]}"
  log "  File            : ${PLAN_JAR_NAME[$i]}"
  log "  Release type    : $CF_RELEASE_TYPE"
  log "  Game versions   : ${PLAN_MC_VERSIONS[$i]}"
  log "  Mod loaders     : ${PLAN_LOADERS[$i]}"
  log "  Environment     : $CF_ENVIRONMENTS"
  [[ -n "$CF_EXTRA_VERSIONS" ]] && log "  Extra versions  : $CF_EXTRA_VERSIONS"
  log "  Changelog lines : $(printf '%s\n' "${PLAN_CHANGELOG[$i]}" | wc -l | tr -d ' ')"
done
log ""

if [[ "$DRY_RUN" -eq 1 ]]; then
  for i in "${!PLAN_TAG[@]}"; do
    log "${C_BOLD}--- changelog preview: ${PLAN_TAG[$i]} ---${C_RESET}"
    log "${PLAN_CHANGELOG[$i]}"
    log ""
  done
  ok "Dry run: no API calls made."
  exit 0
fi

if [[ "$ASSUME_YES" -ne 1 ]]; then
  read -r -p "Proceed with uploading ${#PLAN_TAG[@]} file(s)? [y/N] " REPLY
  [[ "$REPLY" =~ ^[Yy]$ ]] || die "Aborted."
fi

# ---------------------------------------------------------------------------
# Upload
# ---------------------------------------------------------------------------
# gameVersionNames takes plain names directly (e.g. "1.20.1", "Forge"), no
# need to resolve them to numeric ids via a separate API call first.

for i in "${!PLAN_TAG[@]}"; do
  tag="${PLAN_TAG[$i]}"
  jar_path="${PLAN_JAR_PATH[$i]}"
  jar_name="${PLAN_JAR_NAME[$i]}"

  game_version_names_json="$(printf '%s\n' ${PLAN_MC_VERSIONS[$i]} ${PLAN_LOADERS[$i]} $CF_ENVIRONMENTS $CF_EXTRA_VERSIONS | jq -R . | jq -s .)"

  metadata_json="$(jq -n \
    --arg changelog "${PLAN_CHANGELOG[$i]}" \
    --arg changelogType "$CF_CHANGELOG_TYPE" \
    --arg displayName "${PLAN_DISPLAY_NAME[$i]}" \
    --arg releaseType "$CF_RELEASE_TYPE" \
    --argjson gameVersionNames "$game_version_names_json" \
    '{changelog: $changelog, changelogType: $changelogType, displayName: $displayName, releaseType: $releaseType, gameVersionNames: $gameVersionNames}')"

  info "Uploading $jar_name (tag $tag) to CurseForge project $CF_PROJECT_ID..."

  http_response_file="$(mktemp)"
  CLEANUP_PATHS+=("$http_response_file")

  http_status="$(curl -s -o "$http_response_file" -w '%{http_code}' \
    -H "X-Api-Token: $CURSEFORGE_API_TOKEN" \
    -F "metadata=$metadata_json" \
    -F "file=@${jar_path}" \
    "https://minecraft.curseforge.com/api/projects/${CF_PROJECT_ID}/upload-file")"

  response_body="$(cat "$http_response_file")"

  if [[ "$http_status" != "200" ]]; then
    die "CurseForge upload failed for tag '$tag' (HTTP $http_status): $response_body"
  fi

  file_id="$(jq -r '.id // empty' <<<"$response_body")"
  [[ -n "$file_id" ]] || die "Upload response for tag '$tag' did not contain a file id: $response_body"

  tmp_state="$(mktemp)"
  jq --arg pid "$CF_PROJECT_ID" --arg tag "$tag" --arg fid "$file_id" \
     --arg ts "$(date -u +%Y-%m-%dT%H:%M:%SZ)" --arg jar "$jar_name" \
     '.[$pid][$tag] = {fileId: ($fid | tonumber), publishedAt: $ts, jar: $jar}' \
     "$STATE_FILE_PATH" > "$tmp_state"
  mv "$tmp_state" "$STATE_FILE_PATH"

  ok "Published $tag as CurseForge file #$file_id."
  if [[ -n "$CF_PROJECT_SLUG" ]]; then
    log "  https://www.curseforge.com/minecraft/mc-mods/${CF_PROJECT_SLUG}/files/${file_id}"
  fi
done
