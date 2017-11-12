package com.github.ljtfreitas.restify.http.client.message;

import static org.junit.Assert.assertEquals;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Test;

public class HeaderTest {

	@Test
	public void shouldGenerateAcceptHeaderFromSingleContentType() {
		Header header = Header.accept("application/json");

		assertEquals("Accept", header.name());
		assertEquals("application/json", header.value());
	}

	@Test
	public void shouldGenerateAcceptHeaderFromSingleContentTypeObject() {
		Header header = Header.accept(ContentType.of("application/json"));

		assertEquals("Accept", header.name());
		assertEquals("application/json", header.value());
	}

	@Test
	public void shouldGenerateAcceptHeaderFromMultipleContentTypes() {
		Header header = Header.accept("application/json", "application/xml");

		assertEquals("Accept", header.name());
		assertEquals("application/json, application/xml", header.value());
	}

	@Test
	public void shouldGenerateAcceptHeaderFromMultipleContentTypeObjects() {
		Header header = Header.accept(ContentType.of("application/json"), ContentType.of("application/xml"));

		assertEquals("Accept", header.name());
		assertEquals("application/json, application/xml", header.value());
	}

	@Test
	public void shouldGenerateAcceptHeaderFromCollectionOfContentTypeObjects() {
		Header header = Header
				.accept(Arrays.asList(ContentType.of("application/json"), ContentType.of("application/xml")));

		assertEquals("Accept", header.name());
		assertEquals("application/json, application/xml", header.value());
	}

	@Test
	public void shouldGenerateAcceptCharsetHeaderFromSingleCharset() {
		Header header = Header.acceptCharset("UTF-8");

		assertEquals("Accept-Charset", header.name());
		assertEquals("utf-8", header.value());
	}

	@Test
	public void shouldGenerateAcceptCharsetHeaderFromSingleCharsetObject() {
		Header header = Header.acceptCharset(Charset.forName("UTF-8"));

		assertEquals("Accept-Charset", header.name());
		assertEquals("utf-8", header.value());
	}

	@Test
	public void shouldGenerateAcceptCharsetHeaderFromMultipleCharsets() {
		Header header = Header.acceptCharset("UTF-8", "ISO-8859-1");

		assertEquals("Accept-Charset", header.name());
		assertEquals("utf-8, iso-8859-1", header.value());
	}

	@Test
	public void shouldGenerateAcceptCharsetFromMultipleCharsetObjects() {
		Header header = Header.acceptCharset(Charset.forName("UTF-8"), Charset.forName("ISO-8859-1"));

		assertEquals("Accept-Charset", header.name());
		assertEquals("utf-8, iso-8859-1", header.value());
	}

	@Test
	public void shouldGenerateAcceptCharsetHeaderFromCollectionOfCharsetObjects() {
		Header header = Header.acceptCharset(Arrays.asList(Charset.forName("UTF-8"), Charset.forName("ISO-8859-1")));

		assertEquals("Accept-Charset", header.name());
		assertEquals("utf-8, iso-8859-1", header.value());
	}

	@Test
	public void shouldGenerateAcceptEncodingHeaderFromSingleAlgorithm() {
		Header header = Header.acceptEncoding("gzip");

		assertEquals("Accept-Encoding", header.name());
		assertEquals("gzip", header.value());
	}

	@Test
	public void shouldGenerateAcceptEncodingHeaderFromMultipleAlgorithms() {
		Header header = Header.acceptEncoding("gzip", "deflate");

		assertEquals("Accept-Encoding", header.name());
		assertEquals("gzip, deflate", header.value());
	}

	@Test
	public void shouldGenerateAcceptEncodingHeaderFromCollectionOfAlgorithms() {
		Header header = Header.acceptEncoding(Arrays.asList("gzip", "deflate"));

		assertEquals("Accept-Encoding", header.name());
		assertEquals("gzip, deflate", header.value());
	}

	@Test
	public void shouldGenerateAcceptLanguageHeaderFromSingleLanguage() {
		Header header = Header.acceptLanguage("pt");

		assertEquals("Accept-Language", header.name());
		assertEquals("pt", header.value());
	}

