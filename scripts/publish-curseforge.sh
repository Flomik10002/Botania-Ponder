#!/usr/bin/env bash
#
# publish-curseforge.sh — publish the latest (or a chosen) git tag to CurseForge.
#
# The jar is never built or searched for locally — it's downloaded from the
# GitHub Release that matches the tag (attach the build artifact to the
# release first, e.g. via `gh release upload <tag> <jar>`). The GitHub
# release is the single source of truth for both the artifact and its tag.
#
# Portable: drop this file plus a "curseforge.publish.conf" into the root of
# any Gradle mod project and it will work, as long as the config is filled in.
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
CF_MC_VERSIONS=""   # empty = auto-detect from gradle.properties (minecraft_version)
CF_MOD_LOADERS=""   # empty = auto-detect from build.gradle plugin ids
CF_JAVA_VERSIONS=""
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
CONFIG_PATH=""
CLI_RELEASE_TYPE=""

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

Publishes a git tag's build artifact to CurseForge, using
curseforge.publish.conf for project-specific settings.

Options:
  --tag <tag>            Publish this tag instead of the latest one.
  --release-type <type>  Override release type (release|beta|alpha).
  --config <path>        Use a config file other than curseforge.publish.conf.
  --force                Re-upload even if this tag was already published.
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
  if [[ -f "$envfile" ]]; then
    # shellcheck source=/dev/null
    source "$envfile"
    break
  fi
done

[[ -n "${CLI_RELEASE_TYPE}" ]] && CF_RELEASE_TYPE="$CLI_RELEASE_TYPE"

# ---------------------------------------------------------------------------
# --list-tags shortcut (no project id / token required)
# ---------------------------------------------------------------------------

if [[ "${LIST_TAGS_ONLY:-0}" == "1" ]]; then
  git tag --sort=-creatordate
  exit 0
fi

[[ -n "$CF_PROJECT_ID" ]] || die "CF_PROJECT_ID is not set in $CONFIG_PATH."
[[ -n "${CURSEFORGE_API_TOKEN:-}" ]] || die "CURSEFORGE_API_TOKEN is not set (env var or .env.curseforge)."
case "$CF_RELEASE_TYPE" in
  release|beta|alpha) ;;
  *) die "CF_RELEASE_TYPE must be one of release|beta|alpha, got: $CF_RELEASE_TYPE" ;;
esac

STATE_FILE_PATH="$REPO_ROOT/$CF_STATE_FILE"

# ---------------------------------------------------------------------------
# Auto-detect Minecraft version / mod loader(s) when not pinned in the config
# ---------------------------------------------------------------------------

if [[ -z "$CF_MC_VERSIONS" ]]; then
  if [[ -f "$REPO_ROOT/gradle.properties" ]]; then
    CF_MC_VERSIONS="$(sed -n 's/^minecraft_version=//p' "$REPO_ROOT/gradle.properties" | head -n1)"
  fi
  [[ -n "$CF_MC_VERSIONS" ]] || die "Could not auto-detect the Minecraft version (no minecraft_version= in gradle.properties). Set CF_MC_VERSIONS in $CONFIG_PATH."
  info "Auto-detected Minecraft version: $CF_MC_VERSIONS (from gradle.properties)"
fi

