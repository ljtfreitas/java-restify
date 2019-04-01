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
package com.github.ljtfreitas.restify.spring.configure;

import java.io.Serializable;

import javax.json.JsonReaderFactory;
import javax.json.JsonStructure;
import javax.json.JsonWriterFactory;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbConfig;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ljtfreitas.restify.http.client.message.converter.form.FormURLEncodedFormObjectMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.form.FormURLEncodedMapMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.form.FormURLEncodedParametersMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.form.multipart.MultipartFormFileObjectMessageWriter;
import com.github.ljtfreitas.restify.http.client.message.converter.form.multipart.MultipartFormMapMessageWriter;
import com.github.ljtfreitas.restify.http.client.message.converter.form.multipart.MultipartFormObjectMessageWriter;
import com.github.ljtfreitas.restify.http.client.message.converter.form.multipart.MultipartFormParametersMessageWriter;
import com.github.ljtfreitas.restify.http.client.message.converter.json.GsonMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.json.JacksonMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.json.JsonBMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.json.JsonMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.json.JsonPMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.octet.OctetByteArrayMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.octet.OctetInputStreamMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.octet.OctetSerializableMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.text.ScalarMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.text.TextHtmlMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.text.TextPlainMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.xml.JaxBXmlMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.xml.XmlMessageConverter;
import com.google.gson.Gson;

@Configuration
public class RestifyHttpMessageConvertersConfiguration {

	@Configuration
	static class JsonHttpMessageConverterConfiguration {

		@Configuration
		@ConditionalOnClass(JacksonMessageConverter.class)
		static class JacksonHttpMesssageConverterConfiguration {

			@ConditionalOnMissingBean
			@Bean
			public JsonMessageConverter<Object> jacksonMessageConverter(ObjectProvider<ObjectMapper> objectMapperProvider) {
				ObjectMapper objectMapper = objectMapperProvider.getIfAvailable();
				return objectMapper == null ? new JacksonMessageConverter<>() : new JacksonMessageConverter<>(objectMapper);
			}
		}

		@Configuration
		@ConditionalOnClass(GsonMessageConverter.class)
		static class GsonHttpMesssageConverterConfiguration {

			@ConditionalOnMissingBean
			@Bean
			public JsonMessageConverter<Object> gsonMessageConverter(ObjectProvider<Gson> gsonProvider) {
				Gson gson = gsonProvider.getIfAvailable();
				return gson == null ? new GsonMessageConverter<>() : new GsonMessageConverter<>(gson);
			}
		}

		@Configuration
		@ConditionalOnClass(JsonPMessageConverter.class)
		static class JsonPHttpMesssageConverterConfiguration {

			@ConditionalOnMissingBean
			@Bean
			public JsonMessageConverter<JsonStructure> jsonPMessageConverter(ObjectProvider<JsonReaderFactory> jsonReaderFactoryProvider, ObjectProvider<JsonWriterFactory> jsonWriterFactoryProvider) {
				JsonReaderFactory jsonReaderFactory = jsonReaderFactoryProvider.getIfAvailable();
				JsonWriterFactory jsonWriterFactory = jsonWriterFactoryProvider.getIfAvailable();

				if (jsonReaderFactory == null && jsonWriterFactory == null) {
					return new JsonPMessageConverter();

				} else if (jsonReaderFactory != null && jsonWriterFactory != null) {
					return new JsonPMessageConverter(jsonReaderFactory, jsonWriterFactory);

				} else if (jsonReaderFactory != null && jsonWriterFactory == null) {
					return new JsonPMessageConverter(jsonReaderFactory);

				} else if (jsonReaderFactory == null && jsonWriterFactory != null) {
					return new JsonPMessageConverter(jsonWriterFactory);

				} else {
					return new JsonPMessageConverter();
				}
			}
		}

		@Configuration
		@ConditionalOnClass(JsonBMessageConverter.class)
		static class JsonBHttpMesssageConverterConfiguration {

			@ConditionalOnMissingBean
			@Bean
			public JsonMessageConverter<Object> jsonBMessageConverter(ObjectProvider<Jsonb> jsonbProvider, ObjectProvider<JsonbConfig> jsonbConfigProvider) {
				Jsonb jsonb = jsonbProvider.getIfAvailable();
				JsonbConfig jsonbConfig = jsonbConfigProvider.getIfAvailable();

				if (jsonb != null) {
					return new JsonBMessageConverter<>(jsonb);

				} else if (jsonbConfig != null) {
					return new JsonBMessageConverter<>(jsonbConfig);

				} else {
					return new JsonBMessageConverter<>();

				}
			}
		}
	}

	@Configuration
	static class XmlHttpMessageConverterConfiguration {

		@Configuration
		@ConditionalOnClass(JaxBXmlMessageConverter.class)
		static class JaxBHttpMesssageConverterConfiguration {

			@ConditionalOnMissingBean
			@Bean
			public XmlMessageConverter<Object> jaxbMessageConverter() {
				return new JaxBXmlMessageConverter<>();
			}
		}
	}

