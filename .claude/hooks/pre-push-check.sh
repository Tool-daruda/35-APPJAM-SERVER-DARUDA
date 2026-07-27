#!/usr/bin/env bash
# PreToolUse(Bash) hook: `git push` 직전 코드 스타일을 강제한다.
# 실패 시 exit 2로 push를 차단하고 사유를 stderr로 알린다.
#
# 게이트 범위: checkstyleMain/checkstyleTest + editorconfigCheck.
#   전체 test는 느려서 게이트에서 제외 — 필요하면 PUSH_GATE_RUN_TESTS=1로 켠다.
#   (PR을 올리면 test.yml 워크플로우가 어차피 전체 테스트를 돌린다.)

INPUT=$(cat 2>/dev/null || echo '{}')
COMMAND=$(echo "$INPUT" | python3 -c "
import sys, json
try:
    print(json.load(sys.stdin).get('tool_input', {}).get('command', ''))
except Exception:
    print('')
" 2>/dev/null || echo '')

# git push 가 아니면 통과
[[ "$COMMAND" =~ (^|[[:space:]&|;])git[[:space:]]+push ]] || exit 0

cd "${CLAUDE_PROJECT_DIR:-.}" || exit 0
[[ -f "./gradlew" ]] || exit 0

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
