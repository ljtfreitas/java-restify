package com.restify.sample.api;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebServlet("/api/json")
public class MyJsonApi extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private ObjectMapper objectMapper;

	@Override
	public void init() throws ServletException {
		super.init();

		objectMapper = new ObjectMapper();
		objectMapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		write(new MyApiResponse("GET", "My Api GET response"), resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		write(new MyApiResponse("POST", "My Api POST response"), resp);
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		write(new MyApiResponse("PUT", "My Api PUT response"), resp);
	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		write(new MyApiResponse("DELETE", "My Api DELETE response"), resp);
	}

	private void write(MyApiResponse myApiResponse, HttpServletResponse resp) throws IOException {
		resp.setContentType("application/json");
		resp.setStatus(HttpServletResponse.SC_OK);

		ServletOutputStream output = resp.getOutputStream();

		objectMapper.writeValue(output, myApiResponse);
		output.flush();
	}
}
