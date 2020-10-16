package org.epragati.hsrp.dao;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.epragati.common.dao.BaseRepository;
import org.epragati.common.dto.HsrpDetailDTO;
import org.epragati.master.dto.RegistrationDetailsDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface HSRPDetailDAO extends BaseRepository<HsrpDetailDTO, Serializable> {

	Optional<HsrpDetailDTO> findByAuthorizationRefNo(String authorizationRefNo);

	List<HsrpDetailDTO> findByHsrpStatusIn(List<Integer> hsrpStatues);

	List<HsrpDetailDTO> findByRtaStatusAndHsrpStatusIn(Integer rtaStatus, List<Integer> hsrpStatus);

	List<HsrpDetailDTO> findByHsrpStatusInAndModifiedDateBetween(List<Integer> hsrpStatus, LocalDateTime fromDate,
			LocalDateTime toDate);

	Optional<HsrpDetailDTO> findByTrNumber(String trNumber);

	Optional<HsrpDetailDTO> findByTrNumberAndVehicleClassType(String trNumber, String vehicleClassType);

	Optional<HsrpDetailDTO> findByAuthorizationRefNoAndHsrpStatus(String authorizationRefNo, Integer hsrpStatus);
	
	Optional<HsrpDetailDTO> findByPrNumber(String prNumber);
	
	List<HsrpDetailDTO> findByErrorFoundIsTrueAndPrNumberIn(List<String> prNumbers);
	
	List<HsrpDetailDTO> findByErrorFoundIsTrueAndTrNumberIn(List<String> trNumbers);
	
	List<HsrpDetailDTO> findByErrorFoundIsFalseAndHsrpStatusIn(List<Integer> hsrpStatues,Pageable pageable);
	
	List<HsrpDetailDTO> findByErrorFoundIsTrueAndHsrpStatusIn(List<Integer> hsrpStatues);

//	List<HsrpDetailDTO> findByPrNumberIn(List<RegistrationDetailsDTO> prNumbersBasedOnDates);

	List<HsrpDetailDTO> findByHsrpStatusInAndPrNumberIn(List<Integer> asList, List<String> prNumbers);

	List<HsrpDetailDTO> findByPrNumberIn(Set<String> prNumbers);

	List<HsrpDetailDTO> findByTrNumberIn(Set<String> trNumbers);

	List<HsrpDetailDTO> findByErrorFoundIsFalseAndHsrpStatus(Integer value, Pageable pageable);

}
