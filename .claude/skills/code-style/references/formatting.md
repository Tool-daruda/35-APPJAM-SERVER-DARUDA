# 포매팅 · import (daruda)

## 포매팅 (자동 검증됨)

| 규칙 | 값 |
|------|-----|
| 들여쓰기 | **탭** (스페이스 들여쓰기는 Checkstyle 위반) |
| 탭 너비 | 4 |
| 한 줄 최대 길이 | 120자 (package/import/URL 제외) |
| 파일 끝 | 개행 필수 |
| 개행 문자 | LF |
| 인코딩 | UTF-8 |
| 줄 끝 공백 | 금지 |
| 중괄호 | K&R 스타일 (`if (x) {` — 여는 중괄호 같은 줄) |
| 단일 문장 | 중괄호 필수 (`if (x) return;` 금지) |
| 한 줄에 한 문장 | 필수 |

## import 순서

그룹 순서: `java.` → `javax.` → `org.` → `net.` → `com.` → 기타. **그룹 사이에 빈 줄 1개**, 그룹 내부는 알파벳순, 그룹 내부에 빈 줄 금지. **와일드카드(`*`) import 금지** (테스트의 static import는 예외적으로 사용 중).

```java
package com.daruda.darudaserver.domain.board.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.daruda.darudaserver.domain.board.entity.Board;
import com.daruda.darudaserver.global.error.code.ErrorCode;

import lombok.RequiredArgsConstructor;
```

> `lombok.`, `jakarta.`는 `com.` 뒤 "기타" 그룹에 위치한다. 기존 파일들의 배치를 따르면 된다.

## Checkstyle 위반 해결

Checkstyle에는 **자동 수정 기능이 없다.** `build/reports/checkstyle/main.html`(테스트는 `test.html`)에서 지적 위치를 확인해 직접 고친다.

흔한 원인:

| 위반 | 해결 |
|------|------|
| `Indentation` | 스페이스 들여쓰기 → 탭으로 교체 |
| `LineLength` | 120자 초과 → 인자·체이닝 줄바꿈 |
| `ImportOrder` | 그룹 순서 또는 그룹 사이 빈 줄 누락 |
| `UnusedImports` | 안 쓰는 import 제거 |
| `AvoidStarImport` | 와일드카드 import 전개 |
| `AbbreviationAsWordInName` | 연속 대문자 약어 → `HttpClient` 형태 |

editorconfig 위반(개행/공백/인코딩)만 `./gradlew editorconfigFormat`으로 자동 수정된다. IDE에서는 `.editorconfig`가 자동 적용되므로 탭 들여쓰기는 대부분 자동으로 맞는다.
