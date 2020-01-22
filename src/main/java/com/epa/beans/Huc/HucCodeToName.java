package com.epa.beans.Huc;

import org.springframework.stereotype.Component;

/*
 * Map the HUC8code to HUC8name when frontend use HUC8Name to query
 * Example API: ewedService/getFacilityData/HUC8Name/santa%20ana%20watershed%20(ca)/2015/1/2015/12/fuelTypes/all
 */
@Component
public class HucCodeToName {
	private String HUC8Name;
	private String HUC8Code;
	public String getHUC8Name() {
		return HUC8Name;
	}
	public void setHUC8Name(String hUC8Name) {
		HUC8Name = hUC8Name;
	}
	public String getHUC8Code() {
		return HUC8Code;
	}
	public void setHUC8Code(String hUC8Code) {
		HUC8Code = hUC8Code;
	}
	public HucCodeToName(String hUC8Name, String hUC8Code) {
		super();
		HUC8Name = hUC8Name;
		HUC8Code = hUC8Code;
	}
	
	
}
