/*******************************************************************************
 *
 * MIT License
 *
 * Copyright (c) 2016 Tiago de Freitas Lima
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 *******************************************************************************/
package com.github.ljtfreitas.restify.http.client;

import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.ljtfreitas.restify.http.contract.ContentType;
import com.github.ljtfreitas.restify.http.contract.Cookie;
import com.github.ljtfreitas.restify.http.contract.Cookies;

public class Header {

	private final String name;
	private final String value;

	public Header(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public String name() {
		return name;
	}

	public String value() {
		return value;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Header) {
			Header that = (Header) obj;

			return this.name.equalsIgnoreCase(that.name)
				&& this.value.equals(that.value);

		} else return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, value);
	}

	@Override
	public String toString() {
		return name + ": " + value;
	}

	public static Header with(String name, String value) {
		return new Header(name, value);
	}

	public static Header with(String name, String... values) {
		return with(name, Arrays.stream(values));
	}

	public static Header with(String name, Collection<String> values) {
		return with(name, values.stream());
	}

	private static Header with(String name, Stream<String> values) {
		return new Header(name, values.collect(Collectors.joining(", ")));
	}

	public static Header accept(String... contentTypes) {
		return with(Headers.ACCEPT, contentTypes);
	}

	public static Header accept(ContentType... contentTypes) {
		return with(Headers.ACCEPT, Arrays.stream(contentTypes).map(ContentType::toString));
	}

	public static Header accept(Collection<ContentType> contentTypes) {
		return with(Headers.ACCEPT, contentTypes.stream().map(ContentType::toString));
	}

	public static Header acceptCharset(String... charsets) {
		return with(Headers.ACCEPT_CHARSET, Arrays.stream(charsets).map(String::toLowerCase));
	}

	public static Header acceptCharset(Charset... charsets) {
		return with(Headers.ACCEPT_CHARSET, Arrays.stream(charsets).map(c -> c.name().toLowerCase()));
	}

	public static Header acceptCharset(Collection<Charset> charsets) {
		return with(Headers.ACCEPT_CHARSET, charsets.stream().map(c -> c.name().toLowerCase()));
	}

	public static Header acceptEncoding(String... algorithms) {
		return with(Headers.ACCEPT_ENCODING, Arrays.stream(algorithms).map(String::toLowerCase));
	}

	public static Header acceptEncoding(Collection<String> algorithms) {
		return with(Headers.ACCEPT_ENCODING, algorithms.stream().map(String::toLowerCase));
	}

	public static Header acceptLanguage(String... languages) {
		return with(Headers.ACCEPT_LANGUAGE, languages);
	}

	public static Header acceptLanguage(Locale... locales) {
		return with(Headers.ACCEPT_LANGUAGE,
				Arrays.stream(locales)
					.map(locale -> locale.getLanguage() + Optional.ofNullable(locale.getCountry())
						.filter(country -> !country.isEmpty())
							.map(country -> "-" + country).orElse("")));
	}

	public static Header acceptRanges(String unit) {
		return with(Headers.ACCEPT_RANGES, unit);
	}

	public static Header acceptVersion(String version) {
		return with(Headers.ACCEPT_VERSION, version);
	}

	public static Header accessControlAllowCredentials(boolean value) {
		return with(Headers.ACCESS_CONTROL_ALLOW_CREDENTIALS, Boolean.toString(value));
	}

	public static Header accessControlAllowHeaders(String... headers) {
		return with(Headers.ACCESS_CONTROL_ALLOW_HEADERS, headers);
	}

	public static Header accessControlAllowHeaders(Header... headers) {
		return with(Headers.ACCESS_CONTROL_ALLOW_HEADERS, Arrays.stream(headers).map(Header::name));
	}

	public static Header accessControlAllowHeaders(Headers headers) {
		return with(Headers.ACCESS_CONTROL_ALLOW_HEADERS, headers.all().stream().map(Header::name));
	}

	public static Header accessControlAllowMethods(String... methods) {
		return with(Headers.ACCESS_CONTROL_ALLOW_METHODS, methods);
	}
	
	public static Header accessControlAllowOrigin(String... origins) {
		return with(Headers.ACCESS_CONTROL_ALLOW_ORIGIN, origins);
	}
	
	public static Header accessControlAllowOrigin(URL... origins) {
		return with(Headers.ACCESS_CONTROL_ALLOW_ORIGIN, Arrays.stream(origins).map(o -> o.toString()));
	}
	
	public static Header accessControlAllowOrigin(URI... origins) {
		return with(Headers.ACCESS_CONTROL_ALLOW_ORIGIN, Arrays.stream(origins).map(o -> o.toString()));
	}
	
	public static Header accessControlExposeHeaders(String... headers) {
		return with(Headers.ACCESS_CONTROL_EXPOSE_HEADERS, headers);
	}

	public static Header accessControlExposeHeaders(Header... headers) {
		return with(Headers.ACCESS_CONTROL_EXPOSE_HEADERS, Arrays.stream(headers).map(Header::name));
	}

	public static Header accessControlExposeHeaders(Headers headers) {
		return with(Headers.ACCESS_CONTROL_EXPOSE_HEADERS, headers.all().stream().map(Header::name));
	}
	
	public static Header accessControlMaxAge(long seconds) {
		return with(Headers.ACCESS_CONTROL_MAX_AGE, Long.toString(seconds));
	}

	public static Header accessControlMaxAge(Duration duration) {
		return with(Headers.ACCESS_CONTROL_MAX_AGE, Long.toString(duration.get(ChronoUnit.SECONDS)));
	}

	public static Header accessControlRequestHeaders(String... headers) {
		return with(Headers.ACCESS_CONTROL_REQUEST_HEADERS, headers);
	}

	public static Header accessControlRequestHeaders(Header... headers) {
		return with(Headers.ACCESS_CONTROL_REQUEST_HEADERS, Arrays.stream(headers).map(Header::name));
	}

	public static Header accessControlRequestHeaders(Headers headers) {
		return with(Headers.ACCESS_CONTROL_REQUEST_HEADERS, headers.all().stream().map(Header::name));
	}

	public static Header accessControlRequestMethod(String... methods) {
		return with(Headers.ACCESS_CONTROL_REQUEST_METHOD, methods);
	}
	
	public static Header age(long seconds) {
		return with(Headers.AGE, Long.toString(seconds));
	}

	public static Header age(Duration duration) {
		return with(Headers.AGE, Long.toString(duration.get(ChronoUnit.SECONDS)));
	}

	public static Header allow(String... methods) {
		return with(Headers.ALLOW, methods);
	}

	public static Header authorization(String credentials) {
		return with(Headers.AUTHORIZATION, credentials);
	}
	
	public static Header authorization(String type, String credentials) {
		return with(Headers.AUTHORIZATION, type + " " + credentials);
	}
	
	public static Header cacheControl(String... values) {
		return with(Headers.CACHE_CONTROL, values);
	}

	public static Header connection(String... values) {
		return with(Headers.CONNECTION, values);
	}
	
	public static Header contentDisposition(String value) {
		return with(Headers.CONTENT_DISPOSITION, value);
	}

	public static Header contentEncoding(String algorithm) {
		return with(Headers.CONTENT_ENCODING, algorithm);
	}

	public static Header contentLanguage(String language) {
		return with(Headers.CONTENT_LANGUAGE, language);
	}

	public static Header contentLanguage(Locale locale) {
		return with(Headers.CONTENT_LANGUAGE,
				locale.getLanguage() + Optional.ofNullable(locale.getCountry())
					.filter(country -> !country.isEmpty())
						.map(country -> "-" + country)
							.orElse(""));
	}

	public static Header contentLength(long length) {
		return with(Headers.CONTENT_LENGTH, Long.toString(length));
	}

	public static Header contentLocation(String location) {
		return with(Headers.CONTENT_LOCATION, location);
	}

	public static Header contentLocation(URL location) {
		return with(Headers.CONTENT_LOCATION, location.toString());
	}

	public static Header contentLocation(URI location) {
		return with(Headers.CONTENT_LOCATION, location.toString());
	}

	public static Header contentRange(String unit, long start, long end, long size) {
		return with(Headers.CONTENT_RANGE, unit + " " + start + "-" + end + "/" + size);
	}

	public static Header contentRange(long start, long end, long size) {
		return contentRange("bytes", start, end, size);
	}

	public static Header contentType(String contentType) {
		return with(Headers.CONTENT_TYPE, contentType);
	}

	public static Header contentType(ContentType contentType) {
		return with(Headers.CONTENT_TYPE, contentType.toString());
	}

	public static Header cookie(String... cookies) {
		return cookie(new Cookies(cookies));
	}

	public static Header cookie(Cookie... cookies) {
		return cookie(new Cookies(cookies));
	}

	public static Header cookie(Cookies cookies) {
		return with(Headers.COOKIE, cookies.toString());
	}

	public static Header date(long timestamp) {
		return with(Headers.DATE, DateTimeHeaderFormatter.format(timestamp));
	}

	public static Header date(Date date) {
		return with(Headers.DATE, DateTimeHeaderFormatter.format(date));
	}

	public static Header date(Calendar calendar) {
		return with(Headers.DATE, DateTimeHeaderFormatter.format(calendar));
	}

	public static Header date(LocalDateTime dateTime) {
		return with(Headers.DATE, DateTimeHeaderFormatter.format(dateTime));
	}

	public static Header date(Instant instant) {
		return with(Headers.DATE, DateTimeHeaderFormatter.format(instant));
	}

	public static Header date(ZonedDateTime zonedDateTime) {
		return with(Headers.DATE, DateTimeHeaderFormatter.format(zonedDateTime));
	}

	public static Header doNotTrack(boolean value) {
		return with(Headers.DO_NOT_TRACK, value ? "1" : "0");
	}
	
	public static Header eTag(String eTag) {
		return eTag(ETag.of(eTag));
	}

	public static Header eTag(ETag eTag) {
		return with(Headers.ETAG, eTag.toString());
	}

	public static Header expect() {
		return with(Headers.EXPECT, "100-Continue");
	}

	public static Header expires(long timestamp) {
		return with(Headers.EXPIRES, DateTimeHeaderFormatter.format(timestamp));
	}

	public static Header expires(Date date) {
		return with(Headers.EXPIRES, DateTimeHeaderFormatter.format(date));
	}

	public static Header expires(Calendar calendar) {
		return with(Headers.EXPIRES, DateTimeHeaderFormatter.format(calendar));
	}

	public static Header expires(LocalDateTime dateTime) {
		return with(Headers.EXPIRES, DateTimeHeaderFormatter.format(dateTime));
	}

	public static Header expires(Instant instant) {
		return with(Headers.EXPIRES, DateTimeHeaderFormatter.format(instant));
	}

	public static Header expires(ZonedDateTime zonedDateTime) {
		return with(Headers.EXPIRES, DateTimeHeaderFormatter.format(zonedDateTime));
	}

	public static Header forwarded(InetAddress... addresses) {
		return with(Headers.FORWARDED, Arrays.stream(addresses).map(InetAddress::getHostAddress).map(ip -> "for=" + ip));
	}
	
	public static Header forwarded(InetAddress client) {
		return forwarded(client.getHostAddress());
	}
	
	public static Header forwarded(String client) {
		return forwarded(client, null, null, null);
	}
	
	public static Header forwarded(InetAddress client, String by) {
		return forwarded(client.getHostAddress(), by);
	}
	
	public static Header forwarded(String client, String by) {
		return forwarded(client, by, null, null);
	}

	public static Header forwarded(InetAddress client, String by, String host) {
		return forwarded(client.getHostAddress(), by, host);
	}
	
	public static Header forwarded(String client, String by, String host) {
		return forwarded(client, by, host, null);
	}

	public static Header forwarded(InetAddress client, String by, String host, String proto) {
		return forwarded(client.getHostAddress(), by, host, proto);
	}
	
	public static Header forwarded(String client, String by, String host, String proto) {
		StringBuilder forwarded = new StringBuilder().append("for=").append(client);

		if (by != null) forwarded.append("; ").append("by=").append(by);
		if (host != null) forwarded.append("; ").append("host=").append(host);
		if (proto != null) forwarded.append("; ").append("proto=").append(proto);

		return with(Headers.FORWARDED, forwarded.toString());
	}
	
	public static Header from(String email) {
		return with(Headers.FROM, email);
	}

	public static Header host(String host) {
		return with(Headers.HOST, host);
	}
	
	public static Header host(URL host) {
		return with(Headers.HOST, host.getHost() + (host.getPort() == -1 ? "" : ":" + host.getPort()));
	}

	public static Header host(URI host) {
		return with(Headers.HOST, host.getHost() + (host.getPort() == -1 ? "" : ":" + host.getPort()));
	}

	public static Header ifMatch(String... eTags) {
		return with(Headers.IF_MATCH, Arrays.stream(eTags).map(ETag::of).map(ETag::toString));
	}

	public static Header ifMatch(ETag... eTags) {
		return with(Headers.IF_MATCH, Arrays.stream(eTags).map(ETag::toString));
	}
	
	private static class DateTimeHeaderFormatter {

		private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz",
				Locale.US);
		private static final ZoneId GMT = ZoneId.of("GMT");

		private static String format(long timestamp) {
			return format(Instant.ofEpochMilli(timestamp));
		}

		private static String format(Date date) {
			return format(date.toInstant());
		}

		private static String format(Calendar calendar) {
			return format(calendar.getTime());
		}

		private static String format(LocalDateTime dateTime) {
			return format(dateTime.toInstant(ZoneOffset.UTC));
		}

		private static String format(Instant instant) {
			return format(ZonedDateTime.ofInstant(instant, GMT));
		}

		private static String format(ZonedDateTime zonedDateTime) {
			return zonedDateTime.format(FORMATTER);
		}
	}
}
