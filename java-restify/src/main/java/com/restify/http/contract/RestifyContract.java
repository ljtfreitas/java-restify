package com.restify.http.contract;

import com.restify.http.metadata.EndpointTarget;
import com.restify.http.metadata.EndpointType;

public interface RestifyContract {

	public EndpointType read(EndpointTarget target);
}
