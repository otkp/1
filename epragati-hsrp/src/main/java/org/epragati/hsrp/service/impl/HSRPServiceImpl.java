package org.epragati.hsrp.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.epragati.aadhar.DateUtil;
import org.epragati.common.dao.HsrpSchedulerInfoDAO;
import org.epragati.common.dto.ActionsDetailsDTO;
import org.epragati.common.dto.AffixationDTO;
import org.epragati.common.dto.EmbossDTO;
import org.epragati.common.dto.HsrpDetailDTO;
import org.epragati.common.dto.HsrpPayConfirmationDTO;
import org.epragati.common.dto.HsrpSchedulerInfoDTO;
import org.epragati.common.dto.NotifyAffixationDTO;
import org.epragati.constants.MessageKeys;
import org.epragati.dispatcher.vo.InputVO;
import org.epragati.exception.BadRequestException;
import org.epragati.hsrp.apiservices.HSRPRestApiCall;
import org.epragati.hsrp.dao.HSRPDetailDAO;
import org.epragati.hsrp.enums.RTAHSRPStatus;
import org.epragati.hsrp.enums.Status;
import org.epragati.hsrp.mapper.AffixationMapper;
import org.epragati.hsrp.mapper.DataMapper;
import org.epragati.hsrp.mapper.EmbossMapper;
import org.epragati.hsrp.mapper.HsrpPayConfirmationMapper;
import org.epragati.hsrp.mapper.NotifyAffixationMapper;
import org.epragati.hsrp.service.HSRPService;
//import org.epragati.hsrp.utils.HsrpReportEnum;
import org.epragati.hsrp.utils.StringsUtil;
import org.epragati.hsrp.vo.DataVO;
import org.epragati.hsrp.vo.HSRPRTARequestModel;
import org.epragati.hsrp.vo.HSRPRequestModel;
import org.epragati.hsrp.vo.HSRPResposeVO;
import org.epragati.hsrp.vo.HsrpDetailsVO;
import org.epragati.hsrp.vo.HsrpErrorFoundVO;
import org.epragati.hsrp.vo.SaveUpdateResponse;
import org.epragati.master.dao.RegistrationDetailDAO;
import org.epragati.master.dao.StagingRegistrationDetailsDAO;
import org.epragati.master.dto.RegistrationDetailsDTO;
import org.epragati.restGateway.RestGateWayService;
import org.epragati.util.AppMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/**
 * 
 * @author praveen.kuppannagari
 *
 */
@Service
public class HSRPServiceImpl implements HSRPService {

	private static final Logger logger = LoggerFactory.getLogger(HSRPServiceImpl.class);

	@Value("${hsrp.securitykey}")
	private String securitykey;

	@Value("${hsrp.post.tr.records.url}")
	private String POST_TR_HSRP_URL;

	@Value("${hsrp.contenttype}")
	private String contenttype;

	@Value("${hsrp.post.pr.records.url}")
	private String POST_PR_HSRP_URL;

	@Autowired
	private HSRPRestApiCall hsrpRestApiCall;

	@Autowired
	private HSRPDetailDAO hsrpDetailDAO;

	@Autowired
	private AffixationMapper affixationMapper;
	
	@Autowired
	private NotifyAffixationMapper notifyAffixationMapper;

	@Autowired
	private EmbossMapper embossMapper;

	@Autowired
	private DataMapper dataMapper;

	@Autowired
	private HsrpPayConfirmationMapper hsrpPayConfirmationMapper;

	@Autowired
	private AppMessages appMessage;

	@Autowired
	private RestGateWayService restGateWayService;
	
	@Autowired
	private HsrpSchedulerInfoDAO hsrpSchedularInfoDao;
	
	@Autowired
	private RegistrationDetailDAO registrationDetailDAO;
	
	@Autowired
	StagingRegistrationDetailsDAO stagingRegistrationDetailsDAO;
	
	public List<String> errors;  

	public HSRPServiceImpl() {
	}

	@Override
	public void createHSRPTRData(DataVO dataVO) {
		Optional<HsrpDetailDTO> hsrpDetailDtoOptional = hsrpDetailDAO.findByTrNumber(dataVO.getTrNumber());
		if (hsrpDetailDtoOptional.isPresent()) {
			throw new BadRequestException(
					appMessage.getResponseMessage(MessageKeys.HSRP_EXIT_TRNUMBER) + dataVO.getTrNumber());
		}
		HsrpDetailDTO hsrpDetailDTO = dataMapper.convertVO(dataVO);
		hsrpDetailDTO.setIteration(0);
		hsrpDetailDTO.setAuthorizationRefNo(getAuthRefNo(dataVO));
		hsrpDetailDAO.save(hsrpDetailDTO);
	}

	public void processTRDateRange(String sFromDate, String sToDate, Integer sHSRPStatus) {

		// GET TEH BOTH STATUS LIST
		List<Integer> hsrpStatues = Arrays.asList(RTAHSRPStatus.HSRP_OPEN.getValue(), sHSRPStatus);

		List<HsrpDetailDTO> hsrpDetailEntities = hsrpDetailDAO.findByRtaStatusAndHsrpStatusIn(Status.OPEN.getValue(),
				hsrpStatues);
		for (HsrpDetailDTO hsrpDetailDto : hsrpDetailEntities) {

			try {

				DataVO dataVO = dataMapper.convertEntity(hsrpDetailDto);
				if (RTAHSRPStatus.HSRP_OPEN.getValue() == hsrpDetailDto.getHsrpStatus()) {

					postTRDataToHSRP(dataVO);

				} else if (RTAHSRPStatus.TR_POST.getValue() == hsrpDetailDto.getHsrpStatus()
						&& StringUtils.isNotBlank(hsrpDetailDto.getPrNumber())) {

					postPRDataToHSRP(dataVO);
				}
			} catch (Exception ex) {
				logger.error("Exception occured processing From and To Date range data ", ex);
			}
		}
	}

