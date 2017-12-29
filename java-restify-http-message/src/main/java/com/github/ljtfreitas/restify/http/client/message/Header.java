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
package com.github.ljtfreitas.restify.http.client.message;

import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.ljtfreitas.restify.util.Tryable;

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

	public static Header of(String name, String value) {
		return new Header(name, value);
	}

	public static Header of(String name, String... values) {
		return of(name, Arrays.stream(values));
	}

	public static Header of(String name, Collection<String> values) {
		return of(name, values.stream());
	}

	private static Header of(String name, Stream<String> values) {
		return new Header(name, values.collect(Collectors.joining(", ")));
	}

	public static Header accept(String... contentTypes) {
		return of(Headers.ACCEPT, contentTypes);
	}

	public static Header accept(ContentType... contentTypes) {
		return of(Headers.ACCEPT, Arrays.stream(contentTypes).map(ContentType::toString));
	}
	
	public static Header accept(Collection<ContentType> contentTypes) {
		return of(Headers.ACCEPT, contentTypes.stream().map(ContentType::toString));
	}
	
	public static Header acceptCharset(String... charsets) {
		return of(Headers.ACCEPT_CHARSET, Arrays.stream(charsets).map(String::toLowerCase));
	}

	public static Header acceptCharset(Charset... charsets) {
		return of(Headers.ACCEPT_CHARSET, Arrays.stream(charsets).map(c -> c.name().toLowerCase()));
	}

	public static Header acceptCharset(Collection<Charset> charsets) {
		return of(Headers.ACCEPT_CHARSET, charsets.stream().map(c -> c.name().toLowerCase()));
	}

	public static Header acceptEncoding(String... algorithms) {
		return of(Headers.ACCEPT_ENCODING, Arrays.stream(algorithms).map(String::toLowerCase));
	}

	public static Header acceptEncoding(Collection<String> algorithms) {
		return of(Headers.ACCEPT_ENCODING, algorithms.stream().map(String::toLowerCase));
	}

	public static Header acceptLanguage(String... languages) {
		return of(Headers.ACCEPT_LANGUAGE, languages);
	}

	public static Header acceptLanguage(Locale... locales) {
		return of(Headers.ACCEPT_LANGUAGE,
				Arrays.stream(locales)
					.map(locale -> locale.getLanguage() + Optional.ofNullable(locale.getCountry())
						.filter(country -> !country.isEmpty())
							.map(country -> "-" + country).orElse("")));
	}

	public static Header acceptVersion(String version) {
		return of(Headers.ACCEPT_VERSION, version);
	}

	public static Header authorization(String content) {
		return of(Headers.AUTHORIZATION, content);
	}

	public static Header connection(String... values) {
		return of(Headers.CONNECTION, values);
	}

	public static Header contentDisposition(String value) {
		return of(Headers.CONTENT_DISPOSITION, value);
	}

	public static Header contentEncoding(String algorithm) {
		return of(Headers.CONTENT_ENCODING, algorithm);
	}

	public static Header contentLanguage(String language) {
		return of(Headers.CONTENT_LANGUAGE, language);
	}

	public static Header contentLanguage(Locale locale) {
		return of(Headers.CONTENT_LANGUAGE,
				locale.getLanguage() + Optional.ofNullable(locale.getCountry())
					.filter(country -> !country.isEmpty())
						.map(country -> "-" + country)
							.orElse(""));
	}

	public static Header contentLength(long length) {
		return of(Headers.CONTENT_LENGTH, Long.toString(length));
	}

	public static Header contentLocation(String location) {
		return of(Headers.CONTENT_LOCATION, location);
	}

	public static Header contentLocation(URL location) {
		return of(Headers.CONTENT_LOCATION, location.toString());
	}

	public static Header contentLocation(URI location) {
		return of(Headers.CONTENT_LOCATION, location.toString());
	}

	public static Header contentRange(String unit, long start, long end, long size) {
		return of(Headers.CONTENT_RANGE, unit + " " + start + "-" + end + "/" + size);
	}

	public static Header contentType(String contentType) {
		return of(Headers.CONTENT_TYPE, contentType);
	}

	public static Header contentType(ContentType contentType) {
		return of(Headers.CONTENT_TYPE, contentType.toString());
	}
	
	public static Header cookie(String... cookies) {
		return cookie(new Cookies(cookies));
	}

	public static Header cookie(Cookie... cookies) {
		return cookie(new Cookies(cookies));
	}

	public static Header cookie(Cookies cookies) {
		return of(Headers.COOKIE, cookies.toString());
	}

	public static Header date(long timestamp) {
		return of(Headers.DATE, DateTimeHeaderFormatter.format(timestamp));
	}

	public static Header date(Date date) {
		return of(Headers.DATE, DateTimeHeaderFormatter.format(date));
	}

	public static Header date(Calendar calendar) {
		return of(Headers.DATE, DateTimeHeaderFormatter.format(calendar));
	}

	public static Header date(LocalDateTime dateTime) {
		return of(Headers.DATE, DateTimeHeaderFormatter.format(dateTime));
	}

	public static Header date(Instant instant) {
		return of(Headers.DATE, DateTimeHeaderFormatter.format(instant));
	}

	public static Header date(ZonedDateTime zonedDateTime) {
		return of(Headers.DATE, DateTimeHeaderFormatter.format(zonedDateTime));
	}

	public static Header expect() {
		return of(Headers.EXPECT, "100-Continue");
	}

	public static Header from(String email) {
		return of(Headers.FROM, email);
	}

	public static Header host(String host) {
		return of(Headers.HOST, host);
	}

	public static Header host(URL host) {
		return of(Headers.HOST, host.getHost() + (host.getPort() == -1 ? "" : ":" + host.getPort()));
	}

	public static Header host(URI host) {
		return of(Headers.HOST, host.getHost() + (host.getPort() == -1 ? "" : ":" + host.getPort()));
	}

	public static Header ifMatch(String... eTags) {
		return of(Headers.IF_MATCH, Arrays.stream(eTags).map(ETag::of).map(ETag::toString));
	}

	public static Header ifMatch(ETag... eTags) {
		return of(Headers.IF_MATCH, Arrays.stream(eTags).map(ETag::toString));
	}

	public static Header ifModifiedSince(long timestamp) {
		return of(Headers.IF_MODIFIED_SINCE, DateTimeHeaderFormatter.format(timestamp));
	}

	public static Header ifModifiedSince(Date date) {
		return of(Headers.IF_MODIFIED_SINCE, DateTimeHeaderFormatter.format(date));
	}

	public static Header ifModifiedSince(Calendar calendar) {
		return of(Headers.IF_MODIFIED_SINCE, DateTimeHeaderFormatter.format(calendar));
	}

	public static Header ifModifiedSince(LocalDateTime dateTime) {
		return of(Headers.IF_MODIFIED_SINCE, DateTimeHeaderFormatter.format(dateTime));
	}

	public static Header ifModifiedSince(Instant instant) {
		return of(Headers.IF_MODIFIED_SINCE, DateTimeHeaderFormatter.format(instant));
	}

	public static Header ifModifiedSince(ZonedDateTime zonedDateTime) {
		return of(Headers.IF_MODIFIED_SINCE, DateTimeHeaderFormatter.format(zonedDateTime));
	}

	public static Header ifNoneMatch(String... eTags) {
		return of(Headers.IF_NONE_MATCH, Arrays.stream(eTags).map(ETag::of).map(ETag::toString));
	}

	public static Header ifNoneMatch(ETag... eTags) {
		return of(Headers.IF_NONE_MATCH, Arrays.stream(eTags).map(ETag::toString));
	}

	public static Header ifRange(long timestamp) {
		return of(Headers.IF_RANGE, DateTimeHeaderFormatter.format(timestamp));
	}

	public static Header ifRange(Date date) {
		return of(Headers.IF_RANGE, DateTimeHeaderFormatter.format(date));
	}

	public static Header ifRange(Calendar calendar) {
		return of(Headers.IF_RANGE, DateTimeHeaderFormatter.format(calendar));
	}

	public static Header ifRange(LocalDateTime dateTime) {
		return of(Headers.IF_RANGE, DateTimeHeaderFormatter.format(dateTime));
	}

	public static Header ifRange(Instant instant) {
		return of(Headers.IF_RANGE, DateTimeHeaderFormatter.format(instant));
	}

	public static Header ifRange(ZonedDateTime zonedDateTime) {
		return of(Headers.IF_RANGE, DateTimeHeaderFormatter.format(zonedDateTime));
	}

	public static Header ifRange(String eTag) {
		return ifRange(ETag.of(eTag));
	}

	public static Header ifRange(ETag eTag) {
		return of(Headers.IF_RANGE, eTag.toString());
	}

	public static Header ifUnmodifiedSince(long timestamp) {
		return of(Headers.IF_UNMODIFIED_SINCE, DateTimeHeaderFormatter.format(timestamp));
	}

	public static Header ifUnmodifiedSince(Date date) {
		return of(Headers.IF_UNMODIFIED_SINCE, DateTimeHeaderFormatter.format(date));
	}

	public static Header ifUnmodifiedSince(Calendar calendar) {
		return of(Headers.IF_UNMODIFIED_SINCE, DateTimeHeaderFormatter.format(calendar));
	}

	public static Header ifUnmodifiedSince(LocalDateTime dateTime) {
		return of(Headers.IF_UNMODIFIED_SINCE, DateTimeHeaderFormatter.format(dateTime));
	}

	public static Header ifUnmodifiedSince(Instant instant) {
		return of(Headers.IF_UNMODIFIED_SINCE, DateTimeHeaderFormatter.format(instant));
	}

	public static Header ifUnmodifiedSince(ZonedDateTime zonedDateTime) {
		return of(Headers.IF_UNMODIFIED_SINCE, DateTimeHeaderFormatter.format(zonedDateTime));
	}

	public static Header lastModified(long timestamp) {
		return of(Headers.LAST_MODIFIED, DateTimeHeaderFormatter.format(timestamp));
	}

	public static Header proxyAuthorization(String content) {
		return of(Headers.PROXY_AUTHORIZATION, content);
	}

	public static Header range(Range... ranges) {
		return range(Arrays.asList(ranges));
	}

	public static Header range(Collection<Range> ranges) {
		return of(Headers.RANGE, Stream.concat(ranges.stream().limit(1).map(Range::toString), ranges.stream().skip(1).map(Range::format)));
	}

	public static Header referer(String referer) {
		return of(Headers.REFERER, referer);
	}

	public static Header referer(URL referer) {
		return referer(Tryable.of(referer::toURI));
	}

	public static Header referer(URI referer) {
		return of(Headers.REFERER, referer.toASCIIString());
	}

	public static Header userAgent(String userAgent) {
		return of(Headers.USER_AGENT, userAgent);
	}
}