	@Test
	public void shouldGenerateAcceptLanguageHeaderFromMultipleLanguages() {
		Header header = Header.acceptLanguage("pt", "en");

		assertEquals("Accept-Language", header.name());
		assertEquals("pt, en", header.value());
	}

	@Test
	public void shouldGenerateAcceptLanguageHeaderFromSingleLocaleObject() {
		Header header = Header.acceptLanguage(new Locale("pt", "BR"));

		assertEquals("Accept-Language", header.name());
		assertEquals("pt-BR", header.value());
	}

	@Test
	public void shouldGenerateAcceptLanguageHeaderFromMultipleLocaleObjects() {
		Header header = Header.acceptLanguage(new Locale("pt", "BR"), new Locale("en", "US"));

		assertEquals("Accept-Language", header.name());
		assertEquals("pt-BR, en-US", header.value());
	}

	@Test
	public void shouldGenerateAcceptVersionHeader() {
		Header header = Header.acceptVersion("v1");

		assertEquals("Accept-Version", header.name());
		assertEquals("v1", header.value());
	}

	@Test
	public void shouldGenerateConnectionHeaderFromSingleValue() {
		Header header = Header.connection("close");

		assertEquals("Connection", header.name());
		assertEquals("close", header.value());
	}

	@Test
	public void shouldGenerateConnectionHeaderFromMultipleValues() {
		Header header = Header.connection("keep-alive", "any");

		assertEquals("Connection", header.name());
		assertEquals("keep-alive, any", header.value());
	}

	@Test
	public void shouldGenerateContentDispositionHeaderFromHeader() {
		Header header = Header.contentDisposition("form-data");

		assertEquals("Content-Disposition", header.name());
		assertEquals("form-data", header.value());
	}

	@Test
	public void shouldGenerateContentEncodingHeaderFromHeader() {
		Header header = Header.contentEncoding("gzip");

		assertEquals("Content-Encoding", header.name());
		assertEquals("gzip", header.value());
	}

	@Test
	public void shouldGenerateContentLanguageHeaderFromLanguage() {
		Header header = Header.contentLanguage("pt");

		assertEquals("Content-Language", header.name());
		assertEquals("pt", header.value());
	}

	@Test
	public void shouldGenerateContentLanguageHeaderFromLocaleObject() {
		Header header = Header.contentLanguage(new Locale("pt", "BR"));

		assertEquals("Content-Language", header.name());
		assertEquals("pt-BR", header.value());
	}

	@Test
	public void shouldGenerateContentLengthHeaderFromLength() {
		Header header = Header.contentLength(1000);

		assertEquals("Content-Length", header.name());
		assertEquals("1000", header.value());
	}

	@Test
	public void shouldGenerateContentLocationHeaderFromLocation() {
		Header header = Header.contentLocation("/index.html");

		assertEquals("Content-Location", header.name());
		assertEquals("/index.html", header.value());
	}

	@Test
	public void shouldGenerateContentLocationHeaderFromUrlLocation() throws MalformedURLException {
		Header header = Header.contentLocation(new URL("http://my.site.com/index.html"));

		assertEquals("Content-Location", header.name());
		assertEquals("http://my.site.com/index.html", header.value());
	}

	@Test
	public void shouldGenerateContentLocationHeaderFromUriLocation() {
		Header header = Header.contentLocation(URI.create("/index.html"));

		assertEquals("Content-Location", header.name());
		assertEquals("/index.html", header.value());
	}

	@Test
	public void shouldGenerateContentRangeHeaderFromDirectives() {
		Header header = Header.contentRange("bytes", 200, 1000, 10000);

		assertEquals("Content-Range", header.name());
		assertEquals("bytes 200-1000/10000", header.value());
	}

	@Test
	public void shouldGenerateContentTypeHeaderFromContentType() {
		Header header = Header.contentType("application/json");

		assertEquals("Content-Type", header.name());
		assertEquals("application/json", header.value());
	}