	public void postTRDataToHSRP(DataVO dataVO) {

		HSRPRequestModel hsrpRequestModel = new HSRPRequestModel();
		List<DataVO> dataVOList = new ArrayList<>();
		dataVOList.add(dataVO);
		hsrpRequestModel.setData(dataVOList);
		hsrpRequestModel.setSecurityKey(securitykey);
		if(StringUtils.isNoneBlank(dataVO.getPrNumber())){
		hsrpRequestModel.setPrNumber(dataVO.getPrNumber());
		}
		if(StringUtils.isNoneBlank(dataVO.getRegDate())){
		hsrpRequestModel.setRegDate(dataVO.getRegDate());
		}
		hsrpRequestModel.setAuthorizationRefNo(dataVO.getAuthorizationRefNo());
		logger.info("AuthrefNo in TR post {}",dataVO.getAuthorizationRefNo());
		try {
			Optional<HSRPResposeVO> repObjOptional = restGateWayService.callAPIPost(POST_TR_HSRP_URL, hsrpRequestModel,
					contenttype);

			if (repObjOptional.isPresent()) {

				updateHSRPTRStatus(dataVO.getAuthorizationRefNo(), repObjOptional.get().getStatus(),
						repObjOptional.get().getMessage());
			}
		} catch (Exception e1) {
			logger.error("::::::::::TR Internal Server Error from HSRP:::::::::::" + e1.getMessage());
			logger.debug(e1.toString());
		}
	}

	public void postPRDataToHSRP(DataVO dataVO) {
		HSRPRequestModel hsrpRequestModel = new HSRPRequestModel();
		List<DataVO> dataVOList = new ArrayList<>();
		dataVOList.add(dataVO);
		hsrpRequestModel.setData(dataVOList);
		hsrpRequestModel.setSecurityKey(securitykey);
		hsrpRequestModel.setPrNumber(dataVO.getPrNumber());
		hsrpRequestModel.setRegDate(dataVO.getRegDate());
		hsrpRequestModel.setAuthorizationRefNo(dataVO.getAuthorizationRefNo());
		logger.info("AuthrefNo in PR post {}",dataVO.getAuthorizationRefNo());
		try {
			
			Optional<HSRPResposeVO> repObjOptional = restGateWayService.callAPIPost(POST_PR_HSRP_URL, hsrpRequestModel,
					contenttype);

			if (repObjOptional.isPresent()) {

				updateHSRPPRStatus(dataVO.getAuthorizationRefNo(), repObjOptional.get().getStatus(),
						repObjOptional.get().getMessage());
			}
		} catch (Exception e1) {
			logger.error("::::::::::PR Internal Server Error from HSRP:::::::::::" + e1.getMessage());
			logger.debug(e1.toString());
		}

	}

	@Override
	public SaveUpdateResponse updateHSRPLaserCodes(HSRPRTARequestModel hsrprtaDetailModel) {

		SaveUpdateResponse saveUpdateResponse = new SaveUpdateResponse();
		saveUpdateResponse.setStatus("1");
		if (hsrprtaDetailModel == null) {
			saveUpdateResponse.setMessage(MessageKeys.HSRP_DATA_NOTFOUND);
			return saveUpdateResponse;
		}
	Optional<HsrpDetailDTO> hsrpDetailDtoOptional = hsrpDetailDAO.findByAuthorizationRefNo(hsrprtaDetailModel.getAuthRefNo());
		if (!hsrpDetailDtoOptional.isPresent()) {
			saveUpdateResponse.setMessage(MessageKeys.HSRP_INVALID_AUTHREFNO);
			return saveUpdateResponse;
		}
		if(!(hsrpDetailDtoOptional.get().getHsrpStatus().equals(RTAHSRPStatus.PAYMENT_RECIEVE.getValue()))){
			saveUpdateResponse.setMessage(MessageKeys.HSRP_DATA_ALREADY_EXISTS);
			return saveUpdateResponse;
		}
		if (hsrpDetailDtoOptional.get().getHsrpStatus() == null
				|| hsrpDetailDtoOptional.get().getHsrpStatus() == RTAHSRPStatus.PAYMENT_DECLINED.getValue()) {
			saveUpdateResponse.setMessage(MessageKeys.HSRP_CONFIRMATION_PENDING);
			return saveUpdateResponse;
		}
		if (StringsUtil.isNullOrEmpty(hsrprtaDetailModel.getFrontLaserCode())
				|| StringsUtil.isNullOrEmpty(hsrprtaDetailModel.getRearLaserCode())) {
			saveUpdateResponse.setMessage(MessageKeys.HSRP_LASERCODE_NOTFOUND);
			return saveUpdateResponse;
		}
		EmbossDTO embossDTO = embossMapper.convertVO(hsrprtaDetailModel);
		hsrpDetailDtoOptional.get().setHsrpStatus(RTAHSRPStatus.EMB_DATE_CREATE.getValue());
		hsrpDetailDtoOptional.get().setEmboss(embossDTO);
		updateActionsDetails(hsrpDetailDtoOptional.get());
		hsrpDetailDAO.save(hsrpDetailDtoOptional.get());
		saveUpdateResponse.setStatus("0");
		saveUpdateResponse.setMessage(MessageKeys.HSRP_SAVE_SUCCESS);
		return saveUpdateResponse;
	}

	@Override
	public SaveUpdateResponse notifyAffixation(HSRPRTARequestModel hsrprtaDetailModel) {
		
		HsrpDetailDTO hsrpDetailDto = null;
		SaveUpdateResponse saveUpdateResponse = new SaveUpdateResponse();
		saveUpdateResponse.setStatus("1");
		if (hsrprtaDetailModel == null) {
			saveUpdateResponse.setMessage(MessageKeys.HSRP_DATA_NOTFOUND);
			return saveUpdateResponse;
		}
		Optional<HsrpDetailDTO> hsrpDetailDtoOptional = hsrpDetailDAO
				.findByAuthorizationRefNo(hsrprtaDetailModel.getAuthRefNo());
		if (!hsrpDetailDtoOptional.isPresent()) {
			saveUpdateResponse.setMessage(MessageKeys.HSRP_INVALID_AUTHREFNO);
			return saveUpdateResponse;
		}
		hsrpDetailDto = hsrpDetailDtoOptional.get();
		if(!(hsrpDetailDto.getHsrpStatus().equals(RTAHSRPStatus.EMB_DATE_CREATE.getValue()))){
			saveUpdateResponse.setMessage(MessageKeys.HSRP_DATA_ALREADY_EXISTS);
			return saveUpdateResponse;
		}
		NotifyAffixationDTO notifyaffixationDTO = notifyAffixationMapper.convertVO(hsrprtaDetailModel);
		hsrpDetailDto.setNotifyAffixationDTO(notifyaffixationDTO);
		hsrpDetailDto.setHsrpStatus(RTAHSRPStatus.PLATE_AVAIL.getValue());
		updateActionsDetails(hsrpDetailDto);
		hsrpDetailDAO.save(hsrpDetailDto);
		saveUpdateResponse.setStatus("0");
		saveUpdateResponse.setMessage(MessageKeys.HSRP_SAVE_SUCCESS);
		return saveUpdateResponse;
		
	}

