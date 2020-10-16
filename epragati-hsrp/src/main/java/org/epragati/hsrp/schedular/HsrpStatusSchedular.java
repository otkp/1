package org.epragati.hsrp.schedular;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.epragati.auditService.AuditLogsService;
import org.epragati.common.dto.HsrpDetailDTO;
import org.epragati.constants.Schedulers;
import org.epragati.hsrp.dao.HSRPDetailDAO;
import org.epragati.hsrp.enums.RTAHSRPStatus;
import org.epragati.hsrp.mapper.DataMapper;
import org.epragati.hsrp.service.HSRPService;
import org.epragati.hsrp.vo.DataVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
/**
 * 
 * @author praveen.kuppannagari
 *
 */
@Component
public class HsrpStatusSchedular {

	private static final Logger logger = LoggerFactory.getLogger(HsrpStatusSchedular.class);

	@Value("${hsrp.contenttype}")
	String contenttype;

	@Value("${hsrp.post.tr.records.url}")
	String POST_TR_HSRP_URL;

	@Value("${scheduler.hsrp.failure.isEnable:false}")
	private Boolean isHsrpStatusSchedulerEnable;

	@Autowired
	private HSRPService hsrpService;

	@Autowired
	private HSRPDetailDAO hsrpDetailDAO;

	@Autowired
	private DataMapper dataMapper;
	
	@Autowired
	private AuditLogsService auditLogsService;
	
	

	@Scheduled(cron = "${scheduler.hsrp.failure}")
	public void hsrpStatus() {
		LocalDateTime startTime=LocalDateTime.now();
		LocalDateTime endTime=null;
		Boolean isExecuteSucess=true;
		String error=null;
		List<String> internalErrors=null;
		long startTimeInMilli = System.currentTimeMillis();
		long endTimeInMilli = System.currentTimeMillis();
		if (isHsrpStatusSchedulerEnable) {
			logger.info("Hsrp  TR POST status updation process start", LocalDateTime.now());

			//GET TEH BOTH STATUS LIST
		//	List<Integer> hsrpStatues = Arrays.asList(RTAHSRPStatus.HSRP_OPEN.getValue(), RTAHSRPStatus.TR_POST.getValue());
			
			logger.info("Start time of findByHsrpStatusIn {}",startTimeInMilli);
			
			boolean status= Boolean.TRUE;
			do{
				Pageable pageable = new PageRequest(0, 2000);
				List<HsrpDetailDTO> hsrpDetailEntities = hsrpDetailDAO.findByErrorFoundIsFalseAndHsrpStatus(RTAHSRPStatus.HSRP_OPEN.getValue(),pageable);
				
				if(hsrpDetailEntities != null && !hsrpDetailEntities.isEmpty()){
			List<HsrpDetailDTO> hsrpDetailEntitiesForTR=hsrpDetailEntities.stream().filter(P->P.getHsrpStatus().equals(RTAHSRPStatus.HSRP_OPEN.getValue())).collect(Collectors.toList());
			
			//List<HsrpDetailDTO> hsrpDetailEntitiesForPR=hsrpDetailEntities.stream().filter(P->P.getHsrpStatus().equals(RTAHSRPStatus.TR_POST.getValue())).collect(Collectors.toList());
			
			logger.info("Total number of records for TR post{}",hsrpDetailEntitiesForTR.size());
			
			
			logger.info("Excecution time of findByHsrpStatusIn {}",+ (System.currentTimeMillis() - startTimeInMilli) + " milliseconds");
			for (HsrpDetailDTO hsrpDetailDto : hsrpDetailEntities) {

				try {

					DataVO dataVO = dataMapper.convertEntity(hsrpDetailDto);
					if (RTAHSRPStatus.HSRP_OPEN.getValue() == hsrpDetailDto.getHsrpStatus()) {

						hsrpService.postTRDataToHSRP(dataVO);
					}

//					 else if ((RTAHSRPStatus.TR_POST.getValue() == hsrpDetailDto.getHsrpStatus())/* && (StringUtils.isNotBlank(hsrpDetailDto.getPrNumber()))*/) {
//
//						hsrpService.postPRDataToHSRP(dataVO);
//					}
				} catch (Exception ex) {
					error= ex.getMessage();
					isExecuteSucess=false;
					logger.error("Exception occured while hsrp  TR POST status scheduler  is running", ex);
				}
			}
			}else{
				status= Boolean.FALSE;
			}
		}while(status);
			endTime=LocalDateTime.now();
			logger.info("hsrp  TR POST scheduler time is now {}", LocalDateTime.now());

		} else {
			isExecuteSucess=false;
			endTime=LocalDateTime.now();
			logger.info("hsrp TR POST scheduler is skiped at time is now {}", LocalDateTime.now());
		}
		auditLogsService.saveScedhuleLogs(Schedulers.HSRPTRPOST, startTime, endTime, isExecuteSucess, error, null);

	}

	
	@Scheduled(cron="${scheduler.hsrp.failurePr}")
	public void hsrpPrPost(){
		
		LocalDateTime	startTime=LocalDateTime.now();
		LocalDateTime	endTime=null;
		Boolean isExecuteSucess=true;
		String error=null;
		List<String> internalErrors=null;
		long startTimeInMilli = System.currentTimeMillis();
		long endTimeInMilli = System.currentTimeMillis();
		
		if(isHsrpStatusSchedulerEnable) {
			logger.info("Hsrp PR POST status updation process start for pr post", LocalDateTime.now());
			//List<Integer> hsrpStatusIn=Arrays.asList(RTAHSRPStatus.TR_POST.getValue());
			boolean status= Boolean.TRUE;
			do {
				Pageable pageable=new PageRequest(0,2000);
				List<HsrpDetailDTO> hsrpDetailEntitiesPr = hsrpDetailDAO.findByErrorFoundIsFalseAndHsrpStatus(RTAHSRPStatus.TR_POST.getValue(),pageable);
				
				logger.info("Total number of records for PR post {}",hsrpDetailEntitiesPr.size());
				
				if(!hsrpDetailEntitiesPr.isEmpty()&&hsrpDetailEntitiesPr!=null) {
					
					logger.info("Excecution time of findByHsrpStatusIn {}",+ (System.currentTimeMillis() - startTimeInMilli) + " milliseconds");
                for(HsrpDetailDTO hsrpDetailDTO:hsrpDetailEntitiesPr) {
                	try {
                	DataVO data=dataMapper.convertEntity(hsrpDetailDTO);
                			
                			hsrpService.postPRDataToHSRP(data);
                	}catch(Exception ex) {
                		error= ex.getMessage();
    					isExecuteSucess=false;
    					logger.error("Exception occured while hsrp status FOR PR scheduler  is running", ex);
                	}
}

				}else {
					status=Boolean.FALSE;
				}
				
				
			}while(status);
			{
				endTime=LocalDateTime.now();
				logger.info("hsrp  PR POST scheduler time is now {}", LocalDateTime.now());
			}
		}else {
			isExecuteSucess=false;
			endTime=LocalDateTime.now();
			logger.info("hsrp PR POST scheduler  is skiped at time is now {}", LocalDateTime.now());
		}
		auditLogsService.saveScedhuleLogs(Schedulers.HSRPPRPOST, startTime, endTime, isExecuteSucess, error, null);
		
	}
	
}