	@Test
	public void shouldGenerateContentTypeHeaderFromContentTypeWithParameters() {
		Header header = Header.contentType(ContentType.of("application/json; charset=utf-8"));

		assertEquals("Content-Type", header.name());
		assertEquals("application/json; charset=utf-8", header.value());
	}

	@Test
	public void shouldGenerateCookieHeaderFromSingleCookie() {
		Header header = Header.cookie("sessionid=abc1234");

		assertEquals("Cookie", header.name());
		assertEquals("sessionid=abc1234", header.value());
	}

	@Test
	public void shouldGenerateCookieHeaderFromMultipleCookies() {
		Header header = Header.cookie("sessionid=abc1234", "other-cookie=other-value");

		assertEquals("Cookie", header.name());
		assertEquals("sessionid=abc1234; other-cookie=other-value", header.value());
	}

	@Test
	public void shouldGenerateCookieHeaderFromSingleCookieObject() {
		Header header = Header.cookie(new Cookie("sessionid", "abc1234"));

		assertEquals("Cookie", header.name());
		assertEquals("sessionid=abc1234", header.value());
	}

	@Test
	public void shouldGenerateCookieHeaderFromMultipleCookieObjects() {
		Header header = Header.cookie(new Cookie("sessionid", "abc1234"), new Cookie("other-cookie", "other-value"));

		assertEquals("Cookie", header.name());
		assertEquals("sessionid=abc1234; other-cookie=other-value", header.value());
	}

	@Test
	public void shouldGenerateCookieHeaderFromCookiesObject() {
		Cookies cookies = new Cookies();
		cookies.add(new Cookie("sessionid", "abc1234"));
		cookies.add(new Cookie("other-cookie", "other-value"));

		Header header = Header.cookie(cookies);

		assertEquals("Cookie", header.name());
		assertEquals("sessionid=abc1234; other-cookie=other-value", header.value());
	}

	@Test
	public void shouldGenerateDateHeaderFromTimestamp() {
		LocalDateTime datetime = LocalDateTime.of(2014, 02, 03, 9, 00);

		Header header = Header.date(datetime.toInstant(ZoneOffset.UTC).toEpochMilli());

		assertEquals("Date", header.name());
		assertEquals("Mon, 03 Feb 2014 09:00:00 GMT", header.value());
	}

	@Test
	public void shouldGenerateDateHeaderFromDate() {
		LocalDateTime datetime = LocalDateTime.of(2014, 02, 03, 9, 00);

		Header header = Header.date(Date.from(datetime.toInstant(ZoneOffset.UTC)));

		assertEquals("Date", header.name());
		assertEquals("Mon, 03 Feb 2014 09:00:00 GMT", header.value());
	}

	@Test
	public void shouldGenerateDateHeaderFromCalendar() {
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		calendar.set(Calendar.YEAR, 2014);
		calendar.set(Calendar.MONTH, Calendar.FEBRUARY);
		calendar.set(Calendar.DAY_OF_MONTH, 03);
		calendar.set(Calendar.HOUR_OF_DAY, 9);
		calendar.set(Calendar.MINUTE, 00);
		calendar.set(Calendar.SECOND, 00);

		Header header = Header.date(calendar);

		assertEquals("Date", header.name());
		assertEquals("Mon, 03 Feb 2014 09:00:00 GMT", header.value());
	}

	@Test
	public void shouldGenerateDateHeaderFromLocalDateTime() {
		LocalDateTime datetime = LocalDateTime.of(2014, 02, 03, 9, 00);

		Header header = Header.date(datetime);

		assertEquals("Date", header.name());
		assertEquals("Mon, 03 Feb 2014 09:00:00 GMT", header.value());
	}

	@Test
	public void shouldGenerateDateHeaderFromZonedDateTime() {
		LocalDateTime datetime = LocalDateTime.of(2014, 02, 03, 9, 00);
		ZonedDateTime zonedDateTime = ZonedDateTime.of(datetime, ZoneId.of("GMT"));

		Header header = Header.date(zonedDateTime);

		assertEquals("Date", header.name());
		assertEquals("Mon, 03 Feb 2014 09:00:00 GMT", header.value());
	}

