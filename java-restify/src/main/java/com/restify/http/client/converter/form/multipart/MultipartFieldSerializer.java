package com.restify.http.client.converter.form.multipart;

import com.restify.http.client.HttpRequestMessage;

interface MultipartFieldSerializer<T> {

	public void write(String boundary, MultipartField<T> field, HttpRequestMessage httpRequestMessage);

	public boolean supports(Class<?> type);

}
