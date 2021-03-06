package com.epa.dataCollector;

public interface EwedApiService {

	public String getFacility(String filterField, String filterValue, int minYear, int minMonth, int maxYear, int maxMonth);
	
	public String getFutureFacility(String caseModel, String filterField, String filterValue, int minYear, int minMonth, int maxYear, int maxMonth);
	
	public String getFacilityData(String filterField, String filterValue, int minYear, int minMonth, int maxYear, int maxMonth, String fuelTypes, String[] fuelTypeList);
	
	public String getMonthWiseSummary(String filterField, String filterValue, int minYear, int minMonth, int maxYear, int maxMonth, String fuelTypes, String[] fuelTypeList);

	public String getFutureFacilityData(String caseModel, String filterField, String filterValue, int minYear, int minMonth, int maxYear, int maxMonth, String fuelTypes, String[] fuelTypeList);
	
	public String getFutureMonthWiseSummary(String caseModel, String climateModel, String filterField, String filterValue, int minYear, int minMonth, int maxYear, int maxMonth, String fuelTypes, String[] fuelTypeList);
	
	public String defaultGEWData(String filterName, int minYear, int minMonth, int maxYear, int maxMonth, String fuelTypes, String[] fuelTypeList);
	
	public String futureDefaultGEWData(String caseModel, String filterName, int minYear, int minMonth, int maxYear, int maxMonth, String fuelTypes, String[] fuelTypeList);

	public String returnWaterAvailabilityFromHUCs(String filterField, String filterValue, int minYear, int minMonth, int maxYear,
			int maxMonth);

	public String getSummaryWithin(String filterField1, String filterValue1, String filterField2, int minYear,
			int minMonth, int maxYear, int maxMonth, String fuelTypes, String[] fuelTypeList);

	public String getFutureSummaryWithin(String caseModel, String filterField1, String filterValue1, String filterField2, int minYear,
			int minMonth, int maxYear, int maxMonth, String fuelTypes, String[] fuelTypeList);
	
	public String getAllFacilities(String filterField, String filterValue);

	public String processWaterAvailabilityFile(String fileName, int startYear, int endYear);

	public String getFacility(String filterField, String filterValue,
			String matchLevel, int minYear, int maxYear);

}
