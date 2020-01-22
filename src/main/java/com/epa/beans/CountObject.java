package com.epa.beans;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DEPRECATED - NO LONGER USED 
 * This java object can map to the json returned when calling
 * count for any envirofacts table. 
 */
public class CountObject {
	@JsonProperty("TOTALQUERYRESULTS") public int count;

	@Override
	public String toString() {
		return "CountObject [count=" + count + "]";
	} 
	
}
