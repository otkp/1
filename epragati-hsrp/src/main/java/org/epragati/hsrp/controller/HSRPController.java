package org.epragati.hsrp.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.epragati.aadhar.APIResponse;
import org.epragati.common.dto.HsrpDetailDTO;
import org.epragati.constants.MessageKeys;
import org.epragati.dispatcher.vo.InputVO;
import org.epragati.exception.BadRequestException;
import org.epragati.hsrp.service.HSRPService;
import org.epragati.hsrp.utils.ObjectsUtil;
import org.epragati.hsrp.vo.DataVO;
import org.epragati.hsrp.vo.HSRPRTARequestModel;
import org.epragati.hsrp.vo.HsrpErrorFoundVO;
import org.epragati.hsrp.vo.SaveUpdateResponse;
import org.epragati.util.GateWayResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 
 * @author praveen.kuppannagari
 *
 */
@RestController
@CrossOrigin
public class HSRPController {
	/**
	 * 1) create is used to presist the data
	 * 
	 * 2)TR post and PR post are called by the schedular
	 * 
	 * 3)For remaining methods data is given by HSRP and that data is updated in our
	 * 
	 * in our side.
	 */
	private static final Logger logger = LoggerFactory.getLogger(HSRPController.class);

	@Autowired
	private HSRPService hsrpService;

