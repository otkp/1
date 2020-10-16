package org.epragati.hsrp.vo;

import java.time.LocalDate;

import javax.validation.constraints.NotNull;

import org.epragati.hsrp.enums.RTAHSRPStatus;

import com.fasterxml.jackson.annotation.JsonFormat;

public class HSRPReportInput {

	@NotNull(message = "HSRP status is required.")
	private RTAHSRPStatus hsrpStatus;

	@NotNull(message = "From date is required.")
	@JsonFormat(pattern = "dd-MM-yyyy")
	private LocalDate fromDate;

	@NotNull(message = "To date is required.")
	@JsonFormat(pattern = "dd-MM-yyyy")
	private LocalDate toDate;

	/**
	 * @return the hsrpStatus
	 */
	public RTAHSRPStatus getHsrpStatus() {
		return hsrpStatus;
	}

	/**
	 * @param hsrpStatus
	 *            the hsrpStatus to set
	 */
	public void setHsrpStatus(RTAHSRPStatus hsrpStatus) {
		this.hsrpStatus = hsrpStatus;
	}

	/**
	 * @return the fromDate
	 */
	public LocalDate getFromDate() {
		return fromDate;
	}

	/**
	 * @param fromDate
	 *            the fromDate to set
	 */
	public void setFromDate(LocalDate fromDate) {
		this.fromDate = fromDate;
	}

	/**
	 * @return the toDate
	 */
	public LocalDate getToDate() {
		return toDate;
	}

	/**
	 * @param toDate
	 *            the toDate to set
	 */
	public void setToDate(LocalDate toDate) {
		this.toDate = toDate;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "HSRPReportInput [" + (hsrpStatus != null ? "hsrpStatus=" + hsrpStatus + ", " : "")
				+ (fromDate != null ? "fromDate=" + fromDate + ", " : "") + (toDate != null ? "toDate=" + toDate : "")
				+ "]";
	}

}
