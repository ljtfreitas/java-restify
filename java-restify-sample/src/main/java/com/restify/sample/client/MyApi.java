package com.restify.sample.client;

import com.restify.http.contract.BodyParameter;
import com.restify.http.contract.Delete;
import com.restify.http.contract.Get;
import com.restify.http.contract.Header;
import com.restify.http.contract.Path;
import com.restify.http.contract.PathParameter;
import com.restify.http.contract.Post;
import com.restify.http.contract.Put;
import com.restify.http.metadata.MultipartParameters;
import com.restify.sample.api.MyApiResponse;

@Path("/api")
public interface MyApi {

	@Path("/{type}")
	@Get
	public MyApiResponse getAs(@PathParameter String type);

	@Path("/{type}")
	@Post
	public MyApiResponse postAs(@PathParameter String type);

	@Path("/{type}")
	@Put
	public MyApiResponse putAs(@PathParameter String type);

	@Path("/{type}")
	@Delete
	public MyApiResponse deleteAs(@PathParameter String type);

	@Path("/upload")
	@Post
	@Header(name = "Content-Type", value = "multipart/form-data")
	public String upload(@BodyParameter MultipartParameters parameters);

}