	@Test
	public void shouldGenerateDateHeaderFromInstant() {
		LocalDateTime datetime = LocalDateTime.of(2014, 02, 03, 9, 00);

		Header header = Header.date(datetime.toInstant(ZoneOffset.UTC));

		assertEquals("Date", header.name());
		assertEquals("Mon, 03 Feb 2014 09:00:00 GMT", header.value());
	}

	@Test
	public void shouldGenerateExpectHeader() {
		Header header = Header.expect();

		assertEquals("Expect", header.name());
		assertEquals("100-Continue", header.value());
	}

	@Test
	public void shouldGenerateFromHeaderFromEmailAddress() {
		Header header = Header.from("sample@java-restify.com");

		assertEquals("From", header.name());
		assertEquals("sample@java-restify.com", header.value());
	}

	@Test
	public void shouldGenerateHostHeaderFromHost() {
		Header header = Header.host("my.site.com");

		assertEquals("Host", header.name());
		assertEquals("my.site.com", header.value());
	}

	@Test
	public void shouldGenerateHostHeaderFromUrlHost() throws MalformedURLException {
		Header header = Header.host(new URL("http://my.site.com"));

		assertEquals("Host", header.name());
		assertEquals("my.site.com", header.value());
	}

	@Test
	public void shouldGenerateHostHeaderFromUrlHostWithPort() throws MalformedURLException {
		Header header = Header.host(new URL("http://my.site.com:8080"));

		assertEquals("Host", header.name());
		assertEquals("my.site.com:8080", header.value());
	}

	@Test
	public void shouldGenerateHostHeaderFromUriHost() {
		Header header = Header.host(URI.create("http://my.site.com"));

		assertEquals("Host", header.name());
		assertEquals("my.site.com", header.value());
	}

	@Test
	public void shouldGenerateHostHeaderFromUriHostWithPort() {
		Header header = Header.host(URI.create("http://my.site.com:8080"));

		assertEquals("Host", header.name());
		assertEquals("my.site.com:8080", header.value());
	}

	@Test
	public void shouldGenerateIfMatchHeaderFromSingleETag() {
		Header header = Header.ifMatch("abc1234");

		assertEquals("If-Match", header.name());
		assertEquals("\"abc1234\"", header.value());
	}

	@Test
	public void shouldGenerateIfMatchHeaderFromMultipleETags() {
		Header header = Header.ifMatch("abc1234", "qwe5678");

		assertEquals("If-Match", header.name());
		assertEquals("\"abc1234\", \"qwe5678\"", header.value());
	}

	@Test
	public void shouldGenerateIfMatchHeaderFromSingleETagObject() {
		Header header = Header.ifMatch(ETag.of("abc1234"));

		assertEquals("If-Match", header.name());
		assertEquals("\"abc1234\"", header.value());
	}

	@Test
	public void shouldGenerateIfMatchHeaderFromMultipleETagObjects() {
		Header header = Header.ifMatch(ETag.of("abc1234"), ETag.weak("qwe5678"));

		assertEquals("If-Match", header.name());
		assertEquals("\"abc1234\", W/\"qwe5678\"", header.value());
	}

	@Test
	public void shouldGenerateIfModifiedSinceHeaderFromTimestamp() {
		LocalDateTime datetime = LocalDateTime.of(2014, 02, 03, 9, 00);

		Header header = Header.ifModifiedSince(datetime.toInstant(ZoneOffset.UTC).toEpochMilli());

		assertEquals("If-Modified-Since", header.name());
		assertEquals("Mon, 03 Feb 2014 09:00:00 GMT", header.value());
	}

	@Test
	public void shouldGenerateIfModifiedSinceHeaderFromDate() {
		LocalDateTime datetime = LocalDateTime.of(2014, 02, 03, 9, 00);

		Header header = Header.ifModifiedSince(Date.from(datetime.toInstant(ZoneOffset.UTC)));

		assertEquals("If-Modified-Since", header.name());
		assertEquals("Mon, 03 Feb 2014 09:00:00 GMT", header.value());
	}

