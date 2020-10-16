package org.epragati.hsrp.apiservices;

import org.epragati.hsrp.vo.HSRPRequestModel;
import org.springframework.http.ResponseEntity;

public interface HSRPRestApiCall {

	ResponseEntity<String> callAPIPost(String apiPath, HSRPRequestModel hsrprtaDetailModel, String contentType);

}
