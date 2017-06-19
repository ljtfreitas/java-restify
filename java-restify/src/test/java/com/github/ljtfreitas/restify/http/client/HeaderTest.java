package com.github.ljtfreitas.restify.http.client;

import static org.junit.Assert.assertEquals;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.sql.Date;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Test;

import com.github.ljtfreitas.restify.http.contract.ContentType;
import com.github.ljtfreitas.restify.http.contract.Cookie;
import com.github.ljtfreitas.restify.http.contract.Cookies;

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
	public void shouldGenerateAcceptRangesHeader() {
		Header header = Header.acceptRanges("bytes");

		assertEquals("Accept-Ranges", header.name());
		assertEquals("bytes", header.value());
	}

	@Test
	public void shouldGenerateAcceptVersionHeader() {
		Header header = Header.acceptVersion("v1");

		assertEquals("Accept-Version", header.name());
		assertEquals("v1", header.value());
	}

	@Test
	public void shouldGenerateAccessControlAllowCredentialsHeader() {
		Header header = Header.accessControlAllowCredentials(true);

		assertEquals("Access-Control-Allow-Credentials", header.name());
		assertEquals("true", header.value());
	}

	@Test
	public void shouldGenerateAccessControlAllowHeadersHeaderFromSingleHeaderName() {
		Header header = Header.accessControlAllowHeaders("X-Custom-Header");

		assertEquals("Access-Control-Allow-Headers", header.name());
		assertEquals("X-Custom-Header", header.value());
	}

	@Test
	public void shouldGenerateAccessControlAllowHeadersHeaderFromMultipleHeaderNames() {
		Header header = Header.accessControlAllowHeaders("X-Custom-Header-1", "X-Custom-Header-2");

		assertEquals("Access-Control-Allow-Headers", header.name());
		assertEquals("X-Custom-Header-1, X-Custom-Header-2", header.value());
	}

	@Test
	public void shouldGenerateAccessControlAllowHeadersHeaderFromMultipleHeaderObjects() {
		Header customHeader1 = new Header("X-Custom-Header-1", "value1");
		Header customHeader2 = new Header("X-Custom-Header-2", "value2");

		Header header = Header.accessControlAllowHeaders(customHeader1, customHeader2);

		assertEquals("Access-Control-Allow-Headers", header.name());
		assertEquals("X-Custom-Header-1, X-Custom-Header-2", header.value());
	}

	@Test
	public void shouldGenerateAccessControlAllowHeadersHeaderFromHeadersObject() {
		Headers headers = new Headers(new Header("X-Custom-Header-1", "value1"),
				new Header("X-Custom-Header-2", "value2"));

		Header header = Header.accessControlAllowHeaders(headers);

		assertEquals("Access-Control-Allow-Headers", header.name());
		assertEquals("X-Custom-Header-1, X-Custom-Header-2", header.value());
	}

	@Test
	public void shouldGenerateAccessControlAllowMethodsHeaderFromSingleHttpMethod() {
		Header header = Header.accessControlAllowMethods("GET");

		assertEquals("Access-Control-Allow-Methods", header.name());
		assertEquals("GET", header.value());
	}

	@Test
	public void shouldGenerateAccessControlAllowMethodsHeaderFromMultipleHttpMethods() {
		Header header = Header.accessControlAllowMethods("GET", "POST");

		assertEquals("Access-Control-Allow-Methods", header.name());
		assertEquals("GET, POST", header.value());
	}

	@Test
	public void shouldGenerateAccessControlAllowOriginHeaderFromSingleOrigin() {
		Header header = Header.accessControlAllowOrigin("*");

		assertEquals("Access-Control-Allow-Origin", header.name());
		assertEquals("*", header.value());
	}

	@Test
	public void shouldGenerateAccessControlAllowOriginHeaderFromMultipleOrigins() {
		Header header = Header.accessControlAllowOrigin("http://my.domain.com.br", "http://my.other.domain.com.br");

		assertEquals("Access-Control-Allow-Origin", header.name());
		assertEquals("http://my.domain.com.br, http://my.other.domain.com.br", header.value());
	}

	@Test
	public void shouldGenerateAccessControlAllowOriginHeaderFromSingleUrlOrigin() throws MalformedURLException {
		Header header = Header.accessControlAllowOrigin(new URL("http://my.domain.com.br"));

		assertEquals("Access-Control-Allow-Origin", header.name());
		assertEquals("http://my.domain.com.br", header.value());
	}

	@Test
	public void shouldGenerateAccessControlAllowOriginHeaderFromMultipleUrlOrigins() throws MalformedURLException {
		Header header = Header.accessControlAllowOrigin(new URL("http://my.domain.com.br"),
				new URL("http://my.other.domain.com.br"));

		assertEquals("Access-Control-Allow-Origin", header.name());
		assertEquals("http://my.domain.com.br, http://my.other.domain.com.br", header.value());
	}

	@Test
	public void shouldGenerateAccessControlAllowOriginHeaderFromSingleUriOrigin() {
		Header header = Header.accessControlAllowOrigin(URI.create("http://my.domain.com.br"));

		assertEquals("Access-Control-Allow-Origin", header.name());
		assertEquals("http://my.domain.com.br", header.value());
	}

	@Test
	public void shouldGenerateAccessControlAllowOriginHeaderFromMultipleUriOrigins() {
		Header header = Header.accessControlAllowOrigin(URI.create("http://my.domain.com.br"),
				URI.create("http://my.other.domain.com.br"));

		assertEquals("Access-Control-Allow-Origin", header.name());
		assertEquals("http://my.domain.com.br, http://my.other.domain.com.br", header.value());
	}

	@Test
	public void shouldGenerateAccessControlExposeHeadersHeaderFromSingleHeaderName() {
		Header header = Header.accessControlExposeHeaders("X-Custom-Header");

		assertEquals("Access-Control-Expose-Headers", header.name());
		assertEquals("X-Custom-Header", header.value());
	}

	@Test
	public void shouldGenerateAccessControlExposeHeadersHeaderFromMultipleHeaderNames() {
		Header header = Header.accessControlExposeHeaders("X-Custom-Header-1", "X-Custom-Header-2");

		assertEquals("Access-Control-Expose-Headers", header.name());
		assertEquals("X-Custom-Header-1, X-Custom-Header-2", header.value());
	}

	@Test
	public void shouldGenerateAccessControlExposeHeadersHeaderFromMultipleHeaderObjects() {
		Header customHeader1 = new Header("X-Custom-Header-1", "value1");
		Header customHeader2 = new Header("X-Custom-Header-2", "value2");

		Header header = Header.accessControlExposeHeaders(customHeader1, customHeader2);

		assertEquals("Access-Control-Expose-Headers", header.name());
		assertEquals("X-Custom-Header-1, X-Custom-Header-2", header.value());
	}

	@Test
	public void shouldGenerateAccessControlExposeHeadersHeaderFromHeadersObject() {
		Headers headers = new Headers(new Header("X-Custom-Header-1", "value1"),
				new Header("X-Custom-Header-2", "value2"));

		Header header = Header.accessControlExposeHeaders(headers);

		assertEquals("Access-Control-Expose-Headers", header.name());
		assertEquals("X-Custom-Header-1, X-Custom-Header-2", header.value());
	}

	@Test
	public void shouldGenerateAccessControlMaxAgeHeaderFromSeconds() {
		Header header = Header.accessControlMaxAge(3600);

		assertEquals("Access-Control-Max-Age", header.name());
		assertEquals("3600", header.value());
	}

	@Test
	public void shouldGenerateAccessControlMaxAgeHeaderFromDurationSeconds() {
		Header header = Header.accessControlMaxAge(Duration.ofHours(1));

		assertEquals("Access-Control-Max-Age", header.name());
		assertEquals("3600", header.value());
	}

	@Test
	public void shouldGenerateAccessControlRequestHeadersHeaderFromSingleHeaderName() {
		Header header = Header.accessControlRequestHeaders("X-Custom-Header");

		assertEquals("Access-Control-Request-Headers", header.name());
		assertEquals("X-Custom-Header", header.value());
	}

	@Test
	public void shouldGenerateAccessControlRequestHeadersHeaderFromMultipleHeaderNames() {
		Header header = Header.accessControlRequestHeaders("X-Custom-Header-1", "X-Custom-Header-2");

		assertEquals("Access-Control-Request-Headers", header.name());
		assertEquals("X-Custom-Header-1, X-Custom-Header-2", header.value());
	}

	@Test
	public void shouldGenerateAccessControlRequestHeadersHeaderFromMultipleHeaderObjects() {
		Header customHeader1 = new Header("X-Custom-Header-1", "value1");
		Header customHeader2 = new Header("X-Custom-Header-2", "value2");

		Header header = Header.accessControlRequestHeaders(customHeader1, customHeader2);

		assertEquals("Access-Control-Request-Headers", header.name());
		assertEquals("X-Custom-Header-1, X-Custom-Header-2", header.value());
	}

	@Test
	public void shouldGenerateAccessControlRequestMethodHeaderFromSingleHttpMethod() {
		Header header = Header.accessControlRequestMethod("GET");

		assertEquals("Access-Control-Request-Method", header.name());
		assertEquals("GET", header.value());
	}

	@Test
	public void shouldGenerateAccessControlRequestMethodHeaderFromMultipleHttpMethods() {
		Header header = Header.accessControlRequestMethod("GET", "POST");

		assertEquals("Access-Control-Request-Method", header.name());
		assertEquals("GET, POST", header.value());
	}

	@Test
	public void shouldGenerateAgeHeaderFromSeconds() {
		Header header = Header.age(3600);

		assertEquals("Age", header.name());
		assertEquals("3600", header.value());
	}

	@Test
	public void shouldGenerateAgeHeaderFromDurationSeconds() {
		Header header = Header.age(Duration.ofHours(1));

		assertEquals("Age", header.name());
		assertEquals("3600", header.value());
	}

	@Test
	public void shouldGenerateAllowHeaderFromSingleHttpMethod() {
		Header header = Header.allow("GET");

		assertEquals("Allow", header.name());
		assertEquals("GET", header.value());
	}

	@Test
	public void shouldGenerateAllowHeaderFromMultipleHttpMethods() {
		Header header = Header.allow("GET", "POST");

		assertEquals("Allow", header.name());
		assertEquals("GET, POST", header.value());
	}

	@Test
	public void shouldGenerateAuthorizationHeaderFromCredentials() {
		Header header = Header.authorization("aaa111");

		assertEquals("Authorization", header.name());
		assertEquals("aaa111", header.value());
	}

	@Test
	public void shouldGenerateAuthorizationHeaderFromTypeAndCredentials() {
		Header header = Header.authorization("Basic", "aaa111");

		assertEquals("Authorization", header.name());
		assertEquals("Basic aaa111", header.value());
	}

	@Test
	public void shouldGenerateCacheControlHeaderFromSingleValue() {
		Header header = Header.cacheControl("no-cache");

		assertEquals("Cache-Control", header.name());
		assertEquals("no-cache", header.value());
	}

	@Test
	public void shouldGenerateCacheControlHeaderFromMultipleValues() {
		Header header = Header.cacheControl("no-cache", "no-store");

		assertEquals("Cache-Control", header.name());
		assertEquals("no-cache, no-store", header.value());
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
	public void shouldGenerateContentRangeHeaderFromDirectivesWithoutUnit() {
		Header header = Header.contentRange(200, 1000, 10000);

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
	public void shouldGenerateDoNotTrackHeaderFromEnableTrack() {
		Header header = Header.doNotTrack(false);

		assertEquals("DNT", header.name());
		assertEquals("0", header.value());
	}

	@Test
	public void shouldGenerateDoNotTrackHeaderFromDisableTrack() {
		Header header = Header.doNotTrack(true);

		assertEquals("DNT", header.name());
		assertEquals("1", header.value());
	}
	
	@Test
	public void shouldGenerateETagHeaderFromTag() {
		Header header = Header.eTag("abc1234");

		assertEquals("ETag", header.name());
		assertEquals("\"abc1234\"", header.value());
	}

	@Test
	public void shouldGenerateETagHeaderFromWeakTag() {
		Header header = Header.eTag(ETag.weak("33a64df551425fcc55e4d42a148795d9f25f89d4"));

		assertEquals("ETag", header.name());
		assertEquals("W/\"33a64df551425fcc55e4d42a148795d9f25f89d4\"", header.value());
	}
	
	@Test
	public void shouldGenerateExpectHeader() {
		Header header = Header.expect();

		assertEquals("Expect", header.name());
		assertEquals("100-Continue", header.value());
	}
	
	@Test
	public void shouldGenerateExpiresHeaderFromTimestamp() {
		LocalDateTime datetime = LocalDateTime.of(2014, 02, 03, 9, 00);

		Header header = Header.expires(datetime.toInstant(ZoneOffset.UTC).toEpochMilli());

		assertEquals("Expires", header.name());
		assertEquals("Mon, 03 Feb 2014 09:00:00 GMT", header.value());
	}

	@Test
	public void shouldGenerateExpiresHeaderFromDate() {
		LocalDateTime datetime = LocalDateTime.of(2014, 02, 03, 9, 00);

		Header header = Header.expires(Date.from(datetime.toInstant(ZoneOffset.UTC)));

		assertEquals("Expires", header.name());
		assertEquals("Mon, 03 Feb 2014 09:00:00 GMT", header.value());
	}

	@Test
	public void shouldGenerateExpiresHeaderFromCalendar() {
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		calendar.set(Calendar.YEAR, 2014);
		calendar.set(Calendar.MONTH, Calendar.FEBRUARY);
		calendar.set(Calendar.DAY_OF_MONTH, 03);
		calendar.set(Calendar.HOUR_OF_DAY, 9);
		calendar.set(Calendar.MINUTE, 00);
		calendar.set(Calendar.SECOND, 00);

		Header header = Header.expires(calendar);

		assertEquals("Expires", header.name());
		assertEquals("Mon, 03 Feb 2014 09:00:00 GMT", header.value());
	}

	@Test
	public void shouldGenerateExpiresHeaderFromLocalDateTime() {
		LocalDateTime datetime = LocalDateTime.of(2014, 02, 03, 9, 00);

		Header header = Header.expires(datetime);

		assertEquals("Expires", header.name());
		assertEquals("Mon, 03 Feb 2014 09:00:00 GMT", header.value());
	}

	@Test
	public void shouldGenerateExpiresHeaderFromZonedDateTime() {
		LocalDateTime datetime = LocalDateTime.of(2014, 02, 03, 9, 00);
		ZonedDateTime zonedDateTime = ZonedDateTime.of(datetime, ZoneId.of("GMT"));

		Header header = Header.expires(zonedDateTime);

		assertEquals("Expires", header.name());
		assertEquals("Mon, 03 Feb 2014 09:00:00 GMT", header.value());
	}

	@Test
	public void shouldGenerateExpiresHeaderFromInstant() {
		LocalDateTime datetime = LocalDateTime.of(2014, 02, 03, 9, 00);

		Header header = Header.expires(datetime.toInstant(ZoneOffset.UTC));

		assertEquals("Expires", header.name());
		assertEquals("Mon, 03 Feb 2014 09:00:00 GMT", header.value());
	}

	@Test
	public void shouldGenerateForwardedHeaderFromIp() {
		Header header = Header.forwarded("127.0.0.0");

		assertEquals("Forwarded", header.name());
		assertEquals("for=127.0.0.0", header.value());
	}

	@Test
	public void shouldGenerateForwardedHeaderFromInetAddressObject() throws UnknownHostException {
		Header header = Header.forwarded(InetAddress.getByName("127.0.0.0"));

		assertEquals("Forwarded", header.name());
		assertEquals("for=127.0.0.0", header.value());
	}

	@Test
	public void shouldGenerateForwardedHeaderFromMultipleInetAddressObjects() throws UnknownHostException {
		Header header = Header.forwarded(InetAddress.getByName("127.0.0.0"), InetAddress.getByName("203.0.113.43"));

		assertEquals("Forwarded", header.name());
		assertEquals("for=127.0.0.0, for=203.0.113.43", header.value());
	}
	
	@Test
	public void shouldGenerateForwardedHeaderFromInetAddressWithByDirective() throws UnknownHostException {
		Header header = Header.forwarded(InetAddress.getByName("127.0.0.0"), "203.0.113.43");

		assertEquals("Forwarded", header.name());
		assertEquals("for=127.0.0.0; by=203.0.113.43", header.value());
	}
	
	@Test
	public void shouldGenerateForwardedHeaderFromInetAddressWithByAndHostDirectives() throws UnknownHostException {
		Header header = Header.forwarded(InetAddress.getByName("127.0.0.0"), "203.0.113.43", "localhost");

		assertEquals("Forwarded", header.name());
		assertEquals("for=127.0.0.0; by=203.0.113.43; host=localhost", header.value());
	}
	
	@Test
	public void shouldGenerateForwardedHeaderFromInetAddressWithByAndHostAndProtoDirectives() throws UnknownHostException {
		Header header = Header.forwarded(InetAddress.getByName("127.0.0.0"), "203.0.113.43", "localhost", "http");

		assertEquals("Forwarded", header.name());
		assertEquals("for=127.0.0.0; by=203.0.113.43; host=localhost; proto=http", header.value());
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
}
