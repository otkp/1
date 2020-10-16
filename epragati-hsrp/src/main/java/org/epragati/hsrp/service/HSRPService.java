package org.epragati.hsrp.service;

import java.time.LocalDateTime;
import java.util.List;

import org.epragati.common.dto.HsrpDetailDTO;
import org.epragati.dispatcher.vo.InputVO;
import org.epragati.hsrp.vo.DataVO;
import org.epragati.hsrp.vo.HSRPRTARequestModel;
import org.epragati.hsrp.vo.HsrpErrorFoundVO;
import org.epragati.hsrp.vo.SaveUpdateResponse;

public interface HSRPService {

	void postTRDataToHSRP(DataVO dataVO);

	void postPRDataToHSRP(DataVO DataVO);

	public SaveUpdateResponse updateHSRPLaserCodes(HSRPRTARequestModel hsrprtaRequestModel);

	public SaveUpdateResponse notifyAffixation(HSRPRTARequestModel hsrprtaRequestModel);

	public SaveUpdateResponse confirmAffixation(HSRPRTARequestModel hsrprtaRequestModel);

	public void updateHSRPPRStatus(String authorizationRefNo, Integer status, String message);

	public void updateHSRPTRStatus(String authorizationRefNo, Integer status, String message);

	SaveUpdateResponse confirmPaymentOfHSRP(HSRPRTARequestModel hsrpDetailModel);

	void createHSRPTRData(DataVO dataVO);
	
	public void processTRDateRange(String sFromDate, String sToDate, Integer sHSRPStatus);
	
	//for testing pr update
	void updatePRData(DataVO dataVO);

	HsrpDetailDTO fetchHSRPData(String input, String catagory);
	
	InputVO fetchHsrpDataList(InputVO inputVO);
	
	public InputVO fetchHsrpDataListCount(InputVO inputVO);

	public String saveErrorFoundRecords(HsrpErrorFoundVO hsrpErrorFoundVO);

	void saveSchedularDailyStatus(LocalDateTime startTime, LocalDateTime endTime);

	List<HsrpDetailDTO> fetcherrorfoundRecords(InputVO inputVo);

	String pushFaileddata(InputVO inputVo);

	InputVO fetchHsrpFailedDetailsForPrOrTr(InputVO inputVo);

	
}
