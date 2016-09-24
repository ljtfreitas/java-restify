package com.restify.http.client.message.form.multipart;

import com.restify.http.client.request.HttpRequestMessage;

interface MultipartFieldSerializer<T> {

	public void write(String boundary, MultipartField<T> field, HttpRequestMessage httpRequestMessage);

	public boolean supports(Class<?> type);

}
