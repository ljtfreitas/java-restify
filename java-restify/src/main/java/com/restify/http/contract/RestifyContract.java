package com.restify.http.contract;

import com.restify.http.contract.metadata.EndpointTarget;
import com.restify.http.contract.metadata.EndpointType;

public interface RestifyContract {

	public EndpointType read(EndpointTarget target);
}
