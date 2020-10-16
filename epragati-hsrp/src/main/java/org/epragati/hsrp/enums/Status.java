/**
 * 
 */
package org.epragati.hsrp.enums;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
 * @author arun.verma
 *
 */
public enum Status {

	FRESH(1, "FRESH"), PENDING(2, "PENDING"), APPROVED(3, "APPROVED"), REJECTED(4, "REJECTED"),
    CLOSED(5,"CLOSED"), EXPIRED(6, "EXPIRED"), CANCELLED(7,"CANCELLED"), OPEN(8, "OPEN"),
    CANCEL_APPROVED(9, "CANCEL_APPROVED"), INSPECTED(10, "INSPECTED"), CCO(11, "CCO"), MVI(12, "MVI"),
    CCO_MVI(13, "CCO_MVI"), AO(14, "AO"), RTO(15, "RTO"), UPLOADED(20, "UPLOADED"), FAILURE(21, "FAILURE"), SUCCESS(22, "SUCCESS"),
    AADHAR_PENDING(23, "AADHAR_PENDING"), WARNING(24, "WARNING"), PR_PENDING(25, "PR_PENDING"), APP_COMPLETED(26, "APP_COMPLETED"), BODY_BUILDER(27, "BODY_BUILDER"),
    
    STEP1_BUYER_REQUEST(28, "STEP1 REQUEST INITIATE"), STEP2_FINANCER_PENDING(29, "STEP2 FINANCIER PENDING"),
    STEP2_FINANCER_REJECTED(30, "STEP2 FINANCIER REJECTED"), STEP2_FINANCER_ACCEPTED(31, "STEP2 FINANCIER ACCEPTED"),
    STEP3_BUYER_CONFIRM(32, "STEP3 BUYER CONFIRM"), STEP3_BUYER_REFUSE(33, "STEP3 BUYER REFUSE"),
    STEP4_FINANCER_APPROVED(34, "STEP4 FINANCIER APPROVED"), STEP4_FINANCER_REJECTED(35, "STEP4 FINANCIER REJECTED"),
    APP_RE_INITIATED_FINANCE(36, "Application Re-Initiated")/*to track this app has re-initiated at finance step*/,
    
    PAST(40, "PAST"), PRESENT(41, "PRESENT"), FUTURE(42, "FUTURE"), DIFFERENTIAL_TAX(43, "DIFFERENTIAL TAX"),
    
    THEFT_INTIMATION(50, "THEFT_INTIMATION"), SUSPENDED(51, "SUSPENDED"), SURRENDERED(52, "SURRENDERED"), IIB_VERIFIED(53,"IIB_VERIFIED"), OBJECTION(54, "OBJECTION"),
    STOPPAGE_TAX(55, "STOPPAGE TAX"), NA(100, "NA"),LOCKED(101, "LOCKED");
	
    private static final Map<Integer, String> lookup = new HashMap<Integer, String>();
    private static final Map<String, Status> labelToStatus = new HashMap<String, Status>();
    private static final Map<Integer, Status> valueToStatus = new HashMap<Integer, Status>();

    static {
        for (Status status : EnumSet.allOf(Status.class)) {
            lookup.put(status.getValue(), status.getLabel());
        }
        for (Status status : EnumSet.allOf(Status.class)) {
            labelToStatus.put(status.getLabel(), status); 
        }
        for (Status status : EnumSet.allOf(Status.class)) {
            valueToStatus.put(status.getValue(), status); 
        }
    }

    private Integer value;
    private String label;

    private Status(Integer valuesd, String label) {
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
    
    public static Status getStatus(String label) {
        return StringUtils.isBlank(label) ? null : labelToStatus.get(label.toUpperCase());
    }
    
    public static Status getStatus(Integer value) {
        return valueToStatus.get(value);
    }
    
}
