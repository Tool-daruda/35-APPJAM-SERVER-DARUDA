package com.daruda.darudaserver.global.scraper;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Optional;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 블로그 URL의 Open Graph / <meta> 태그를 긁어 카드 노출용 메타데이터를 만든다.
 * 모든 수집은 best-effort다. 실패 시 예외를 전파하지 않고 빈 값을 돌려준다.
 */
@Slf4j
@Component
public class OgMetadataScraper {

	private static final int TIMEOUT_MS = 3000;
	private static final int MAX_BODY_SIZE = 2 * 1024 * 1024;
	private static final String USER_AGENT = "Mozilla/5.0 (compatible; DarudaBot/1.0)";
	private static final String FAVICON_SELECTOR = "link[rel~=(?i)(shortcut )?icon]";
	private static final String UNKNOWN_HOST = "unknown";

	public Optional<OgMetadata> fetch(final String url) {
		if (url == null || url.isBlank()) {
			return Optional.empty();
		}
		// 최초 URL만 검증한다. 리다이렉트 경유지는 재검증하지 않는다(수용된 범위).
		if (!isSafePublicHttpUrl(url)) {
			log.warn("차단된 호스트로의 스크래핑 시도: host={}", safeHost(url));
			return Optional.empty();
		}
		try {
			Document document = Jsoup.connect(url)
				.timeout(TIMEOUT_MS)
				.followRedirects(true)
				.ignoreHttpErrors(true)
				.userAgent(USER_AGENT)
				.maxBodySize(MAX_BODY_SIZE)
				.get();
			String finalUrl = firstNonBlank(document.location(), url);
			return Optional.of(parse(document, finalUrl));
		} catch (IOException | RuntimeException e) {
			log.warn("OG 메타데이터 수집 실패: host={}", safeHost(url), e);
			return Optional.empty();
		}
	}

	public OgMetadata parse(final Document doc, final String pageUrl) {
		String title = firstNonBlank(
			attr(doc, "meta[property=og:title]", "content"),
			doc.title()
		);
		String thumbnailUrl = firstNonBlank(
			attr(doc, "meta[property=og:image]", "abs:content"),
			attr(doc, "meta[name=twitter:image]", "abs:content")
		);
		String summary = firstNonBlank(
			attr(doc, "meta[property=og:description]", "content"),
			attr(doc, "meta[name=description]", "content")
		);
		String siteName = firstNonBlank(
			attr(doc, "meta[property=og:site_name]", "content"),
			host(pageUrl)
		);
		String faviconUrl = firstNonBlank(
			attr(doc, FAVICON_SELECTOR, "abs:href"),
			defaultFavicon(pageUrl)
		);
		return new OgMetadata(title, thumbnailUrl, summary, siteName, faviconUrl);
	}

	/**
	 * 스크래핑 대상 URL이 공개 인터넷상의 http(s) 주소인지 가볍게 검증한다(SSRF 완화).
	 * 최초 URL만 검사하며, 리다이렉트 홉은 재검사하지 않는다(수용된 범위). package-private: 단위 테스트용.
	 */
	static boolean isSafePublicHttpUrl(final String url) {
		final URI uri;
		try {
			uri = URI.create(url.trim());
		} catch (IllegalArgumentException e) {
			return false;
		}
		String scheme = uri.getScheme();
		if (scheme == null || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))) {
			return false;
		}
		String host = uri.getHost();
		if (host == null || "localhost".equalsIgnoreCase(host)) {
			return false;
		}
		try {
			for (InetAddress addr : InetAddress.getAllByName(host)) {
				if (addr.isLoopbackAddress() || addr.isAnyLocalAddress() || addr.isLinkLocalAddress()
					|| addr.isSiteLocalAddress() || addr.isMulticastAddress()) {
					return false;
				}
			}
		} catch (UnknownHostException e) {
			return false;
		}
		return true;
	}

	private static String attr(final Document doc, final String cssQuery, final String attributeKey) {
		return doc.select(cssQuery).attr(attributeKey);
	}

	private static String firstNonBlank(final String... values) {
		for (String value : values) {
			if (value != null && !value.isBlank()) {
				return value.trim();
			}
		}
		return null;
	}

	private static String host(final String pageUrl) {
		try {
			return URI.create(pageUrl).getHost();
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	private static String safeHost(final String url) {
		String host = host(url);
		return host != null ? host : UNKNOWN_HOST;
	}

	private static String defaultFavicon(final String pageUrl) {
		try {
			URI uri = URI.create(pageUrl);
			if (uri.getScheme() == null || uri.getHost() == null) {
				return null;
			}
			// resolve 로 절대 URI에 맞추면 포트(:8080 등)가 보존된다.
			return uri.resolve("/favicon.ico").toString();
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
}
