#!/usr/bin/env bash
# PostToolUse hook: 편집한 파일의 editorconfig 규칙을 자동 보정한다.
#   - 파일 끝 개행 (newline-eof)
#   - 줄 끝 공백 제거 (no-trailing-spaces)
#
# Checkstyle에는 자동 수정 기능이 없으므로 여기서는 editorconfig 규칙만 보정한다.
# 스타일 위반(들여쓰기, 120자, import 순서)은 `./gradlew checkstyleMain`으로 확인한다.

set -euo pipefail

INPUT=$(cat 2>/dev/null || echo '{}')

FILE=$(echo "$INPUT" | python3 -c "
import sys, json
try:
    d = json.load(sys.stdin)
    tool_input = d.get('tool_input', d)
    print(tool_input.get('file_path', ''))
except Exception:
    print('')
" 2>/dev/null || echo '')

[[ -n "$FILE" && -f "$FILE" && -s "$FILE" ]] || exit 0

# 텍스트 소스 파일만 대상으로 한다 (바이너리에 개행을 붙이면 파일이 깨진다)
case "$FILE" in
	*.java | *.gradle | *.kts | *.xml | *.yml | *.yaml | *.json | *.md | *.sh | *.properties | *.sql | *.txt | *.http) ;;
	*) exit 0 ;;
esac

# 줄 끝 공백 제거 (탭 들여쓰기는 보존)
if grep -qE '[ \t]+$' "$FILE" 2>/dev/null; then
	perl -i -pe 's/[ \t]+$//' "$FILE"
fi

# 파일 끝 개행 보정
if [[ "$(tail -c 1 "$FILE" | od -An -tx1 | tr -d ' ')" != "0a" ]]; then
	printf '\n' >>"$FILE"
fi

exit 0
