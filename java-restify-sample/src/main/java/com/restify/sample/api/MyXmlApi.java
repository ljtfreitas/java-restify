package com.restify.sample.api;

import java.io.IOException;
import java.time.Instant;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

@WebServlet("/api/xml")
public class MyXmlApi extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private JAXBContext jaxbContext;

	@Override
	public void init() throws ServletException {
		super.init();

		try {
			jaxbContext = JAXBContext.newInstance(MyApiResponse.class);
		} catch (JAXBException e) {
			throw new ServletException(e);
		}
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

	@Override
	protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		super.doHead(req, resp);
		resp.addDateHeader("X-MyXmlApi-Timestamp", Instant.now().toEpochMilli());
	}

	private void write(MyApiResponse myApiResponse, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("application/xml");
		resp.setStatus(HttpServletResponse.SC_OK);

		try {
			ServletOutputStream output = resp.getOutputStream();

			Marshaller marshaller = jaxbContext.createMarshaller();

			marshaller.marshal(myApiResponse, output);

			output.flush();

		} catch (JAXBException e) {
			throw new ServletException(e);
		}
	}
}