	@Override
	public SaveUpdateResponse confirmAffixation(HSRPRTARequestModel hsrprtaDetailModel) {

		HsrpDetailDTO hsrpDetailDto = null;
		SaveUpdateResponse saveUpdateResponse = new SaveUpdateResponse();
		saveUpdateResponse.setStatus("1");
		if (hsrprtaDetailModel == null) {
			saveUpdateResponse.setMessage(MessageKeys.HSRP_DATA_NOTFOUND);
			return saveUpdateResponse;
		}
		Optional<HsrpDetailDTO> hsrpDetailDtoOptional = hsrpDetailDAO
				.findByAuthorizationRefNo(hsrprtaDetailModel.getAuthRefNo());
		if (!hsrpDetailDtoOptional.isPresent()) {
			saveUpdateResponse.setMessage(MessageKeys.HSRP_INVALID_AUTHREFNO);
			return saveUpdateResponse;
		}
		hsrpDetailDto = hsrpDetailDtoOptional.get();
		if(!(hsrpDetailDto.getHsrpStatus().equals(RTAHSRPStatus.PLATE_AVAIL.getValue()))){
			saveUpdateResponse.setMessage(MessageKeys.HSRP_DATA_ALREADY_EXISTS);
			return saveUpdateResponse;
		}
		AffixationDTO affixationDTO = affixationMapper.convertVO(hsrprtaDetailModel);
		hsrpDetailDto.setHsrpStatus(RTAHSRPStatus.CLOSED.getValue());
		hsrpDetailDto.setAffixation(affixationDTO);
		updateActionsDetails(hsrpDetailDto);
		hsrpDetailDAO.save(hsrpDetailDto);
		saveUpdateResponse.setStatus("0");
		saveUpdateResponse.setMessage(MessageKeys.HSRP_SAVE_SUCCESS);
		return saveUpdateResponse;
	}

	@Override
	@Transactional
	public void updateHSRPTRStatus(String authorizationRefNo, Integer status, String message) {
		HsrpDetailDTO hsrpDetailDto = null;
	
	
		long startTimeInMilli = System.currentTimeMillis();
		logger.info("Start time of findByAuthorizationRefNo while TR post {}",startTimeInMilli);
		
		Optional<HsrpDetailDTO> hsrpDetailDtoOptional = hsrpDetailDAO.findByAuthorizationRefNo(authorizationRefNo);
		logger.info("Excecution time of findByAuthorizationRefNo while TR post {}",+ (System.currentTimeMillis() - startTimeInMilli) + " milliseconds");
		
		if (hsrpDetailDtoOptional.isPresent()) {
			hsrpDetailDto = hsrpDetailDtoOptional.get();
			if (status == 0) {
				hsrpDetailDto.setHsrpStatus(RTAHSRPStatus.TR_POST.getValue());
				hsrpDetailDto.setIteration(0);
				
				 
				hsrpDetailDto.setErrorFound(Boolean.TRUE);
			} else {
				hsrpDetailDto.setHsrpStatus(RTAHSRPStatus.HSRP_OPEN.getValue());
				hsrpDetailDto.setIteration(hsrpDetailDto.getIteration() + 1);
				hsrpDetailDto.setErrorFound(Boolean.TRUE);
			}
			hsrpDetailDto.setMessage(message);
			//hsrpDetailDto.setCreatedDate(LocalDateTime.now());
			hsrpDetailDto.setModifiedDate(LocalDateTime.now());
			updateActionsDetails(hsrpDetailDto); 
			hsrpDetailDAO.save(hsrpDetailDto);
		}
	}

	@Override
	@Transactional
	public void updateHSRPPRStatus(String authorizationRefNo, Integer status, String message) {
		HsrpDetailDTO hsrpDetailDTO = null;
		
		long startTimeInMilli = System.currentTimeMillis();
		logger.info("Start time of findByAuthorizationRefNo while PR post {}",startTimeInMilli);
		
		Optional<HsrpDetailDTO> hsrpDetailDTOOptional = hsrpDetailDAO.findByAuthorizationRefNo(authorizationRefNo);
		
		logger.info("Excecution time of findByAuthorizationRefNo while PR post {}",+ (System.currentTimeMillis() - startTimeInMilli) + " milliseconds");
		
		if (hsrpDetailDTOOptional.isPresent()) {
			hsrpDetailDTO = hsrpDetailDTOOptional.get();
			if (status == 0) {
				hsrpDetailDTO.setHsrpStatus(RTAHSRPStatus.PR_POST.getValue());
				hsrpDetailDTO.setIteration(0);
				hsrpDetailDTO.setErrorFound(Boolean.TRUE);
			} else {
				hsrpDetailDTO.setHsrpStatus(RTAHSRPStatus.TR_POST.getValue());
				hsrpDetailDTO.setIteration(hsrpDetailDTO.getIteration() + 1);
				hsrpDetailDTO.setErrorFound(Boolean.TRUE);
			}
			hsrpDetailDTO.setMessage(message);
			//hsrpDetailDTO.setCreatedDate(LocalDateTime.now());
			hsrpDetailDTO.setModifiedDate(LocalDateTime.now());
			updateActionsDetails(hsrpDetailDTO);
			hsrpDetailDAO.save(hsrpDetailDTO);
		}
	}