	@Test
	public void shouldGenerateIfModifiedSinceHeaderFromCalendar() {
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		calendar.set(Calendar.YEAR, 2014);
		calendar.set(Calendar.MONTH, Calendar.FEBRUARY);
		calendar.set(Calendar.DAY_OF_MONTH, 03);
		calendar.set(Calendar.HOUR_OF_DAY, 9);
		calendar.set(Calendar.MINUTE, 00);
		calendar.set(Calendar.SECOND, 00);

		Header header = Header.ifModifiedSince(calendar);

		assertEquals("If-Modified-Since", header.name());
		assertEquals("Mon, 03 Feb 2014 09:00:00 GMT", header.value());
	}

	@Test
	public void shouldGenerateIfModifiedSinceHeaderFromLocalDateTime() {
		LocalDateTime datetime = LocalDateTime.of(2014, 02, 03, 9, 00);

		Header header = Header.ifModifiedSince(datetime);

		assertEquals("If-Modified-Since", header.name());
		assertEquals("Mon, 03 Feb 2014 09:00:00 GMT", header.value());
	}

	@Test
	public void shouldGenerateIfModifiedSinceHeaderFromZonedDateTime() {
		LocalDateTime datetime = LocalDateTime.of(2014, 02, 03, 9, 00);
		ZonedDateTime zonedDateTime = ZonedDateTime.of(datetime, ZoneId.of("GMT"));

		Header header = Header.ifModifiedSince(zonedDateTime);

		assertEquals("If-Modified-Since", header.name());
		assertEquals("Mon, 03 Feb 2014 09:00:00 GMT", header.value());
	}

	@Test
	public void shouldGenerateIfModifiedSinceHeaderFromInstant() {
		LocalDateTime datetime = LocalDateTime.of(2014, 02, 03, 9, 00);

		Header header = Header.ifModifiedSince(datetime.toInstant(ZoneOffset.UTC));

		assertEquals("If-Modified-Since", header.name());
		assertEquals("Mon, 03 Feb 2014 09:00:00 GMT", header.value());
	}

	@Test
	public void shouldGenerateIfNoneMatchHeaderFromSingleETag() {
		Header header = Header.ifNoneMatch("abc1234");

		assertEquals("If-None-Match", header.name());
		assertEquals("\"abc1234\"", header.value());
	}

	@Test
	public void shouldGenerateIfNoneMatchHeaderFromMultipleETags() {
		Header header = Header.ifNoneMatch("abc1234", "qwe5678");

		assertEquals("If-None-Match", header.name());
		assertEquals("\"abc1234\", \"qwe5678\"", header.value());
	}

	@Test
	public void shouldGenerateIfNoneMatchHeaderFromSingleETagObject() {
		Header header = Header.ifNoneMatch(ETag.of("abc1234"));

		assertEquals("If-None-Match", header.name());
		assertEquals("\"abc1234\"", header.value());
	}

	@Test
	public void shouldGenerateIfNoneMatchHeaderFromMultipleETagObjects() {
		Header header = Header.ifNoneMatch(ETag.of("abc1234"), ETag.weak("qwe5678"));

		assertEquals("If-None-Match", header.name());
		assertEquals("\"abc1234\", W/\"qwe5678\"", header.value());
	}

	@Test
	public void shouldGenerateIfRangeHeaderFromTimestamp() {
		LocalDateTime datetime = LocalDateTime.of(2014, 02, 03, 9, 00);

		Header header = Header.ifRange(datetime.toInstant(ZoneOffset.UTC).toEpochMilli());

		assertEquals("If-Range", header.name());
		assertEquals("Mon, 03 Feb 2014 09:00:00 GMT", header.value());
	}

