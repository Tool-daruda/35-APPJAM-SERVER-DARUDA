#!/usr/bin/env bash
# Stop hook: 세션 종료 시 추적 파일의 EOF 개행을 자동 보정한다.
# editorconfig의 [newline-eof] 규칙 위반으로 빌드가 깨지는 것을 막는다.
# 실패해도 세션 종료를 막지 않는다.

set -euo pipefail

PROJECT_ROOT="${CLAUDE_PROJECT_DIR:-.}"
cd "$PROJECT_ROOT" || exit 0

# 확장자 목록은 신뢰할 수 없다(.zip·.pdf·확장자 없는 바이너리 등이 빠진다).
# git과 동일한 휴리스틱으로 앞부분에 NUL 바이트가 있으면 바이너리로 판정한다.
is_binary() {
	local total stripped
	total=$(LC_ALL=C head -c 8000 "$1" 2>/dev/null | wc -c)
	stripped=$(LC_ALL=C head -c 8000 "$1" 2>/dev/null | LC_ALL=C tr -d '\000' | wc -c)
	[[ "$total" != "$stripped" ]]
}

while IFS= read -r file; do
	# 심볼릭 링크는 저장소 밖 파일을 가리킬 수 있으므로 -f 검사(링크를 따라간다)보다 먼저 건너뛴다
	[[ -L "$file" ]] && continue
	[[ -f "$file" ]] || continue
	[[ -s "$file" ]] || continue

	is_binary "$file" && continue

	last_byte=$(tail -c 1 "$file" | od -An -tx1 | tr -d ' ')
	if [[ "$last_byte" != "0a" ]]; then
		printf '\n' >>"$file"
		echo "개행 문자 추가: $file"
	fi
done < <(git ls-files 2>/dev/null || true)

exit 0
