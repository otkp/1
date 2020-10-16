package org.epragati.hsrp.utils;

/**
 * Class having basic utility methods for processing on strings.
 * 
 */
public class StringsUtil {

    /**
     * Check whether two strings are equal. </br>
     * Strings equality check are case insensitive.
     * 
     * @param str1
     * @param str2
     * @return true if equal else false
     */
    public static boolean isEqualIgnoreCase(String str1, String str2) {
        if (ObjectsUtil.isNull(str1) || ObjectsUtil.isNull(str2)) {
            return false;
        }
        return str1.trim().equalsIgnoreCase(str2.trim());
    }
    
    public static StringBuilder appendIfNotNull(StringBuilder sb, String strToAppend) {
        if (sb == null) {
            return null;
        }
        if (strToAppend != null) {
            sb.append(", ").append(strToAppend);
        }
        return sb;
    }
    
    public static StringBuilder appendIfNotNull(StringBuilder sb, Long longToAppend) {
        if (sb == null) {
            return null;
        }
        if (longToAppend != null) {
            sb.append(longToAppend);
        }
        return sb;
    }
    
    public static boolean isNullOrEmpty(String str) {
        return (str == null) || str.trim().equals("");
    }
    
    public static int percentageEqualsIntoTwoString(String string1, String string2) {
		
		if(isNullOrEmpty(string1) || isNullOrEmpty(string2) ){
			return 0;
		} else if(string1.trim().equalsIgnoreCase(string2.trim())){
			return 100;
		}else{
			string1 = string1.trim().toLowerCase();
			string2 = string2.trim().toLowerCase();
			int string1Length = string1.length();
			int string2Length = string2.length();
			int count = 0, percentage =100;
			char[] string1Char = string1.toCharArray();
			char[] string2Char = string2.toCharArray();
			
			if(string1Length <= string2Length){
				for(int i=0; i<string1Length; i++ ){
					if(string1Char[i] == string2Char[i]){
						count++;
					}
				}
				percentage = (percentage*count)/string2Length;
			} else {
				for(int i=0; i < string2Length; i++ ){
					if(string1Char[i] == string2Char[i]){
						count++;
					}
				}
				percentage = (percentage*count)/string1Length;
			}
			return percentage;
		}
	}
}
