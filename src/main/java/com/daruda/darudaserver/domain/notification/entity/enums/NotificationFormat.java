package com.daruda.darudaserver.domain.notification.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationFormat {
	// 댓글 알림 형식
	COMMENT_TITLE_TEXT_ONLY("%s"),
	COMMENT_TITLE_IMAGE_ONLY("[이미지]"),
	COMMENT_TITLE_IMAGE_WITH_TEXT("[이미지] %s"),
	COMMENT_CONTENT_BOARD_TITLE("%s"),

	// 공지사항 형식
	NOTICE_TITLE("[공지] %s"),
	NOTICE_CONTENT("팀 Faber"),

	// 서비스 제한 알림 형식
	COMMUNITY_BLOCK_NOTICE_TITLE("[공지] %s님의 신고가 접수되었습니다."),
	COMMUNITY_BLOCK_NOTICE_CONTENT("""
		회원님께서는\s
		커뮤니티이용규칙을 위반하여
		%s부로 %d일간 커뮤니티의 일부 활동을 제한합니다.\s
		문의사항이 있다면 홈 화면 우측 상단의 ‘문의하기’를 이용해 주시기 바랍니다.
		감사합니다.""");

	private final String messageFormat;

	public String format(Object... args) {
		return String.format(messageFormat, args);
	}
}
