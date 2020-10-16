package org.epragati.hsrp.apiservices.impl;

import org.epragati.hsrp.apiservices.HSRPRestApiCall;
import org.epragati.hsrp.vo.HSRPRequestModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class HSRPRestApiCallImpl implements HSRPRestApiCall {
	
	@Autowired
	private RestTemplate restTemplate;

	private static final Logger logger = LoggerFactory.getLogger(HSRPRestApiCall.class);

	public ResponseEntity<String> callAPIPost(String apiPath, HSRPRequestModel hsrprtaDetailModel, String contentType){
		logger.info("::::::::::callAPIPost:::::::::::");
		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", contentType);
		HttpEntity<HSRPRequestModel> entity = new HttpEntity<>(hsrprtaDetailModel, headers);
		return restTemplate.exchange(apiPath, HttpMethod.POST, entity, String.class);
	}
}
