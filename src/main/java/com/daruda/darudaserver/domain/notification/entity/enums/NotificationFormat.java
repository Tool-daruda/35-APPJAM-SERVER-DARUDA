package com.daruda.darudaserver.domain.notification.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationFormat {
	COMMENT_NOTIFICATION_TITLE("내가 쓴 글에 댓글이 달렸어요: %s"),
	COMMUNITY_BLOCK_NOTICE_TITLE("[공지] %s님의 신고가 접수되었습니다."),
	REGISTER_NOTICE_TITLE("[공지] daruda에 오신 걸 환영합니다!"),

	COMMENT_NOTIFICATION_CONTENT("제목: %s"),
	COMMUNITY_BLOCK_NOTICE_CONTENT("회원님께서는 \n"
		+ "커뮤니티이용규칙을 위반하여\n"
		+ "%s부로 %d일간 커뮤니티의 일부 활동을 제한합니다. \n"
		+ "문의사항이 있다면 홈 화면 우측 상단의 ‘문의하기’를 이용해 주시기 바랍니다.\n"
		+ "감사합니다."),
	REGISTER_NOTICE_CONTENT("안녕하세요,\n"
		+ "대학생활에 필요한 툴을 다루다, daruda입니다.\n"
		+ "daruda에서는 다양한 툴의 플랜, 기능, 특징 등을 한눈에 확인할 수 있어요.\n"
		+ "지금 회원님께 가장 잘 맞는 툴을 찾아보세요!\n"
		+ "툴에 대해 궁금한 점이 있다면, 커뮤니티에서 다른 유저들과 자유롭게 소통하며 정보를 나눌 수 있어요.\n"
		+ "회원님의 방문을 진심으로 환영합니다.\n"
		+ "감사합니다.");

	private final String messageFormat;
}
