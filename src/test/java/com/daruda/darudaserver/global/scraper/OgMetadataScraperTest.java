package com.daruda.darudaserver.global.scraper;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.jsoup.Jsoup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class OgMetadataScraperTest {

	private static final String PAGE_URL = "https://example.com/post";

	private final OgMetadataScraper scraper = new OgMetadataScraper();

	private OgMetadata parse(final String html) {
		return scraper.parse(Jsoup.parse(html, PAGE_URL), PAGE_URL);
	}

	@Nested
	@DisplayName("parse - OG/메타 태그 파싱")
	class Parse {

		@Test
		@DisplayName("전체 OG 태그가 있으면 모든 필드를 채운다")
		void parse_fullOgTags() {
			// given
			String html = """
				<html><head>
				<title>fallback title</title>
				<meta property="og:title" content="진짜 제목">
				<meta property="og:image" content="https://cdn.example.com/thumb.png">
				<meta property="og:description" content="요약 설명">
				<meta property="og:site_name" content="예시 블로그">
				<link rel="icon" href="https://example.com/assets/favicon.ico">
				</head><body></body></html>
				""";

			// when
			OgMetadata result = parse(html);

			// then
			assertThat(result.title()).isEqualTo("진짜 제목");
			assertThat(result.thumbnailUrl()).isEqualTo("https://cdn.example.com/thumb.png");
			assertThat(result.summary()).isEqualTo("요약 설명");
			assertThat(result.siteName()).isEqualTo("예시 블로그");
			assertThat(result.faviconUrl()).isEqualTo("https://example.com/assets/favicon.ico");
		}

		@Test
		@DisplayName("title 태그와 meta[name=description]만 있으면 폴백으로 채운다")
		void parse_titleAndDescriptionFallback() {
			// given
			String html = """
				<html><head>
				<title>문서 제목</title>
				<meta name="description" content="문서 설명">
				</head><body></body></html>
				""";

			// when
			OgMetadata result = parse(html);

			// then
			assertThat(result.title()).isEqualTo("문서 제목");
			assertThat(result.summary()).isEqualTo("문서 설명");
			assertThat(result.thumbnailUrl()).isNull();
			assertThat(result.siteName()).isEqualTo("example.com");
			assertThat(result.faviconUrl()).isEqualTo("https://example.com/favicon.ico");
		}

		@Test
		@DisplayName("메타 태그가 전혀 없으면 title 은 <title>, siteName 은 host, favicon 은 기본 경로로 채우고 나머지는 null")
		void parse_noMetaAtAll() {
			// given
			String html = "<html><head><title>제목만 있음</title></head><body></body></html>";

			// when
			OgMetadata result = parse(html);

			// then
			assertThat(result.title()).isEqualTo("제목만 있음");
			assertThat(result.siteName()).isEqualTo("example.com");
			assertThat(result.faviconUrl()).isEqualTo("https://example.com/favicon.ico");
			assertThat(result.thumbnailUrl()).isNull();
			assertThat(result.summary()).isNull();
		}

		@Test
		@DisplayName("상대 경로 og:image 와 상대 경로 favicon 은 절대 URL 로 변환한다")
		void parse_relativeUrlsResolvedToAbsolute() {
			// given
			String html = """
				<html><head>
				<title>상대 경로</title>
				<meta property="og:image" content="/img/thumb.png">
				<link rel="shortcut icon" href="favicon.png">
				</head><body></body></html>
				""";

			// when
			OgMetadata result = parse(html);

			// then
			assertThat(result.thumbnailUrl()).isEqualTo("https://example.com/img/thumb.png");
			assertThat(result.faviconUrl()).isEqualTo("https://example.com/favicon.png");
		}

		@Test
		@DisplayName("포트가 있는 페이지 URL 이면 기본 favicon 경로에도 포트를 보존한다")
		void parse_defaultFaviconPreservesPort() {
			// given
			String html = "<html><head><title>포트 있음</title></head><body></body></html>";

			// when
			OgMetadata result = scraper.parse(Jsoup.parse(html, "http://example.com:8080/post"),
				"http://example.com:8080/post");

			// then
			assertThat(result.faviconUrl()).isEqualTo("http://example.com:8080/favicon.ico");
		}
	}

	@Nested
	@DisplayName("isSafePublicHttpUrl - SSRF 완화용 URL 검증")
	class IsSafePublicHttpUrl {

		@Test
		@DisplayName("공개 IP 주소의 http URL 은 허용한다")
		void isSafePublicHttpUrl_publicIp_true() {
			// when & then
			assertThat(OgMetadataScraper.isSafePublicHttpUrl("http://8.8.8.8/path")).isTrue();
		}

		@Test
		@DisplayName("공개 도메인의 https URL 은 허용한다 (DNS 미가용 환경에서는 스킵)")
		void isSafePublicHttpUrl_publicDomain_true() {
			// given
			assumeTrue(resolves("example.com"), "DNS 미가용 환경 - 스킵");

			// when & then
			assertThat(OgMetadataScraper.isSafePublicHttpUrl("https://example.com/post")).isTrue();
		}

		@Test
		@DisplayName("localhost 호스트는 차단한다")
		void isSafePublicHttpUrl_localhost_false() {
			// when & then
			assertThat(OgMetadataScraper.isSafePublicHttpUrl("http://localhost/admin")).isFalse();
		}

		@Test
		@DisplayName("루프백 IP 는 차단한다")
		void isSafePublicHttpUrl_loopbackIp_false() {
			// when & then
			assertThat(OgMetadataScraper.isSafePublicHttpUrl("https://127.0.0.1/secret")).isFalse();
		}

		@Test
		@DisplayName("링크 로컬(메타데이터 엔드포인트) IP 는 차단한다")
		void isSafePublicHttpUrl_linkLocalIp_false() {
			// when & then
			assertThat(OgMetadataScraper.isSafePublicHttpUrl("http://169.254.169.254/latest/meta-data/")).isFalse();
		}

		@Test
		@DisplayName("사설(사이트 로컬) IP 는 차단한다")
		void isSafePublicHttpUrl_siteLocalIp_false() {
			// when & then
			assertThat(OgMetadataScraper.isSafePublicHttpUrl("http://10.0.0.5/")).isFalse();
		}

		@Test
		@DisplayName("http(s) 가 아닌 스킴은 차단한다")
		void isSafePublicHttpUrl_nonHttpScheme_false() {
			// when & then
			assertThat(OgMetadataScraper.isSafePublicHttpUrl("ftp://example.com")).isFalse();
		}

		@Test
		@DisplayName("URL 로 파싱되지 않는 문자열은 차단한다")
		void isSafePublicHttpUrl_notAUrl_false() {
			// when & then
			assertThat(OgMetadataScraper.isSafePublicHttpUrl("not a url")).isFalse();
		}

		@Test
		@DisplayName("IPv6 Unique Local Address(fc00::/7) 는 차단한다")
		void isSafePublicHttpUrl_ipv6UniqueLocal_false() {
			// when & then
			assertThat(OgMetadataScraper.isSafePublicHttpUrl("http://[fc00::1]/")).isFalse();
			assertThat(OgMetadataScraper.isSafePublicHttpUrl("http://[fd00::1]/")).isFalse();
		}

		private boolean resolves(final String host) {
			try {
				InetAddress.getByName(host);
				return true;
			} catch (UnknownHostException e) {
				return false;
			}
		}
	}
}