if [[ -z "$CF_MOD_LOADERS" ]]; then
  DETECTED_LOADERS=()
  BUILD_GRADLE_FILES=()
  while IFS= read -r -d '' f; do BUILD_GRADLE_FILES+=("$f"); done < <(
    find "$REPO_ROOT" -maxdepth 3 -name 'build.gradle' \
      -not -path '*/build/*' -not -path '*/.gradle/*' \
      -not -path '*/references/*' -not -path '*/node_modules/*' -print0
  )
  for f in "${BUILD_GRADLE_FILES[@]}"; do
    grep -q "net\.minecraftforge\.gradle" "$f" && DETECTED_LOADERS+=("Forge")
    grep -q "net\.neoforged" "$f" && DETECTED_LOADERS+=("NeoForge")
    grep -q "fabric-loom" "$f" && DETECTED_LOADERS+=("Fabric")
    grep -q "org\.quiltmc\.loom" "$f" && DETECTED_LOADERS+=("Quilt")
  done
  # de-duplicate while preserving order
  if [[ ${#DETECTED_LOADERS[@]} -gt 0 ]]; then
    CF_MOD_LOADERS="$(printf '%s\n' "${DETECTED_LOADERS[@]}" | awk '!seen[$0]++' | tr '\n' ' ')"
    CF_MOD_LOADERS="${CF_MOD_LOADERS% }"
  fi
  [[ -n "$CF_MOD_LOADERS" ]] || die "Could not auto-detect the mod loader from build.gradle. Set CF_MOD_LOADERS in $CONFIG_PATH."
  info "Auto-detected mod loader(s): $CF_MOD_LOADERS (from build.gradle)"
fi

if [[ -z "$CF_GITHUB_REPO" ]]; then
  ORIGIN_URL="$(git -C "$REPO_ROOT" remote get-url origin 2>/dev/null || true)"
  CF_GITHUB_REPO="$(printf '%s' "$ORIGIN_URL" | sed -E 's#^(git@github\.com:|https://github\.com/)##; s#\.git$##')"
  [[ -n "$CF_GITHUB_REPO" ]] || die "Could not auto-detect the GitHub repo from the 'origin' remote. Set CF_GITHUB_REPO in $CONFIG_PATH."
  info "Auto-detected GitHub repo: $CF_GITHUB_REPO (from git remote)"
fi

# ---------------------------------------------------------------------------
# Resolve the target tag
# ---------------------------------------------------------------------------

if [[ -z "$TARGET_TAG" ]]; then
  TARGET_TAG="$(git tag --sort=-creatordate | head -n1)"
  [[ -n "$TARGET_TAG" ]] || die "No git tags found. Create one first, or pass --tag."
  info "Latest tag: $TARGET_TAG"
else
  git rev-parse -q --verify "refs/tags/$TARGET_TAG" >/dev/null || die "Tag not found: $TARGET_TAG"
fi

VERSION="$TARGET_TAG"
if [[ -n "$CF_TAG_PREFIX" && "$VERSION" == "$CF_TAG_PREFIX"* ]]; then
  VERSION="${VERSION#"$CF_TAG_PREFIX"}"
fi

TAG_COMMIT="$(git rev-list -n1 "$TARGET_TAG")"
HEAD_COMMIT="$(git rev-parse HEAD)"
if [[ "$TAG_COMMIT" != "$HEAD_COMMIT" ]]; then
  warn "HEAD is not at tag '$TARGET_TAG'. Make sure the build artifact you upload actually corresponds to that tag."
fi

# ---------------------------------------------------------------------------
# Idempotency check
# ---------------------------------------------------------------------------

[[ -f "$STATE_FILE_PATH" ]] || echo '{}' > "$STATE_FILE_PATH"

PREVIOUS_FILE_ID="$(jq -r --arg pid "$CF_PROJECT_ID" --arg tag "$TARGET_TAG" \
  '.[$pid][$tag].fileId // empty' "$STATE_FILE_PATH")"

if [[ -n "$PREVIOUS_FILE_ID" && "$FORCE" -ne 1 ]]; then
  ok "Tag '$TARGET_TAG' was already published as CurseForge file #$PREVIOUS_FILE_ID. Nothing to do (use --force to re-upload)."
  exit 0
fi

# ---------------------------------------------------------------------------
# Fetch the build artifact from the matching GitHub Release
# ---------------------------------------------------------------------------

info "Looking up GitHub release '$TARGET_TAG' in $CF_GITHUB_REPO..."
ASSET_NAMES="$(gh release view "$TARGET_TAG" --repo "$CF_GITHUB_REPO" --json assets --jq '.assets[].name' 2>&1)" \
  || die "No GitHub release found for tag '$TARGET_TAG' in $CF_GITHUB_REPO. Create it first (gh release create $TARGET_TAG)."

CANDIDATES=()
while IFS= read -r name; do
  [[ -z "$name" ]] && continue
  [[ "$name" == *.jar ]] || continue
  if [[ -n "$CF_JAR_EXCLUDE_PATTERN" && "$name" =~ $CF_JAR_EXCLUDE_PATTERN ]]; then
    continue
  fi
  CANDIDATES+=("$name")
done <<<"$ASSET_NAMES"

if [[ ${#CANDIDATES[@]} -eq 0 ]]; then
  die "Release '$TARGET_TAG' has no .jar asset attached. Attach the build first: gh release upload $TARGET_TAG <path-to-jar> --repo $CF_GITHUB_REPO"
elif [[ ${#CANDIDATES[@]} -gt 1 ]]; then
  warn "Multiple .jar assets on release '$TARGET_TAG':"
  printf '  %s\n' "${CANDIDATES[@]}" >&2
  die "Narrow CF_JAR_EXCLUDE_PATTERN so exactly one asset matches."
fi

JAR_NAME="${CANDIDATES[0]}"
DOWNLOAD_DIR="$(mktemp -d)"
CLEANUP_PATHS+=("$DOWNLOAD_DIR")

gh release download "$TARGET_TAG" --repo "$CF_GITHUB_REPO" --pattern "$JAR_NAME" --dir "$DOWNLOAD_DIR" --clobber \
  || die "Failed to download '$JAR_NAME' from the GitHub release."

JAR_PATH="$DOWNLOAD_DIR/$JAR_NAME"
info "Artifact: $JAR_NAME ($(du -h "$JAR_PATH" | cut -f1)) — from GitHub release $TARGET_TAG"

# ---------------------------------------------------------------------------
# Build the changelog for this version
# ---------------------------------------------------------------------------

CHANGELOG=""
if [[ -f "$REPO_ROOT/$CF_CHANGELOG_FILE" ]]; then
  VER_ESCAPED="$(printf '%s' "$VERSION" | sed 's/[.[\*^$/]/\\&/g')"
  CHANGELOG="$(awk -v ver="$VER_ESCAPED" '
    /^## / {
      if (found) exit
      if ($0 ~ "^## \\[?" ver "\\]?([ (—-]|$)") { found=1; next }
      next
    }
    found { print }
  ' "$REPO_ROOT/$CF_CHANGELOG_FILE")"
  # trim leading/trailing blank lines
  CHANGELOG="$(printf '%s\n' "$CHANGELOG" | sed -e '/./,$!d' -e ':a' -e '/^\n*$/{$d;N;ba' -e '}')"
fi

if [[ -z "$CHANGELOG" ]]; then
  warn "No changelog section found for '$VERSION' in $CF_CHANGELOG_FILE. Falling back to the tag/commit message."
  CHANGELOG="$(git tag -l --format='%(contents)' "$TARGET_TAG")"
  [[ -n "$CHANGELOG" ]] || CHANGELOG="$(git log -1 --format='%B' "$TAG_COMMIT")"
fi

# ---------------------------------------------------------------------------
# Display name
# ---------------------------------------------------------------------------

if [[ -z "$CF_MOD_NAME" && -f "$REPO_ROOT/gradle.properties" ]]; then
  CF_MOD_NAME="$(sed -n 's/^mod_name=//p' "$REPO_ROOT/gradle.properties" | head -n1)"
fi
[[ -n "$CF_MOD_NAME" ]] || CF_MOD_NAME="$(basename "$REPO_ROOT")"

DISPLAY_NAME="${CF_DISPLAY_NAME_TEMPLATE//\{mod_name\}/$CF_MOD_NAME}"
DISPLAY_NAME="${DISPLAY_NAME//\{version\}/$VERSION}"
DISPLAY_NAME="${DISPLAY_NAME//\{tag\}/$TARGET_TAG}"

# ---------------------------------------------------------------------------
# Summary + confirmation
# ---------------------------------------------------------------------------

info "${C_BOLD}About to publish:${C_RESET}"
log "  GitHub repo     : $CF_GITHUB_REPO"
log "  Project ID     : $CF_PROJECT_ID"
log "  Tag             : $TARGET_TAG"
log "  Display name    : $DISPLAY_NAME"
log "  File            : $JAR_NAME"
log "  Release type    : $CF_RELEASE_TYPE"
log "  Game versions   : $CF_MC_VERSIONS"
log "  Mod loaders     : $CF_MOD_LOADERS"
[[ -n "$CF_JAVA_VERSIONS" ]] && log "  Java versions   : $CF_JAVA_VERSIONS"
log "  Changelog lines : $(printf '%s\n' "$CHANGELOG" | wc -l | tr -d ' ')"

if [[ "$DRY_RUN" -eq 1 ]]; then
  log ""
  log "${C_BOLD}--- changelog preview ---${C_RESET}"
  log "$CHANGELOG"
  ok "Dry run: no API calls made."
  exit 0
fi

if [[ "$ASSUME_YES" -ne 1 ]]; then
  read -r -p "Proceed with upload? [y/N] " REPLY
  [[ "$REPLY" =~ ^[Yy]$ ]] || die "Aborted."
fi

# ---------------------------------------------------------------------------
# Upload
# ---------------------------------------------------------------------------
# gameVersionNames takes plain names directly (e.g. "1.20.1", "Forge"), no
# need to resolve them to numeric ids via a separate API call first.

GAME_VERSION_NAMES_JSON="$(printf '%s\n' $CF_MC_VERSIONS $CF_MOD_LOADERS $CF_JAVA_VERSIONS | jq -R . | jq -s .)"

METADATA_JSON="$(jq -n \
  --arg changelog "$CHANGELOG" \
  --arg changelogType "$CF_CHANGELOG_TYPE" \
  --arg displayName "$DISPLAY_NAME" \
  --arg releaseType "$CF_RELEASE_TYPE" \
  --argjson gameVersionNames "$GAME_VERSION_NAMES_JSON" \
  '{changelog: $changelog, changelogType: $changelogType, displayName: $displayName, releaseType: $releaseType, gameVersionNames: $gameVersionNames}')"

info "Uploading $JAR_NAME to CurseForge project $CF_PROJECT_ID..."

HTTP_RESPONSE_FILE="$(mktemp)"
CLEANUP_PATHS+=("$HTTP_RESPONSE_FILE")

HTTP_STATUS="$(curl -s -o "$HTTP_RESPONSE_FILE" -w '%{http_code}' \
  -H "X-Api-Token: $CURSEFORGE_API_TOKEN" \
  -F "metadata=$METADATA_JSON" \
  -F "file=@${JAR_PATH}" \
  "https://minecraft.curseforge.com/api/projects/${CF_PROJECT_ID}/upload-file")"

RESPONSE_BODY="$(cat "$HTTP_RESPONSE_FILE")"

if [[ "$HTTP_STATUS" != "200" ]]; then
  die "CurseForge upload failed (HTTP $HTTP_STATUS): $RESPONSE_BODY"
fi

FILE_ID="$(jq -r '.id // empty' <<<"$RESPONSE_BODY")"
[[ -n "$FILE_ID" ]] || die "Upload response did not contain a file id: $RESPONSE_BODY"

# ---------------------------------------------------------------------------
# Persist state
# ---------------------------------------------------------------------------

TMP_STATE="$(mktemp)"
jq --arg pid "$CF_PROJECT_ID" --arg tag "$TARGET_TAG" --arg fid "$FILE_ID" \
   --arg ts "$(date -u +%Y-%m-%dT%H:%M:%SZ)" --arg jar "$JAR_NAME" \
   '.[$pid][$tag] = {fileId: ($fid | tonumber), publishedAt: $ts, jar: $jar}' \
   "$STATE_FILE_PATH" > "$TMP_STATE"
mv "$TMP_STATE" "$STATE_FILE_PATH"

ok "Published $TARGET_TAG as CurseForge file #$FILE_ID."
if [[ -n "$CF_PROJECT_SLUG" ]]; then
  log "  https://www.curseforge.com/minecraft/mc-mods/${CF_PROJECT_SLUG}/files/${FILE_ID}"
fi
