package com.restify.sample.client;

import java.util.Optional;
import java.util.function.BiConsumer;

import com.restify.http.client.Headers;
import com.restify.http.client.call.EndpointCall;
import com.restify.http.client.request.async.EndpointCallCallback;
import com.restify.http.client.request.async.EndpointCallSuccessCallback;
import com.restify.http.client.response.EndpointResponse;
import com.restify.http.contract.BodyParameter;
import com.restify.http.contract.CallbackParameter;
import com.restify.http.contract.Delete;
import com.restify.http.contract.Get;
import com.restify.http.contract.Head;
import com.restify.http.contract.Header;
import com.restify.http.contract.MultipartParameters;
import com.restify.http.contract.Options;
import com.restify.http.contract.Path;
import com.restify.http.contract.PathParameter;
import com.restify.http.contract.Post;
import com.restify.http.contract.Put;
import com.restify.sample.api.MyApiResponse;

@Path("/api")
public interface MyApi {

	@Path("/{type}")
	@Get
	public Optional<MyApiResponse> getAs(@PathParameter String type);

	@Path("/{type}")
	@Post
	public MyApiResponse postAs(@PathParameter String type);

	@Path("/{type}")
	@Put
	public MyApiResponse putAs(@PathParameter String type);

	@Path("/{type}")
	@Delete
	public MyApiResponse deleteAs(@PathParameter String type);

	@Path("/{type}")
	@Head
	public Headers headAs(@PathParameter String type);

	@Path("/{type}")
	@Options
	public Headers optionsAs(@PathParameter String type);

	@Path("/upload")
	@Post
	@Header(name = "Content-Type", value = "multipart/form-data")
	public String upload(@BodyParameter MultipartParameters parameters);

	@Path("/{type}")
	@Get
	public EndpointResponse<MyApiResponse> getResponseObjectAs(@PathParameter String type);

	@Path("/{type}")
	@Get
	public EndpointCall<MyApiResponse> call(@PathParameter String type);

	@Path("/resource-not-found")
	@Get
	public Optional<String> optional();

	@Path("/{type}")
	@Get
	public void async(@PathParameter String type, @CallbackParameter EndpointCallCallback<MyApiResponse> callback);

	@Path("/{type}")
	@Get
	public void asyncWithConsumerCallback(@PathParameter String type, @CallbackParameter BiConsumer<MyApiResponse, Throwable> callback);

	@Path("/{type}")
	@Get
	public void optionalAsync(@PathParameter String type, @CallbackParameter EndpointCallSuccessCallback<Optional<MyApiResponse>> callback);
}