	@Override
	@Transactional
	public SaveUpdateResponse confirmPaymentOfHSRP(HSRPRTARequestModel hsrprtaRequestModel) {
		SaveUpdateResponse saveUpdateResponse = new SaveUpdateResponse();
		saveUpdateResponse.setStatus("1");
		if (hsrprtaRequestModel == null) {
			saveUpdateResponse.setMessage(MessageKeys.HSRP_DATA_NOTFOUND);
			return saveUpdateResponse;
		}
		
		HsrpDetailDTO hsrpDetailDto = null;
		Optional<HsrpDetailDTO> hsrpDetailDtoOptional = hsrpDetailDAO
				.findByAuthorizationRefNo(hsrprtaRequestModel.getAuthRefNo());
		
		if (!hsrpDetailDtoOptional.isPresent()) {
			saveUpdateResponse.setMessage(MessageKeys.HSRP_INVALID_AUTHREFNO);
			return saveUpdateResponse;
		}
		hsrpDetailDto = hsrpDetailDtoOptional.get();
		
		if(!(hsrpDetailDto.getHsrpStatus().equals(RTAHSRPStatus.PR_POST.getValue()))){
			saveUpdateResponse.setMessage(MessageKeys.HSRP_DATA_ALREADY_EXISTS);
			return saveUpdateResponse;
		}
		if (hsrprtaRequestModel.getTransactionStatus().equalsIgnoreCase("PR")){
			if (!hsrprtaRequestModel.getTransactionNo().equalsIgnoreCase(hsrpDetailDto.getTransactionNo())) {
				saveUpdateResponse.setMessage(MessageKeys.HSRP_INVALID_TRANSACTION);
				return saveUpdateResponse;
			}
			if (StringsUtil.isNullOrEmpty(hsrprtaRequestModel.getOrderNo())) {
				saveUpdateResponse.setMessage(MessageKeys.HSRP_ORDERNO_REQUIRED);
				return saveUpdateResponse;
			}
			if (hsrprtaRequestModel.getAmount() > 0) {
				if (hsrprtaRequestModel.getAmount() != Double.parseDouble(hsrpDetailDto.getHsrpFee())) {
					saveUpdateResponse.setMessage("HSRP fee amount is not matching with transaction amount ");
					return saveUpdateResponse;
				}
			}else{
				saveUpdateResponse.setMessage("HSRP fee amount is matching ");
				return saveUpdateResponse;
			}
			if (StringsUtil.isNullOrEmpty(hsrprtaRequestModel.getOrderDate())) {
				saveUpdateResponse.setMessage(MessageKeys.HSRP_ORDERDATA_REQUIRED);
				return saveUpdateResponse;
			}
			if (StringsUtil.isNullOrEmpty(hsrprtaRequestModel.getPaymentReceivedDate())
					|| (!DateUtil.isSameOrGreaterDate(DateUtil.toCurrentUTCTimeStamp(),
							DateUtil.dateFormater(hsrprtaRequestModel.getPaymentReceivedDate())))) {
				saveUpdateResponse.setMessage(MessageKeys.HSRP_PAYDATA_INCORRECT);
				return saveUpdateResponse;
			}
			hsrpDetailDto.setHsrpStatus(RTAHSRPStatus.PAYMENT_RECIEVE.getValue());
		}else{
			hsrpDetailDto.setHsrpStatus(RTAHSRPStatus.PAYMENT_DECLINED.getValue());
			saveUpdateResponse.setMessage("Invalid Payment Status Received ");
			return saveUpdateResponse;
		}

		hsrpDetailDto.setTransactionNo(hsrprtaRequestModel.getTransactionNo());
		HsrpPayConfirmationDTO hsrpPayConfirmationDTO = hsrpPayConfirmationMapper.convertVO(hsrprtaRequestModel);
		hsrpDetailDto.setHsrpPayConfirmation(hsrpPayConfirmationDTO);
		updateActionsDetails(hsrpDetailDto);
		hsrpDetailDAO.save(hsrpDetailDto);
		saveUpdateResponse.setStatus("0");
		saveUpdateResponse.setMessage(MessageKeys.HSRP_SAVE_SUCCESS);
		return saveUpdateResponse;
	}

	private void updateActionsDetails(HsrpDetailDTO entity) {
		ActionsDetailsDTO actionDetailsDTO = new ActionsDetailsDTO();

		actionDetailsDTO.setActionDatetime(LocalDateTime.now());
		if(entity.getHsrpStatus()==RTAHSRPStatus.TR_POST.getValue() || entity.getHsrpStatus()==RTAHSRPStatus.PR_POST.getValue()){
		actionDetailsDTO.setActionBy("Schedular");
		}
		else{
			actionDetailsDTO.setActionBy("performed by HSRP");
		}
		actionDetailsDTO.setReason(entity.getMessage());
		if (entity.getActionsDetails() == null) {
			entity.setActionsDetails(new ArrayList<>());
		}
		entity.getActionsDetails().add(actionDetailsDTO);
	}

	private String getAuthRefNo(DataVO dataVO) {
		Long currentDate = DateUtil.toCurrentUTCTimeStamp();
		String authRefNo = "HSRPRTA" + dataVO.getTransactionNo() + currentDate.toString();
		return authRefNo;
	}

	@Override
	public void updatePRData(DataVO dataVO) {
		Optional<HsrpDetailDTO> hsrpDetailDtoOptional = hsrpDetailDAO.findByTrNumberAndVehicleClassType(dataVO.getTrNumber(), dataVO.getVehicleClassType());
		if (hsrpDetailDtoOptional.isPresent()){
			HsrpDetailDTO hsrpDetailDTO = dataMapper.convertVO(dataVO);
			hsrpDetailDAO.save(hsrpDetailDTO);
		}
		
	}

	@Override
	public HsrpDetailDTO fetchHSRPData(String input,String catagory) {
		Optional<HsrpDetailDTO>  hsrpDetailOptional=null;
		HsrpDetailDTO hsrpDetails=null;
		if(catagory!=null && catagory.equals("TR")){
		 hsrpDetailOptional =hsrpDetailDAO.findByTrNumber(input);
		}else{
		 hsrpDetailOptional =hsrpDetailDAO.findByPrNumber(input);
		}
		if(hsrpDetailOptional.isPresent()){
			hsrpDetails=hsrpDetailOptional.get();
			hsrpDetails.setActionsDetails(null);
		}
		
		return hsrpDetails;
	}
	
