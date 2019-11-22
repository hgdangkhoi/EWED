package com.epa.beans.SummaryData;

import com.epa.beans.Facility.Facility;

public class FacilityWithSummaryData_Custom {
	private Facility facility;
	private FacilityDataSummary facilityDataSummary;
	
	public FacilityWithSummaryData_Custom() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Facility getFacility() {
		return facility;
	}
	public void setFacility(Facility facility) {
		this.facility = facility;
	}
	public FacilityDataSummary getFacilityDataSummary() {
		return facilityDataSummary;
	}
	public void setFacilityDataSummary(FacilityDataSummary facilityDataSummary) {
		this.facilityDataSummary = facilityDataSummary;
	}
	
	
}