	@Configuration
	static class FormUrlEncodedHttpMessageConverterConfiguration {

		@Configuration
		@ConditionalOnClass(FormURLEncodedFormObjectMessageConverter.class)
		static class FormURLEncodedFormObjectHttpMesssageConverterConfiguration {

			@ConditionalOnMissingBean
			@Bean
			public FormURLEncodedFormObjectMessageConverter formURLEncodedFormObjectMessageConverter() {
				return new FormURLEncodedFormObjectMessageConverter();
			}
		}

		@Configuration
		@ConditionalOnClass(FormURLEncodedMapMessageConverter.class)
		static class FormURLEncodedMapHttpMesssageConverterConfiguration {

			@ConditionalOnMissingBean
			@Bean
			public FormURLEncodedMapMessageConverter formURLEncodedMapMessageConverter() {
				return new FormURLEncodedMapMessageConverter();
			}
		}

		@Configuration
		@ConditionalOnClass(FormURLEncodedParametersMessageConverter.class)
		static class FormURLEncodedParametersHttpMesssageConverterConfiguration {

			@ConditionalOnMissingBean
			@Bean
			public FormURLEncodedParametersMessageConverter formURLEncodedParametersMessageConverter() {
				return new FormURLEncodedParametersMessageConverter();
			}
		}
	}

	@Configuration
	static class MultipartFormHttpMessageConverterConfiguration {

		@Configuration
		@ConditionalOnClass(MultipartFormFileObjectMessageWriter.class)
		static class MultipartFormFileObjectHttpMesssageConverterConfiguration {

			@ConditionalOnMissingBean
			@Bean
			public MultipartFormFileObjectMessageWriter multipartFormFileObjectMessageWriter() {
				return new MultipartFormFileObjectMessageWriter();
			}
		}

		@Configuration
		@ConditionalOnClass(MultipartFormMapMessageWriter.class)
		static class MultipartFormMapHttpMesssageConverterConfiguration {

			@ConditionalOnMissingBean
			@Bean
			public MultipartFormMapMessageWriter multipartFormMapMessageWriter() {
				return new MultipartFormMapMessageWriter();
			}
		}

		@Configuration
		@ConditionalOnClass(MultipartFormObjectMessageWriter.class)
		static class MultipartFormObjectHttpMesssageConverterConfiguration {

			@ConditionalOnMissingBean
			@Bean
			public MultipartFormObjectMessageWriter multipartFormObjectMessageWriter() {
				return new MultipartFormObjectMessageWriter();
			}
		}

		@Configuration
		@ConditionalOnClass(MultipartFormParametersMessageWriter.class)
		static class MultipartFormParametersHttpMesssageConverterConfiguration {

			@ConditionalOnMissingBean
			@Bean
			public MultipartFormParametersMessageWriter multipartFormParametersMessageWriter() {
				return new MultipartFormParametersMessageWriter();
			}
		}
	}

	@Configuration
	static class TextHttpMessageConverterConfiguration {

		@Configuration
		@ConditionalOnClass(ScalarMessageConverter.class)
		static class ScalarHttpMesssageConverterConfiguration {

			@ConditionalOnMissingBean
			@Bean
			public ScalarMessageConverter scalarMessageConverter() {
				return new ScalarMessageConverter();
			}
		}

		@Configuration
		@ConditionalOnClass(TextHtmlMessageConverter.class)
		static class TextHtmlHttpMesssageConverterConfiguration {

			@ConditionalOnMissingBean
			@Bean
			public TextHtmlMessageConverter textHtmlMessageConverter() {
				return new TextHtmlMessageConverter();
			}
		}

		@Configuration
		@ConditionalOnClass(TextPlainMessageConverter.class)
		static class TextPlainHttpMesssageConverterConfiguration {

			@ConditionalOnMissingBean
			@Bean
			public TextPlainMessageConverter textPlainMessageConverter() {
				return new TextPlainMessageConverter();
			}
		}
	}

	@Configuration
	static class OctetStreamHttpMessageConverterConfiguration {

		@Configuration
		@ConditionalOnClass(OctetByteArrayMessageConverter.class)
		static class OctetStreamHttpMesssageConverterConfiguration {

			@ConditionalOnMissingBean
			@Bean
			public OctetByteArrayMessageConverter octetByteArrayMessageConverter() {
				return new OctetByteArrayMessageConverter();
			}
		}

		@Configuration
		@ConditionalOnClass(OctetInputStreamMessageConverter.class)
		static class OctetInputHttpMesssageConverterConfiguration {

			@ConditionalOnMissingBean
			@Bean
			public OctetInputStreamMessageConverter octetInputStreamMessageConverter() {
				return new OctetInputStreamMessageConverter();
			}
		}

		@Configuration
		@ConditionalOnClass(OctetSerializableMessageConverter.class)
		static class OctetSerializableHttpMesssageConverterConfiguration {

			@ConditionalOnMissingBean
			@Bean
			public OctetSerializableMessageConverter<Serializable> octetSerializableMessageConverter() {
				return new OctetSerializableMessageConverter<>();
			}
		}
	}
}