	@Test
	public void shouldGenerateIfRangeHeaderFromDate() {
		LocalDateTime datetime = LocalDateTime.of(2014, 02, 03, 9, 00);

		Header header = Header.ifRange(Date.from(datetime.toInstant(ZoneOffset.UTC)));

		assertEquals("If-Range", header.name());
		assertEquals("Mon, 03 Feb 2014 09:00:00 GMT", header.value());
	}

	@Test
	public void shouldGenerateIfRangeHeaderFromCalendar() {
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		calendar.set(Calendar.YEAR, 2014);
		calendar.set(Calendar.MONTH, Calendar.FEBRUARY);
		calendar.set(Calendar.DAY_OF_MONTH, 03);
		calendar.set(Calendar.HOUR_OF_DAY, 9);
		calendar.set(Calendar.MINUTE, 00);
		calendar.set(Calendar.SECOND, 00);

		Header header = Header.ifRange(calendar);

		assertEquals("If-Range", header.name());
		assertEquals("Mon, 03 Feb 2014 09:00:00 GMT", header.value());
	}

	@Test
	public void shouldGenerateIfRangeHeaderFromLocalDateTime() {
		LocalDateTime datetime = LocalDateTime.of(2014, 02, 03, 9, 00);

		Header header = Header.ifRange(datetime);

		assertEquals("If-Range", header.name());
		assertEquals("Mon, 03 Feb 2014 09:00:00 GMT", header.value());
	}

	@Test
	public void shouldGenerateIfRangeHeaderFromZonedDateTime() {
		LocalDateTime datetime = LocalDateTime.of(2014, 02, 03, 9, 00);
		ZonedDateTime zonedDateTime = ZonedDateTime.of(datetime, ZoneId.of("GMT"));

		Header header = Header.ifRange(zonedDateTime);

		assertEquals("If-Range", header.name());
		assertEquals("Mon, 03 Feb 2014 09:00:00 GMT", header.value());
	}

	@Test
	public void shouldGenerateIfRangeHeaderFromInstant() {
		LocalDateTime datetime = LocalDateTime.of(2014, 02, 03, 9, 00);

		Header header = Header.ifRange(datetime.toInstant(ZoneOffset.UTC));

		assertEquals("If-Range", header.name());
		assertEquals("Mon, 03 Feb 2014 09:00:00 GMT", header.value());
	}

	@Test
	public void shouldGenerateIfRangeHeaderFromETag() {
		Header header = Header.ifRange("abc1234");

		assertEquals("If-Range", header.name());
		assertEquals("\"abc1234\"", header.value());
	}

	@Test
	public void shouldGenerateIfRangeHeaderFromETagObject() {
		Header header = Header.ifRange(ETag.of("abc1234"));

		assertEquals("If-Range", header.name());
		assertEquals("\"abc1234\"", header.value());
	}

	@Test
	public void shouldGenerateIfUnmodifiedSinceHeaderFromTimestamp() {
		LocalDateTime datetime = LocalDateTime.of(2014, 02, 03, 9, 00);

		Header header = Header.ifUnmodifiedSince(datetime.toInstant(ZoneOffset.UTC).toEpochMilli());

		assertEquals("If-Unmodified-Since", header.name());
		assertEquals("Mon, 03 Feb 2014 09:00:00 GMT", header.value());
	}

	@Test
	public void shouldGenerateIfUnmodifiedSinceHeaderFromDate() {
		LocalDateTime datetime = LocalDateTime.of(2014, 02, 03, 9, 00);

		Header header = Header.ifUnmodifiedSince(Date.from(datetime.toInstant(ZoneOffset.UTC)));

		assertEquals("If-Unmodified-Since", header.name());
		assertEquals("Mon, 03 Feb 2014 09:00:00 GMT", header.value());
	}

	@Test
	public void shouldGenerateIfUnmodifiedSinceHeaderFromCalendar() {
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		calendar.set(Calendar.YEAR, 2014);
		calendar.set(Calendar.MONTH, Calendar.FEBRUARY);
		calendar.set(Calendar.DAY_OF_MONTH, 03);
		calendar.set(Calendar.HOUR_OF_DAY, 9);
		calendar.set(Calendar.MINUTE, 00);
		calendar.set(Calendar.SECOND, 00);

		Header header = Header.ifUnmodifiedSince(calendar);

		assertEquals("If-Unmodified-Since", header.name());
		assertEquals("Mon, 03 Feb 2014 09:00:00 GMT", header.value());
	}

