/**
 * 
 */
package org.epragati.hsrp.enums;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public enum RTAHSRPStatus {

	TR_POST(1, "TR_POST"), PR_POST(2, "PR_POST"), PAYMENT_DEPOSIT(3, "PAYMENT_DEPOSIT"), PAYMENT_DECLINED(4,
			"PAYMENT_DECLINED"), PAYMENT_RECIEVE(5, "PAYMENT_RECIEVE"), EMB_DATE_CREATE(6,
					"EMB_DATE_CREATE"), PLATE_AVAIL(7,
							"PLATE_AVAIL"), CLOSED(8, "CLOSED"), PENDING(9, "PENDING"), HSRP_OPEN(10, "HSRP_OPEN");

	private static final Map<Integer, String> lookup = new HashMap<Integer, String>();
	private static final Map<String, RTAHSRPStatus> labelToStatus = new HashMap<String, RTAHSRPStatus>();
	private static final Map<Integer, RTAHSRPStatus> valueToStatus = new HashMap<Integer, RTAHSRPStatus>();

	static {
		for (RTAHSRPStatus status : EnumSet.allOf(RTAHSRPStatus.class)) {
			lookup.put(status.getValue(), status.getLabel());
		}
		for (RTAHSRPStatus status : EnumSet.allOf(RTAHSRPStatus.class)) {
			labelToStatus.put(status.getLabel(), status);
		}
		for (RTAHSRPStatus status : EnumSet.allOf(RTAHSRPStatus.class)) {
			valueToStatus.put(status.getValue(), status);
		}
	}

	private Integer value;
	private String label;

	private RTAHSRPStatus(Integer valuesd, String label) {
		this.value = valuesd;
		this.label = label;
	}

	public Integer getValue() {
		return value;
	}

	public String getLabel() {
		return label;
	}

	public static String getLabel(Integer value) {
		return lookup.get(value);
	}

	public static RTAHSRPStatus getStatus(String label) {
		return StringUtils.isBlank(label) ? null : labelToStatus.get(label.toUpperCase());
	}

	public static RTAHSRPStatus getStatus(Integer value) {
		return valueToStatus.get(value);
	}

}
