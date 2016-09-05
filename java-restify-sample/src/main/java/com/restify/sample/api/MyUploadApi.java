package com.restify.sample.api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

@MultipartConfig(location = "/tmp/java-restify-api/", fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024
		* 2, maxRequestSize = (1024 * 1024 * 2) * 5)
@WebServlet("/api/upload")
public class MyUploadApi extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String destination = req.getParameter("destination");

		Part file = req.getPart("file");

		Path newFile = Files.createFile(Paths.get(destination, file.getSubmittedFileName()));

		Files.copy(file.getInputStream(), newFile, StandardCopyOption.REPLACE_EXISTING);

		resp.setHeader("Content-Type", "text/plain");
		resp.getWriter().write(newFile.toString());
	}
}