	@RequestMapping(value = "/posttrhsrp", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	public void postTRDataToHSRP(@RequestBody DataVO dataVO) {
		hsrpService.postTRDataToHSRP(dataVO);
	}

	@RequestMapping(value = "/postprhsrp", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	public void postPRDataToHSRP(@RequestBody DataVO dataVO) {
		hsrpService.postPRDataToHSRP(dataVO);
	}

	@RequestMapping(path = "/provide/laser/", method = RequestMethod.POST, produces = {
			MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	public ResponseEntity<?> updateHSRPLaserCodes(HttpServletRequest request,
			@RequestBody HSRPRTARequestModel hsrprtaDetailModel) {
		SaveUpdateResponse saveUpdateResponse = new SaveUpdateResponse();
		try {
			saveUpdateResponse = hsrpService.updateHSRPLaserCodes(hsrprtaDetailModel);
		} catch (IllegalArgumentException ie) {
			saveUpdateResponse.setStatus(SaveUpdateResponse.FAILURE);
			saveUpdateResponse.setMessage(ie.getMessage());
			return ResponseEntity.status(HttpStatus.ACCEPTED).body(saveUpdateResponse);
		} catch (Exception e) {
			logger.error("Exception while saving updateHSRPLaserCodes HSRPTransaction Details :: " + e.getMessage());
			saveUpdateResponse.setStatus(SaveUpdateResponse.FAILURE);
			saveUpdateResponse.setMessage("Parameter(s) violating Some Data Integrity !");
			return ResponseEntity.status(HttpStatus.ACCEPTED).body(saveUpdateResponse);
		}
		if (ObjectsUtil.isNull(saveUpdateResponse)) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
		}
		return ResponseEntity.status(HttpStatus.OK).body(saveUpdateResponse);
	}

	@RequestMapping(path = "/notify/affixation", method = RequestMethod.POST, produces = {
			MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	public ResponseEntity<?> notifyAffixation(HttpServletRequest request,
			@Valid @RequestBody HSRPRTARequestModel hsrprtaDetailModel) {
		SaveUpdateResponse saveUpdateResponse = new SaveUpdateResponse();
		try {
			saveUpdateResponse = hsrpService.notifyAffixation(hsrprtaDetailModel);
		} catch (Exception e) {
			logger.error("Exception while saving notifyAffixation HSRPTransaction Details :: " + e.getMessage());
			saveUpdateResponse.setStatus(SaveUpdateResponse.FAILURE);
			saveUpdateResponse.setMessage("Parameter(s) violating Some Data Integrity !");
			return ResponseEntity.status(HttpStatus.ACCEPTED).body(saveUpdateResponse);
		}
		if (ObjectsUtil.isNull(saveUpdateResponse)) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
		}
		return ResponseEntity.status(HttpStatus.OK).body(saveUpdateResponse);
	}

	@RequestMapping(path = "/confirm/affixation", method = RequestMethod.POST, produces = {
			MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	public ResponseEntity<?> confirmAffixation(HttpServletRequest request,
			@Valid @RequestBody HSRPRTARequestModel hsrprtaDetailModel) {
		SaveUpdateResponse saveUpdateResponse = new SaveUpdateResponse();
		try {
			saveUpdateResponse = hsrpService.confirmAffixation(hsrprtaDetailModel);
		} catch (Exception e) {
			logger.error("Exception while saving confirmAffixation HSRPTransaction Details :: " + e.getMessage());
			logger.debug(e.toString());
			saveUpdateResponse.setStatus(SaveUpdateResponse.FAILURE);
			saveUpdateResponse.setMessage("Parameter(s) violating Some Data Integrity !");
			return ResponseEntity.status(HttpStatus.ACCEPTED).body(saveUpdateResponse);
		}
		if (ObjectsUtil.isNull(saveUpdateResponse)) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
		}
		return ResponseEntity.status(HttpStatus.OK).body(saveUpdateResponse);
	}

	@RequestMapping(path = "/notify/payment/", method = RequestMethod.POST, produces = {
			MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	public ResponseEntity<?> confirmPaymentOfHSRP(HttpServletRequest request,
			@Valid @RequestBody HSRPRTARequestModel hsrprtaDetailModel) {
		SaveUpdateResponse saveUpdateResponse = new SaveUpdateResponse();
		if (ObjectsUtil.isNull(hsrprtaDetailModel)) {
			saveUpdateResponse.setStatus(SaveUpdateResponse.FAILURE);
			saveUpdateResponse.setMessage("HSRPDetail Required !!!");
			return ResponseEntity.status(HttpStatus.ACCEPTED).body(saveUpdateResponse);
		}
		try {
			saveUpdateResponse = hsrpService.confirmPaymentOfHSRP(hsrprtaDetailModel);
		} catch (IllegalArgumentException ie) {
			saveUpdateResponse.setStatus(SaveUpdateResponse.FAILURE);
			saveUpdateResponse.setMessage(ie.getMessage());
			return ResponseEntity.status(HttpStatus.ACCEPTED).body(saveUpdateResponse);
		} catch (Exception e) {
			logger.error("Exception while saving confirmPaymentOfHSRP HSRPTransaction Details :: " + e.getMessage());
			logger.debug(e.toString());
			saveUpdateResponse.setStatus(SaveUpdateResponse.FAILURE);
			saveUpdateResponse.setMessage("Parameter(s) violating Some Data Integrity !");
			return ResponseEntity.status(HttpStatus.ACCEPTED).body(saveUpdateResponse);
		}
		if (ObjectsUtil.isNull(saveUpdateResponse)) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
		}
		return ResponseEntity.status(HttpStatus.OK).body(saveUpdateResponse);
	}

	@RequestMapping(path = "/createtrmodel", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	public APIResponse<?> createHSRPTRData(@RequestBody DataVO dataVO) {
		try {
			hsrpService.createHSRPTRData(dataVO);
			return new APIResponse<>(true, HttpStatus.OK, "successfully saved");
		} catch (BadRequestException bre) {
			return new APIResponse(HttpStatus.NOT_FOUND, bre.getMessage());
		}
	}

	@RequestMapping(path = "/fetchhsrpdetails", method = RequestMethod.GET, produces = {
			MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	public GateWayResponse<?> fetchHsrpDetails(@RequestParam(value = "input") String input,
			@RequestParam(value = "catagory") String catagory) {
		try {
			HsrpDetailDTO result = hsrpService.fetchHSRPData(input, catagory);
			if (result != null) {
				return new GateWayResponse<>(HttpStatus.OK, result, MessageKeys.MESSAGE_SUCCESS);
			} else {
				return new GateWayResponse<>(HttpStatus.OK, result, MessageKeys.HSRP_DATA_NOTFOUND);
			}
		} catch (BadRequestException bre) {
			return new GateWayResponse<>(HttpStatus.NO_CONTENT, bre, MessageKeys.HSRP_DATA_NOTFOUND);
		}
	}

	/**
	 * hsrp error found true List
	 */
	/*@PostMapping(path = "/fetcherrorfoundhsrprecordsdatewise", produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	public GateWayResponse<?> fetchHsrpDetailsList(@RequestBody InputVO inputVo) {
		try {
			List<HsrpDetailDTO> result = hsrpService.fetchHsrpDataList(inputVo);
			if (result != null) {
				return new GateWayResponse<>(HttpStatus.OK, result, MessageKeys.MESSAGE_SUCCESS);
			} else {
				return new GateWayResponse<>(HttpStatus.OK, result, MessageKeys.HSRP_DATA_NOTFOUND);
			}
		} catch (BadRequestException bre) {
			return new GateWayResponse<>(HttpStatus.NO_CONTENT, bre, MessageKeys.HSRP_DATA_NOTFOUND);
		}
	}
*/
	
	/*
	 * @PostMapping(path="/viewHrspData", produces=
	 * {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}) public
	 * GateWayResponse<?> viewHsrpData(@ResponseBody InputVO inputVO){
	 * 
	 * }
	 */
	/**
	 * hsrp error found count
	 */
	/**@PostMapping(path = "/fetchhsrpdetailscount", produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	public GateWayResponse<?> fetchHsrpDetailsCount(@RequestBody InputVO inputVo) {
		try {
			InputVO result = hsrpService.fetchHsrpDataListCount(inputVo);
			if (result != null) {
				return new GateWayResponse<>(HttpStatus.OK, result, MessageKeys.MESSAGE_SUCCESS);
			} else {
				return new GateWayResponse<>(HttpStatus.OK, result, MessageKeys.HSRP_DATA_NOTFOUND);
			}
		} catch (BadRequestException bre) {
			return new GateWayResponse<>(HttpStatus.NO_CONTENT, bre, MessageKeys.HSRP_DATA_NOTFOUND);
		}
	}
	
	*/
	/**
	 * hsrp data update to new collection hsrp_error_found
	 */
	@RequestMapping(path = "/saveerrorfoundrecordslist", method = RequestMethod.GET, produces = {
			MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	public GateWayResponse<?> saveErrorFoundRecordsList(@RequestBody HsrpErrorFoundVO hsrpErrorFoundVO) {
		try {
			String result = hsrpService.saveErrorFoundRecords(hsrpErrorFoundVO);
			if (result != null) {
				return new GateWayResponse<>(HttpStatus.OK, result, MessageKeys.MESSAGE_SUCCESS);
			} else {
				return new GateWayResponse<>(HttpStatus.OK, result, MessageKeys.HSRP_DATA_NOTFOUND);
			}
		} catch (BadRequestException bre) {
			return new GateWayResponse<>(HttpStatus.NO_CONTENT, bre, MessageKeys.HSRP_DATA_NOTFOUND);
		}
	}
	
	/**
	 * pull service for HSRP.
	 */
	@PostMapping(path="/fetcherrorfounddatabyhsrp", produces= {MediaType.APPLICATION_XML_VALUE,MediaType.APPLICATION_JSON_VALUE})
	public GateWayResponse<?> fetcherrorFoundDataByHsrp(@RequestBody InputVO inputVo){
		try {
		List<HsrpDetailDTO> hsrpDetailsList =  hsrpService.fetcherrorfoundRecords(inputVo);
		if (hsrpDetailsList != null) {
			return new GateWayResponse<>(HttpStatus.OK, hsrpDetailsList, MessageKeys.MESSAGE_SUCCESS);
		} else {
			return new GateWayResponse<>(HttpStatus.OK, hsrpDetailsList, MessageKeys.HSRP_DATA_NOTFOUND);
		}
	} catch (BadRequestException bre) {
		return new GateWayResponse<>(HttpStatus.NO_CONTENT, bre, MessageKeys.HSRP_DATA_NOTFOUND);
	}
		
	}
	
	@PostMapping(path = "/fetchhsrpdetailsList", produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	public GateWayResponse<?> fetchHsrpDetailsList(@RequestBody InputVO inputVo) {
		try {
			InputVO result = hsrpService.fetchHsrpDataList(inputVo);
			if (result != null) {
				return new GateWayResponse<>(HttpStatus.OK, result, MessageKeys.MESSAGE_SUCCESS);
			} else {
				return new GateWayResponse<>(HttpStatus.OK, result, MessageKeys.HSRP_DATA_NOTFOUND);
			}
		} catch (BadRequestException bre) {
			return new GateWayResponse<>(HttpStatus.NO_CONTENT, bre, MessageKeys.HSRP_DATA_NOTFOUND);
		}
	}
	
	@PostMapping(path = "/fetchhsrpdetailscount", produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	public GateWayResponse<?> fetchHsrpDetailsCount(@RequestBody InputVO inputVo) {
		try {
			InputVO result = hsrpService.fetchHsrpDataListCount(inputVo);
			if (result != null) {
				return new GateWayResponse<>(HttpStatus.OK, result, MessageKeys.MESSAGE_SUCCESS);
			} else {
				return new GateWayResponse<>(HttpStatus.OK, result, MessageKeys.HSRP_DATA_NOTFOUND);
			}
		} catch (BadRequestException bre) {
			return new GateWayResponse<>(HttpStatus.NO_CONTENT, bre, MessageKeys.HSRP_DATA_NOTFOUND);
		}
	}
	
	@PostMapping(path = "/pushFaileddata", produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	public GateWayResponse<?> pushFaileddata(@RequestBody InputVO inputVo) {
		try {
			String result = hsrpService.pushFaileddata(inputVo);
			if (result != null) {
				return new GateWayResponse<>(HttpStatus.OK, result, MessageKeys.MESSAGE_SUCCESS);
			} else {
				return new GateWayResponse<>(HttpStatus.OK, result, MessageKeys.HSRP_DATA_NOTFOUND);
			}
		} catch (BadRequestException bre) {
			return new GateWayResponse<>(HttpStatus.NO_CONTENT, bre, MessageKeys.HSRP_DATA_NOTFOUND);
		}
	}
	

	
	@PostMapping(path = "/fetchfailrecordsPrORTr", produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	public GateWayResponse<?> fetchHsrpFailedDetailsForPrOrTr(@RequestBody InputVO inputVo) {
		try {
			
			
			if ((inputVo.getPrNo()!=null) ||(inputVo.getTrNo()!=null) )
			
			{

				InputVO result = hsrpService.fetchHsrpFailedDetailsForPrOrTr(inputVo);
				if (result != null) {
					return new GateWayResponse<>(HttpStatus.OK, result, MessageKeys.MESSAGE_SUCCESS);
				} else {
					return new GateWayResponse<>(HttpStatus.BAD_REQUEST,  MessageKeys.HSRP_DATA_NOTFOUND);
				}

			} else
				throw new BadRequestException("No inputs");

		} catch (BadRequestException bre) {
			return new GateWayResponse<>(HttpStatus.BAD_REQUEST, bre.getMessage());
		}
	}
	
}