	public Map<String, LocalDateTime> stringToDateConvertor(String from, String to) {
		String datePart[] = from.split("-");

		int dd = Integer.parseInt(datePart[0]);
		int mm = Integer.parseInt(datePart[1]);
		int yyyy = Integer.parseInt(datePart[2]);

		String dateFrom = LocalDate.of(yyyy, mm, dd).toString() + "T00:00:00.000Z";
		ZonedDateTime zdt = ZonedDateTime.parse(dateFrom);
		LocalDateTime localDateTime = zdt.toLocalDateTime();

		String datePart1[] = to.split("-");

		dd = Integer.parseInt(datePart1[0]);
		mm = Integer.parseInt(datePart1[1]);
		yyyy = Integer.parseInt(datePart1[2]);

		String dateFrom1 = LocalDate.of(yyyy, mm, dd).toString() + "T23:59:59.999Z";
		ZonedDateTime zdt1 = ZonedDateTime.parse(dateFrom1);
		LocalDateTime localDateTime1 = zdt1.toLocalDateTime();

		Map<String, LocalDateTime> dates = new HashMap<>();

		dates.put("from", localDateTime);
		dates.put("to", localDateTime1);
		return dates;
	}

/**	@Override
	public List<HsrpDetailDTO> fetchHsrpDataList(InputVO inputVo) {
		List<HsrpDetailDTO> hsrpDetailDtoList=null;
		List<HsrpDetailDTO> hsrpDetailDtoList1=new ArrayList<>();
		Map<String, LocalDateTime> dates = stringToDateConvertor(inputVo.getFromDate(), inputVo.getToDate());

		LocalDateTime fromDate = dates.get("from");
		LocalDateTime toDate = dates.get("to");
		hsrpDetailDtoList=hsrpDetailDAO.findByHsrpStatusInAndModifiedDateBetween
				(Arrays.asList(RTAHSRPStatus.HSRP_OPEN.getValue(), RTAHSRPStatus.TR_POST.getValue(),RTAHSRPStatus.PR_POST.getValue()),fromDate,toDate);
		for (HsrpDetailDTO hsrpDetailDto1: hsrpDetailDtoList) {
			hsrpDetailDto1.setActionsDetails(null);
		}
		//switch(inputVo.getErrorFound().equalsIgnoreCase()) {
		
		//}
		if(inputVo.getHsrpFailedStatusType().equalsIgnoreCase(HsrpReportEnum.INITIALFAILED.getValue())) {
			hsrpDetailDtoList1=hsrpDetailDtoList.stream().filter(a->a.getHsrpStatus().equals(RTAHSRPStatus.HSRP_OPEN.getValue())
				      && a.isErrorFound()).collect(Collectors.toList());
				      
	return hsrpDetailDtoList1;
	
	
		}else if(inputVo.getHsrpFailedStatusType().equalsIgnoreCase(HsrpReportEnum.TRFAILLIST.getValue())) {
			hsrpDetailDtoList1=hsrpDetailDtoList.stream().filter(a->a.getHsrpStatus().equals(RTAHSRPStatus.TR_POST.getValue())
				      && a.isErrorFound()).collect(Collectors.toList());
			return hsrpDetailDtoList1;
		}
		
		else if(inputVo.getHsrpFailedStatusType().equalsIgnoreCase(HsrpReportEnum.PRFAILEDLIST.getValue())) {
			hsrpDetailDtoList1=hsrpDetailDtoList.stream().filter(a->a.getHsrpStatus().equals(RTAHSRPStatus.PR_POST.getValue())
				      && a.isErrorFound()).collect(Collectors.toList());
			return hsrpDetailDtoList1;
		}
		return null;
	}
	*/
	
	
	/*public InputVO fetchHsrpDataListCount(InputVO inputVo) {
		
		List<HsrpDetailDTO> hsrpDetailDtoList=null;
		///List<HsrpDetailDTO> hsrpDetailDtoList1=new ArrayList<>();
		Map<String, LocalDateTime> dates = stringToDateConvertor(inputVo.getFromDate(), inputVo.getToDate());

		LocalDateTime fromDate = dates.get("from");
		LocalDateTime toDate = dates.get("to");
		
		/*
		 * hsrpDetailDtoList=hsrpDetailDAO.findByHsrpStatusInAndModifiedDateBetween
		 * (Arrays.asList(RTAHSRPStatus.HSRP_OPEN.getValue(),
		 * RTAHSRPStatus.TR_POST.getValue(),RTAHSRPStatus.PR_POST.getValue()),fromDate,
		 * toDate);
		 */
		
		/*hsrpDetailDtoList=hsrpDetailDAO.findByPrNumberIn(getPRNumbersBasedOnDates(inputVo));
	
		
		for (HsrpDetailDTO hsrpDetailDto1: hsrpDetailDtoList) {
			hsrpDetailDto1.setActionsDetails(null);
		}
		
		inputVo.setTotalCount(hsrpDetailDtoList.size());
		inputVo.setTrSuccessCount((int)hsrpDetailDtoList.stream().filter(a->a.getHsrpStatus().equals(RTAHSRPStatus.TR_POST.getValue())
				      && !a.isErrorFound()).count());
		inputVo.setPrSuccessCount((int)hsrpDetailDtoList.stream().filter(a->a.getHsrpStatus().equals(RTAHSRPStatus.PR_POST.getValue())
			      && !a.isErrorFound()).count());
		inputVo.setIntialSuccessCount((int)hsrpDetailDtoList.stream().filter(a->a.getHsrpStatus().equals(RTAHSRPStatus.HSRP_OPEN.getValue())
			      && !a.isErrorFound()).count());
		
		
		inputVo.setTrFailCount((int)hsrpDetailDtoList.stream().filter(a->a.getHsrpStatus().equals(RTAHSRPStatus.TR_POST.getValue())
			      && a.isErrorFound()).count());
		
		inputVo.setPrFailCount((int)hsrpDetailDtoList.stream().filter(a->a.getHsrpStatus().equals(RTAHSRPStatus.PR_POST.getValue())
			      && a.isErrorFound()).count());
		inputVo.setIntialFailCount((int)hsrpDetailDtoList.stream().filter(a->a.getHsrpStatus().equals(RTAHSRPStatus.HSRP_OPEN.getValue())
			      && a.isErrorFound()).count());
		
		return inputVo;
	}*/
	