	@Test
	public void shouldGenerateIfUnmodifiedSinceHeaderFromLocalDateTime() {
		LocalDateTime datetime = LocalDateTime.of(2014, 02, 03, 9, 00);

		Header header = Header.ifUnmodifiedSince(datetime);

		assertEquals("If-Unmodified-Since", header.name());
		assertEquals("Mon, 03 Feb 2014 09:00:00 GMT", header.value());
	}

	@Test
	public void shouldGenerateIfUnmodifiedSinceHeaderFromZonedDateTime() {
		LocalDateTime datetime = LocalDateTime.of(2014, 02, 03, 9, 00);
		ZonedDateTime zonedDateTime = ZonedDateTime.of(datetime, ZoneId.of("GMT"));

		Header header = Header.ifUnmodifiedSince(zonedDateTime);

		assertEquals("If-Unmodified-Since", header.name());
		assertEquals("Mon, 03 Feb 2014 09:00:00 GMT", header.value());
	}

	@Test
	public void shouldGenerateLastModifiedHeaderFromInstant() {
		LocalDateTime datetime = LocalDateTime.of(2014, 02, 03, 9, 00);

		Header header = Header.ifUnmodifiedSince(datetime.toInstant(ZoneOffset.UTC));

		assertEquals("If-Unmodified-Since", header.name());
		assertEquals("Mon, 03 Feb 2014 09:00:00 GMT", header.value());
	}

	@Test
	public void shouldGenerateLastModifiedHeaderFromTimestamp() {
		LocalDateTime datetime = LocalDateTime.of(2014, 02, 03, 9, 00);

		Header header = Header.lastModified(datetime.toInstant(ZoneOffset.UTC).toEpochMilli());

		assertEquals("Last-Modified", header.name());
		assertEquals("Mon, 03 Feb 2014 09:00:00 GMT", header.value());
	}

	@Test
	public void shouldGenerateRangeHeaderFromSingleRange() {
		Header header = Header.range(new Range.Builder().unit("bytes").start(1000l).end(2000l).build());

		assertEquals("Range", header.name());
		assertEquals("bytes=1000-2000", header.value());
	}

	@Test
	public void shouldGenerateRangeHeaderFromMultipleRanges() {
		Header header = Header.range(new Range.Builder().unit("bytes").start(1000l).end(2000l).build(),
				Range.bytes(2000l, 4000l), Range.bytes(4000l));

		assertEquals("Range", header.name());
		assertEquals("bytes=1000-2000, 2000-4000, 4000-", header.value());
	}

	@Test
	public void shouldGenerateRangeHeaderFromCollectionOfRanges() {
		Header header = Header.range(Arrays.asList(new Range.Builder().unit("bytes").start(1000l).end(2000l).build(),
				Range.bytes(2000l, 4000l), Range.bytes(4000l)));

		assertEquals("Range", header.name());
		assertEquals("bytes=1000-2000, 2000-4000, 4000-", header.value());
	}

	@Test
	public void shouldGenerateReferHeaderFromUrl() {
		Header header = Header.referer("http://my.site.com");

		assertEquals("Referer", header.name());
		assertEquals("http://my.site.com", header.value());
	}

	@Test
	public void shouldGenerateRefererHeaderFromUrlReferer() throws MalformedURLException {
		Header header = Header.referer(new URL("http://my.site.com"));

		assertEquals("Referer", header.name());
		assertEquals("http://my.site.com", header.value());
	}

	@Test
	public void shouldGenerateRefererHeaderFromUriReferer() {
		Header header = Header.referer(URI.create("http://my.site.com"));

		assertEquals("Referer", header.name());
		assertEquals("http://my.site.com", header.value());
	}
}
