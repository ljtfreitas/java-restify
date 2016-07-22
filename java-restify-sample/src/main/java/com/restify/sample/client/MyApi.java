package com.restify.sample.client;

import com.restify.http.contract.Delete;
import com.restify.http.contract.Get;
import com.restify.http.contract.Path;
import com.restify.http.contract.Post;
import com.restify.http.contract.Put;
import com.restify.sample.api.MyApiResponse;

@Path("/api")
public interface MyApi {

	@Path("/json") @Get
	public MyApiResponse getAsJson();

	@Path("/json") @Post
	public MyApiResponse postAsJson();

	@Path("/json") @Put
	public MyApiResponse putAsJson();

	@Path("/json") @Delete
	public MyApiResponse deleteAsJson();

	@Path("/xml") @Get
	public MyApiResponse getAsXml();

	@Path("/xml") @Post
	public MyApiResponse postAsXml();

	@Path("/xml") @Put
	public MyApiResponse putAsXml();

	@Path("/xml") @Delete
	public MyApiResponse deleteAsXml();

}