	private List<RegistrationDetailsDTO> getPRNumbersBasedOnDates(InputVO inputVo) {
		List<String> prNumbers =new ArrayList<>();
		
		Map<String, LocalDateTime> dates = stringToDateConvertor(inputVo.getFromDate(), inputVo.getToDate());
		
		LocalDateTime fromDate = dates.get("from");
		LocalDateTime toDate = dates.get("to");
		
		List<RegistrationDetailsDTO> regDetailsList =registrationDetailDAO.findByPrGeneratedDateBetween(fromDate, toDate);
		return regDetailsList;
		
	}
	
	
	

	@Override
	public String saveErrorFoundRecords(HsrpErrorFoundVO hsrpErrorFoundVO) {
		hsrpDetailDAO.findByErrorFoundIsTrueAndPrNumberIn(hsrpErrorFoundVO.getTrNos());
		return null;
	}

	@Override
	public void saveSchedularDailyStatus(LocalDateTime starttime,LocalDateTime endTime) {
		List<HsrpDetailDTO> hsrpDetailDtoList=null;
		List<HsrpDetailDTO> hsrpDetailDtoList1=new ArrayList<>();
		List<HsrpDetailDTO> hsrpDetailDtoList2=new ArrayList<>();
		List<HsrpDetailDTO> hsrpDetailDtoList3=new ArrayList<>();
		
		List<HsrpDetailDTO> hsrpDetailDtoList4=new ArrayList<>();
		List<HsrpDetailDTO> hsrpDetailDtoList5=new ArrayList<>();
		List<HsrpDetailDTO> hsrpDetailDtoList6=new ArrayList<>();
		
		hsrpDetailDtoList=hsrpDetailDAO.findByHsrpStatusInAndModifiedDateBetween
				(Arrays.asList(RTAHSRPStatus.HSRP_OPEN.getValue(), RTAHSRPStatus.TR_POST.getValue(),
						RTAHSRPStatus.PR_POST.getValue()),starttime,
						endTime);
		
		hsrpDetailDtoList6=hsrpDetailDtoList.stream().filter(p->p.getHsrpStatus().equals( RTAHSRPStatus.HSRP_OPEN.getValue()) && p.isErrorFound()).collect(Collectors.toList());//tr failed
		
		
		hsrpDetailDtoList1=hsrpDetailDtoList.stream().filter(p->p.getHsrpStatus().equals( RTAHSRPStatus.TR_POST.getValue())).collect(Collectors.toList());
		hsrpDetailDtoList3=hsrpDetailDtoList1.stream().filter(p->p.isErrorFound()).collect(Collectors.toList());//pr failed
		hsrpDetailDtoList4=hsrpDetailDtoList1.stream().filter(p-> !p.isErrorFound()).collect(Collectors.toList());//tr sucess
		
		
		hsrpDetailDtoList2=hsrpDetailDtoList.stream().filter(p->p.getHsrpStatus().equals( RTAHSRPStatus.PR_POST.getValue())).collect(Collectors.toList());//tr sucess
		//hsrpDetailDtoList6=hsrpDetailDtoList2.stream().filter(p-> !p.isErrorFound()).collect(Collectors.toList());
	//	hsrpDetailDtoList5=hsrpDetailDtoList2.stream().filter(p-> p.isErrorFound()).collect(Collectors.toList());
		
		HsrpSchedulerInfoDTO hsrpSchedularInfo =new HsrpSchedulerInfoDTO();
		hsrpSchedularInfo.setTrErrorCount(hsrpDetailDtoList3.size());
		hsrpSchedularInfo.setTrErrorList(hsrpDetailDtoList3.stream().map(HsrpDetailDTO::getTrNumber).collect(Collectors.toList()));
		//List<String> names = personList.stream().map(Person::getName).collect(Collectors.toList());
		hsrpSchedularInfo.setTrSucessCount(hsrpDetailDtoList4.size());
		hsrpSchedularInfo.setTrSucessList(hsrpDetailDtoList4.stream().map(HsrpDetailDTO::getTrNumber).collect(Collectors.toList()));
		
		hsrpSchedularInfo.setPrSucessCount(hsrpDetailDtoList2.size());
		hsrpSchedularInfo.setPrSucessList(hsrpDetailDtoList2.stream().map(HsrpDetailDTO::getPrNumber).collect(Collectors.toList()));
		hsrpSchedularInfo.setPrErrorCount(hsrpDetailDtoList3.size());
		hsrpSchedularInfo.setPrErrorList(hsrpDetailDtoList3.stream().map(HsrpDetailDTO::getPrNumber).collect(Collectors.toList()));
		hsrpSchedularInfo.setTotalCount(hsrpDetailDtoList.size());
		hsrpSchedularInfo.setCreatedDate(LocalDateTime.now());
		hsrpSchedularInfoDao.save(hsrpSchedularInfo);
		//hsrpSchedularInfo.setTrSucessCount();
		

		
		
	}

	/**
	 * Pull service for HSRP side
	 */
	@Override
	public List<HsrpDetailDTO> fetcherrorfoundRecords(InputVO inputVo) {
		
		List<HsrpDetailDTO> hsrpDetailDtoListwithErrorFound=new ArrayList<>();
		
		Map<String, LocalDateTime> dates = stringToDateConvertor(inputVo.getFromDate(), inputVo.getToDate());
		
		LocalDateTime fromDate = dates.get("from");
		LocalDateTime toDate = dates.get("to");
		
		List<HsrpDetailDTO> listHsrpDetailsDto=hsrpDetailDAO.findByHsrpStatusInAndModifiedDateBetween
		(Arrays.asList(RTAHSRPStatus.HSRP_OPEN.getValue(), RTAHSRPStatus.TR_POST.getValue(),
				RTAHSRPStatus.PR_POST.getValue()),fromDate,
				toDate);
		hsrpDetailDtoListwithErrorFound=listHsrpDetailsDto.stream().filter(p->p.isErrorFound()).collect(Collectors.toList());

		return hsrpDetailDtoListwithErrorFound;
	}

