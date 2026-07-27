#!/usr/bin/env bash
# PR 전 전체 검증 스크립트
# 실행: ./.claude/scripts/check-all.sh

set -euo pipefail

# 프로젝트 루트: Claude Code가 주입하는 $CLAUDE_PROJECT_DIR 우선, 없으면 이 스크립트 위치 기준
ROOT="${CLAUDE_PROJECT_DIR:-$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)}"
cd "$ROOT"

echo "========================================"
echo "  PR 전 전체 검증"
echo "========================================"

echo ""
echo "[1/4] editorconfig 자동 수정 (개행/공백/인코딩)..."
./gradlew editorconfigFormat --quiet
echo "✓ editorconfig format 완료"

echo ""
echo "[2/4] editorconfig 검증..."
./gradlew editorconfigCheck
echo "✓ editorconfig 통과"

echo ""
echo "[3/4] Checkstyle 스타일 검증 (naver rules, 경고 0)..."
./gradlew checkstyleMain checkstyleTest
echo "✓ Checkstyle 통과"

echo ""
echo "[4/4] 전체 테스트..."
./gradlew test
echo "✓ 테스트 통과"

echo ""
echo "========================================"
echo "  모든 검증 통과! PR 준비 완료."
echo "========================================"
