package com.restify.spring.sample.client;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.restify.http.client.Headers;
import com.restify.http.spring.contract.FormParameters;
import com.restify.spring.sample.api.MyApiResponse;

@RequestMapping("/api")
public interface MyApi {

	@GetMapping("/{id}")
	public MyApiResponse get(@PathVariable String id);

	@GetMapping("/api/{id}")
	public ListenableFuture<MyApiResponse> getAsync(@PathVariable String id);

	@PostMapping(produces = "application/x-www-form-urlencoded")
	public MyApiResponse post(@RequestBody FormParameters parameters);

	@PutMapping(produces = "application/x-www-form-urlencoded")
	public MyApiResponse put(@RequestBody FormParameters parameters);

	@DeleteMapping("/{id}")
	public MyApiResponse delete(@PathVariable String id);

	@RequestMapping(method = RequestMethod.HEAD)
	public Headers head();

	@RequestMapping(method = RequestMethod.OPTIONS)
	public HttpHeaders options();

	@PostMapping(path = "/upload", produces = "multipart/form-data")
	public String upload(@RequestBody FormParameters parameters);

	@GetMapping("/{id}")
	public ResponseEntity<MyApiResponse> getResponseObject(@PathVariable String id);
}