	@Override
	public InputVO fetchHsrpDataList(InputVO inputVo) {
		List<HsrpDetailDTO> hsrpPrTotalData=null;
		List<HsrpDetailDTO> prPostSuccesRecords=new ArrayList<>();
		List<HsrpDetailDTO> prPostFailedRecords=new ArrayList<>();
		List<HsrpDetailDTO> trPostSuccesRecords=new ArrayList<>();
		List<HsrpDetailDTO> trPostSuccesRecords2=new ArrayList<>();
		List<HsrpDetailDTO> trPostFailedRecords=new ArrayList<>();
		List<String> prNumbers=new ArrayList<>();
		HsrpDetailsVO hsrpDetailsVO=new HsrpDetailsVO();
		List<HsrpDetailsVO> prFailedlist=new ArrayList<>();
		List<HsrpDetailsVO> trFailedlist=new ArrayList<>();
		
		
		prNumbers  = getPrNumbers(inputVo);
		hsrpPrTotalData = hsrpDetailDAO.findByHsrpStatusInAndPrNumberIn(Arrays.asList(RTAHSRPStatus.PR_POST.getValue()
				,RTAHSRPStatus.TR_POST.getValue(),RTAHSRPStatus.HSRP_OPEN.getValue()),prNumbers); 
		
		prPostSuccesRecords = hsrpPrTotalData.stream().filter(a->a.getHsrpStatus().equals(RTAHSRPStatus.PR_POST.getValue()) &&
               ! a.isErrorFound()).collect(Collectors.toList()); 


        prPostFailedRecords = hsrpPrTotalData.stream().filter(a->a.getHsrpStatus().equals(RTAHSRPStatus.TR_POST.getValue()) &&
                  a.isErrorFound()).collect(Collectors.toList()); 
        
        if(prPostFailedRecords!=null)
        {
        	 prPostFailedRecords.stream().forEach(record->{
        		 
        		 HsrpDetailsVO hsrpDetailsVOForPrFail=new HsrpDetailsVO();
        		 
        		 hsrpDetailsVOForPrFail.setPrNumber(record.getPrNumber());
        		 hsrpDetailsVOForPrFail.setHsrpStatus(record.getHsrpStatus());
        		 hsrpDetailsVOForPrFail.setMessage(record.getMessage());
        		 hsrpDetailsVOForPrFail.setVehicleType(record.getVehicleType());
        		 hsrpDetailsVOForPrFail.setTrNumber(record.getTrNumber());
        		 hsrpDetailsVOForPrFail.setErrorFound(record.isErrorFound());
        		prFailedlist.add(hsrpDetailsVOForPrFail);
        		 });	
        } 
        inputVo.setPrSuccessCount(prPostSuccesRecords.size());	 
		inputVo.setPrFailCount(prPostFailedRecords.size());
		inputVo.setHsrpPrFailedList(prFailedlist);
		trPostSuccesRecords = hsrpPrTotalData.stream().filter(a->a.getHsrpStatus().equals(RTAHSRPStatus.TR_POST.getValue()) &&
               ! a.isErrorFound()).collect(Collectors.toList()); 
		
		trPostSuccesRecords2 = hsrpPrTotalData.stream().filter(a->a.getHsrpStatus().equals(RTAHSRPStatus.TR_POST.getValue()) &&
               ! a.isErrorFound() && a.getPrNumber().isEmpty()).collect(Collectors.toList()); 
		
        trPostFailedRecords = hsrpPrTotalData.stream().filter(a->a.getHsrpStatus().equals(RTAHSRPStatus.HSRP_OPEN.getValue()) &&
                 a.isErrorFound()).collect(Collectors.toList());   
        if(trPostFailedRecords!=null)
        {
        	trPostFailedRecords.stream().forEach(record->{
        		
        		 HsrpDetailsVO hsrpDetailsVOForTrFail=new HsrpDetailsVO();
        		 hsrpDetailsVOForTrFail.setPrNumber(record.getPrNumber());
        		 hsrpDetailsVOForTrFail.setHsrpStatus(record.getHsrpStatus());
        		 hsrpDetailsVOForTrFail.setMessage(record.getMessage());
        		 hsrpDetailsVOForTrFail.setVehicleType(record.getVehicleType());
        		 hsrpDetailsVOForTrFail.setTrNumber(record.getTrNumber());
        		 hsrpDetailsVOForTrFail.setErrorFound(record.isErrorFound());
        		trFailedlist.add(hsrpDetailsVOForTrFail);
        		 });	
        } 
        Integer trSuccessCount=trPostSuccesRecords.size()+trPostSuccesRecords2.size();
        inputVo.setTrSuccessCount(trSuccessCount);	 
		inputVo.setTrFailCount(trPostFailedRecords.size());
		inputVo.setHsrpTrFailedList(trFailedlist);
		return inputVo;
		}
	private List<String> getPrNumbers(InputVO inputVo) {
		List<RegistrationDetailsDTO> regRecords =null;
		List<String> prNumbers =new ArrayList();
		Map<String, LocalDateTime> dates = stringToDateConvertor(inputVo.getFromDate(), inputVo.getToDate());

		LocalDateTime fromDate = dates.get("from");
		LocalDateTime toDate = dates.get("to");
		
		
		
		regRecords=	registrationDetailDAO.findByPrGeneratedDateBetweenAndApplicantTypeIsNullAndHsrpfeeIsNotNullOrderByCreatedDateDesc(fromDate, toDate);
		
		regRecords.stream().forEach(record->{
			
		prNumbers.add(record.getPrNo());
		});
		
		return prNumbers;
	}


