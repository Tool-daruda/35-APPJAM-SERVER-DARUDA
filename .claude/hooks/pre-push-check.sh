#!/usr/bin/env bash
# PreToolUse(Bash) hook: `git push` 직전 코드 스타일을 강제한다.
# 실패 시 exit 2로 push를 차단하고 사유를 stderr로 알린다.
#
# 게이트 범위: checkstyleMain/checkstyleTest + editorconfigCheck.
#   전체 test는 느려서 게이트에서 제외 — 필요하면 PUSH_GATE_RUN_TESTS=1로 켠다.
#   (PR을 올리면 test.yml 워크플로우가 어차피 전체 테스트를 돌린다.)
#
# 검증 환경이 깨졌을 때(프로젝트 디렉터리 이동 실패, Gradle 프로젝트인데 gradlew 없음)는
# 통과시키지 않고 fail-closed로 차단한다. 조용히 exit 0 하면 게이트가 무력화된다.

set -uo pipefail

INPUT=$(cat 2>/dev/null || echo '{}')

# `git push` 판별: 단순 정규식은 `git -C <dir> push`, `git --git-dir=... push`,
# `FOO=1 git push`, `a && git push` 같은 변형을 놓친다. 토큰 단위로 하위 명령을 뽑아낸다.
SUBCOMMANDS=$(echo "$INPUT" | python3 -c '
import sys, json, shlex

SEPARATORS = {"&&", "||", "|", ";", "&", "(", ")", "{", "}"}
# git 하위 명령 앞에 오면서 인자를 하나 더 먹는 전역 옵션
VALUE_OPTS = {"-C", "-c", "--git-dir", "--work-tree", "--namespace",
              "--exec-path", "--super-prefix", "--config-env"}

try:
    command = json.load(sys.stdin).get("tool_input", {}).get("command", "")
except Exception:
    command = ""

if not command:
    sys.exit(0)

try:
    tokens = shlex.split(command, comments=True)
except ValueError:
    # 따옴표 불일치 등 파싱 실패 → 판별 불가이므로 게이트를 태운다(fail-closed)
    print("push")
    sys.exit(0)

segments, current = [], []
for token in tokens:
    if token in SEPARATORS:
        current = []
        segments.append(current)
    else:
        current.append(token)
    if not segments:
        segments.append(current)

for segment in segments:
    i = 0
    # `FOO=bar git push` 같은 환경변수 접두사 건너뛰기
    while i < len(segment) and "=" in segment[i] and not segment[i].startswith("-"):
        i += 1
    if i >= len(segment):
        continue
    argv0 = segment[i]
    if argv0 != "git" and not argv0.endswith("/git"):
        continue
    i += 1
    while i < len(segment):
        token = segment[i]
        if token in VALUE_OPTS:
            i += 2
            continue
        if token.startswith("-"):
            i += 1
            continue
        print(token)  # 첫 비옵션 토큰 = 하위 명령(또는 alias)
        break
' 2>/dev/null)

[[ -n "$SUBCOMMANDS" ]] || exit 0

IS_PUSH=0
while IFS= read -r sub; do
	[[ -n "$sub" ]] || continue
	if [[ "$sub" == "push" ]]; then
		IS_PUSH=1
		break
	fi
	# alias가 push로 확장되는 경우도 게이트 대상이다 (예: alias.p = push)
	expansion=$(git config --get "alias.$sub" 2>/dev/null || true)
	if [[ -n "$expansion" && "$expansion" =~ (^|[[:space:]!])push([[:space:]]|$) ]]; then
		IS_PUSH=1
		break
	fi
done <<<"$SUBCOMMANDS"

[[ "$IS_PUSH" == "1" ]] || exit 0

PROJECT_DIR="${CLAUDE_PROJECT_DIR:-.}"
if ! cd "$PROJECT_DIR" 2>/dev/null; then
	echo "❌ 프로젝트 디렉터리로 이동할 수 없습니다: $PROJECT_DIR" >&2
	echo "   검증을 수행할 수 없어 push를 차단합니다. CLAUDE_PROJECT_DIR을 확인하세요." >&2
	exit 2
fi

if [[ ! -f "./gradlew" ]]; then
	# Gradle 프로젝트인데 래퍼가 없으면 검증 불가 → 차단
	if compgen -G "./build.gradle*" >/dev/null || compgen -G "./settings.gradle*" >/dev/null; then
		echo "❌ Gradle 프로젝트인데 ./gradlew 가 없습니다 ($PROJECT_DIR)." >&2
		echo "   검증을 수행할 수 없어 push를 차단합니다." >&2
		exit 2
	fi
	exit 0 # Gradle 프로젝트가 아니면 이 게이트의 대상이 아니다
fi

if ! ./gradlew checkstyleMain checkstyleTest --quiet 2>&1; then
	echo "❌ Checkstyle 실패. build/reports/checkstyle/main.html 을 확인해 직접 수정하세요." >&2
	echo "   (Checkstyle에는 자동 수정 기능이 없습니다. 들여쓰기는 탭, 한 줄 120자, import 순서를 확인하세요.)" >&2
	exit 2
fi

if ! ./gradlew editorconfigCheck --quiet 2>&1; then
	echo "❌ editorconfig 위반. './gradlew editorconfigFormat' 으로 자동 수정 후 다시 커밋하고 push하세요." >&2
	exit 2
fi

if [[ "${PUSH_GATE_RUN_TESTS:-0}" == "1" ]]; then
	if ! ./gradlew test --quiet 2>&1; then
		echo "❌ 테스트 실패. 테스트를 통과시킨 후 다시 push하세요." >&2
		exit 2
	fi
fi

exit 0
