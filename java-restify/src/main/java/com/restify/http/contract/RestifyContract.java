package com.restify.http.contract;

import com.restify.http.contract.metadata.EndpointTarget;
import com.restify.http.contract.metadata.EndpointType;

public interface RestifyContract {

	EndpointType read(EndpointTarget target);

}