	@Override
	public String pushFaileddata(InputVO inputVo) {
		Set<String> prNumbers =new HashSet<>();
		Set<String> trNumbers =new HashSet<>();
		List<HsrpDetailDTO> HsrpDetailDTO=new ArrayList<HsrpDetailDTO>();
		
		if(inputVo.getReqType().equalsIgnoreCase("date")||inputVo.getReqType()=="date") {
		
		inputVo.getHsrpPrFailedList().stream().forEach(x->{
			prNumbers.add(x.getPrNumber());
		});
		
		inputVo.getHsrpTrFailedList().stream().forEach(y->{
			if(y.getPrNumber().isEmpty()) {
				trNumbers.add(y.getTrNumber());
		}
			else
				prNumbers.add(y.getPrNumber());
				});
		if(!prNumbers.isEmpty()) {
		HsrpDetailDTO =hsrpDetailDAO.findByPrNumberIn(prNumbers);
		
		if(!HsrpDetailDTO.isEmpty()) {
		HsrpDetailDTO.stream().forEach(record1->{
			record1.setErrorFound(Boolean.FALSE);
		});
		}
		hsrpDetailDAO.save(HsrpDetailDTO);
		}
		HsrpDetailDTO=null;
		if(!trNumbers.isEmpty()) {
		HsrpDetailDTO=	hsrpDetailDAO.findByTrNumberIn(trNumbers);
		if(!HsrpDetailDTO.isEmpty()) {
		HsrpDetailDTO.stream().forEach(record2->{
			record2.setErrorFound(Boolean.FALSE);
		});
		}
		hsrpDetailDAO.save(HsrpDetailDTO);
		}
		String message="Data pushed";
		
		return message;
	}
	else 
	{
		String message=	pushSingelFaileddata(inputVo);
	
	return message;
		
	}
	}

	private String pushSingelFaileddata(InputVO inputVo) {
		HsrpDetailDTO hsrpDetailDTO=new HsrpDetailDTO();
if(inputVo.getReqType()=="prNo") {
Optional<HsrpDetailDTO> failedPrData=	hsrpDetailDAO.findByPrNumber(inputVo.getPrNo());
if(failedPrData.isPresent()) {
	hsrpDetailDTO=failedPrData.get();
	hsrpDetailDTO.setErrorFound(Boolean.FALSE);
	hsrpDetailDAO.save(hsrpDetailDTO);
}
}
else {
	
	hsrpDetailDTO=null;
	Optional<HsrpDetailDTO> failedTrData=	hsrpDetailDAO.findByTrNumber(inputVo.getTrNo());
	if(failedTrData.isPresent()) {
		hsrpDetailDTO=failedTrData.get();
		hsrpDetailDTO.setErrorFound(Boolean.FALSE);
		hsrpDetailDAO.save(hsrpDetailDTO);
	}
}
String message="Data pushed";
		return message;
	}

	
	@Override
	public InputVO fetchHsrpFailedDetailsForPrOrTr(InputVO inputVo) {

		HsrpDetailsVO hsrpFaildata = new HsrpDetailsVO();
		HsrpDetailDTO hsrpDetailDTO = new HsrpDetailDTO();

		if ( inputVo.getPrNo() != null) {

			Optional<HsrpDetailDTO> hsrpData = hsrpDetailDAO.findByPrNumber(inputVo.getPrNo());
			if(hsrpData.isPresent()) {
			hsrpDetailDTO = hsrpData.get();
            setDataToVo(hsrpDetailDTO,hsrpFaildata);
         inputVo.setSerchList(hsrpFaildata);
           return inputVo;
           
			}
			else
				throw new BadRequestException("No Data found");
           
		}

		else if ( inputVo.getTrNo() != null) {

			Optional<HsrpDetailDTO> hsrpData = hsrpDetailDAO.findByTrNumber(inputVo.getTrNo());
			if(hsrpData.isPresent()) {
			hsrpDetailDTO = hsrpData.get();
			setDataToVo(hsrpDetailDTO,hsrpFaildata);
			inputVo.setSerchList(hsrpFaildata);
			 return inputVo;
			}
			else
				throw new BadRequestException("No Data found");
			
			
			
		}
		else 
			throw new BadRequestException("No inputs");
		
	}

	
	private void setDataToVo(HsrpDetailDTO hsrpDetailDTO, HsrpDetailsVO hsrpFaildata) {

		hsrpFaildata.setPrNumber(hsrpDetailDTO.getPrNumber());
		hsrpFaildata.setHsrpStatus(hsrpDetailDTO.getHsrpStatus());
		hsrpFaildata.setMessage(hsrpDetailDTO.getMessage());
		hsrpFaildata.setVehicleType(hsrpDetailDTO.getVehicleType());
		hsrpFaildata.setTrNumber(hsrpDetailDTO.getTrNumber());
	
		
}

	@Override
public InputVO fetchHsrpDataListCount(InputVO inputVo) {
		
		List<HsrpDetailDTO> hsrpDetailDtoList=null;
		///List<HsrpDetailDTO> hsrpDetailDtoList1=new ArrayList<>();
		Map<String, LocalDateTime> dates = stringToDateConvertor(inputVo.getFromDate(), inputVo.getToDate());

		LocalDateTime fromDate = dates.get("from");
		LocalDateTime toDate = dates.get("to");
		inputVo.setPrGeneratedCount(getPrGeneratedCount( inputVo));
		inputVo.setTrGeneratedCount(getTrGeneratedCount( inputVo));
		return inputVo;
	}
	
	private Integer getPrGeneratedCount(InputVO inputVo) {
			Map<String, LocalDateTime> dates = stringToDateConvertor(inputVo.getFromDate(), inputVo.getToDate());
		LocalDateTime fromDate = dates.get("from");
		LocalDateTime toDate = dates.get("to");
	    Integer prGeneratedCount =registrationDetailDAO.countByPrGeneratedDateBetweenAndApplicantTypeIsNullAndHsrpfeeIsNotNullOrderByCreatedDateDesc(fromDate, toDate);
		return prGeneratedCount;
	}
	
	private Integer getTrGeneratedCount(InputVO inputVo) {
		Map<String, LocalDateTime> dates = stringToDateConvertor(inputVo.getFromDate(), inputVo.getToDate());
		LocalDateTime fromDate = dates.get("from");
		LocalDateTime toDate = dates.get("to");
		Integer trGeneratedCount =stagingRegistrationDetailsDAO.countByTrGeneratedDateBetween(fromDate, toDate);
		return trGeneratedCount;	
	}
	
}
