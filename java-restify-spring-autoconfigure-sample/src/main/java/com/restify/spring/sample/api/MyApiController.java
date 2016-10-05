package com.restify.spring.sample.api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class MyApiController {

	@GetMapping("/{id}")
	public MyApiResponse get(@PathVariable String id) {
		return new MyApiResponse("GET", "My Api GET response. Id: " + id);
	}

	@PostMapping
	public MyApiResponse post(@RequestParam String message) {
		return new MyApiResponse("POST", "My Api POST response. Message: " + message);
	}

	@PutMapping
	public MyApiResponse put(@RequestParam String message) {
		return new MyApiResponse("PUT", "My Api PUT response. Message: " + message);
	}

	@DeleteMapping("/{id}")
	public MyApiResponse delete(@PathVariable String id) {
		return new MyApiResponse("DELETE", "My Api DELETE response. Id: " + id);
	}

	@RequestMapping(method = RequestMethod.HEAD)
	public HttpHeaders head() {
		HttpHeaders headers = new HttpHeaders();
		headers.setDate("X-MyJsonApi-Timestamp", Instant.now().toEpochMilli());

		return headers;
	}

	@PostMapping(path = "/upload", produces = "text/plain")
	public String upload(@RequestParam String destination, @RequestParam MultipartFile file) throws IOException {
		Path newFile = Files.createFile(Paths.get(destination, file.getOriginalFilename()));

		Files.copy(file.getInputStream(), newFile, StandardCopyOption.REPLACE_EXISTING);

		return newFile.toString();
	}
}
