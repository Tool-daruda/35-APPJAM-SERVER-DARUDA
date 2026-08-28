package com.daruda.darudaserver.global.scraper;

import java.io.IOException;
import java.net.URI;
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

	public Optional<OgMetadata> fetch(final String url) {
		if (url == null || url.isBlank()) {
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
			return Optional.of(parse(document, url));
		} catch (IOException | RuntimeException e) {
			log.warn("OG 메타데이터 수집 실패: url={}", url, e);
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

	private static String defaultFavicon(final String pageUrl) {
		try {
			URI uri = URI.create(pageUrl);
			if (uri.getScheme() == null || uri.getHost() == null) {
				return null;
			}
			return uri.getScheme() + "://" + uri.getHost() + "/favicon.ico";
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
}
