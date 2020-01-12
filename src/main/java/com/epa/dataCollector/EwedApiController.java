package com.epa.dataCollector;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/ewedService/")
@RestController
public class EwedApiController {

	@Autowired
	EwedApiService apiService;
	
	@RequestMapping("/test")
	public String test() {
		System.out.println("Reached ewed api test");
		return "Reached ewed api test";
	}
	
	/**
	 * REQUIRED PARAMS - 
	 * @param filterField
	 * @param filterValue
	 * @param minYear
	 * @param maxYear
	 * 
	 * This function uses the filter field to match the filter value in the 
	 * facility info table and returns the facility information json along
	 * with a summary as a json of the EWEDataReturn class.
	 * 
	 * @return Facility Information and Summary JSON
	 */
	@RequestMapping("/getFacility/{filterField}/{filterValue}/{minYear}/{minMonth}/{maxYear}/{maxMonth}")
	public String getFacility(
			@PathVariable(value="filterField") String filterField, 
			@PathVariable(value="filterValue") String filterValue, 
			@PathVariable(value="minYear") int minYear, @PathVariable(value="minMonth") int minMonth, 
			@PathVariable(value="maxYear") int maxYear, @PathVariable(value="maxMonth") int maxMonth) {
		return apiService.getFacility(filterField ,filterValue, minYear, minMonth, maxYear, maxMonth);
	}
	
	@RequestMapping("/getFutureData/getFacility/{caseModel}/{filterField}/{filterValue}/{minYear}/{minMonth}/{maxYear}/{maxMonth}")
	public String getFutureFacility(
			@PathVariable(value="caseModel") String caseModel,
			@PathVariable(value="filterField") String filterField, 
			@PathVariable(value="filterValue") String filterValue, 
			@PathVariable(value="minYear") int minYear, @PathVariable(value="minMonth") int minMonth, 
			@PathVariable(value="maxYear") int maxYear, @PathVariable(value="maxMonth") int maxMonth) {
		return apiService.getFutureFacility(caseModel, filterField ,filterValue, minYear, minMonth, maxYear, maxMonth);
	}
	
	/**
	 * REQUIRED PARAMS - 
	 * @param filterField
	 * @param filterValue
	 * @param matchLevel (Extra from above)
	 * @param minYear
	 * @param maxYear
	 * 
	 * This works exactly like the above function but having this method ensures
	 * that a match level is an optional parameter without having to use request
	 * parameters. This function ensures that a match level is used.
	 * 
	 * @return Facility Information and Summary JSON
	 */
	@RequestMapping("/getFacility/{filterField}/{filterValue}/{matchLevel}/{minYear}/{maxYear}")
	public String getFacilityWithMatchLevel(@PathVariable(value="filterField") String filterField, @PathVariable(value="filterValue") String filterValue, 
			@PathVariable(value="matchLevel") String matchLevel,
			@PathVariable(value="minYear") int minYear, @PathVariable(value="maxYear") int maxYear) {
		return apiService.getFacility(filterField ,filterValue, matchLevel, minYear, maxYear);
	}
	
	@RequestMapping("/getAllFacilities/{filterField}/{filterValue}")
	public String getAllFacilities(@PathVariable(value="filterField") String filterField, @PathVariable(value="filterValue") String filterValue) {
		return apiService.getAllFacilities(filterField ,filterValue);
	}
	
	@RequestMapping("/getFacilityData/{filterField}/{filterValue}/{minYear}/{minMonth}/{maxYear}/{maxMonth}/{fuelTypes}/{fuelTypeList}")
	public String getFacilityData(
			@PathVariable(value="filterField") String filterField, 
			@PathVariable(value="filterValue") String filterValue, 
			@PathVariable(value="minYear") int minYear, @PathVariable(value="minMonth") int minMonth, 
			@PathVariable(value="maxYear") int maxYear, @PathVariable(value="maxMonth") int maxMonth,
			@PathVariable(value="fuelTypes") String fuelTypes,
			@PathVariable(value="fuelTypeList") String[] fuelTypeList) {
		return apiService.getFacilityData(filterField ,filterValue, minYear, minMonth, maxYear, maxMonth, fuelTypes, fuelTypeList);
	}
	
	@RequestMapping("/getMonthWiseSummary/{filterField}/{filterValue}/{minYear}/{minMonth}/{maxYear}/{maxMonth}/{fuelTypes}/{fuelTypeList}")
	public String getMonthWiseSummary(
			@PathVariable(value="filterField") String filterField, 
			@PathVariable(value="filterValue") String filterValue, 
			@PathVariable(value="minYear") int minYear, @PathVariable(value="minMonth") int minMonth, 
			@PathVariable(value="maxYear") int maxYear, @PathVariable(value="maxMonth") int maxMonth,
			@PathVariable(value="fuelTypes") String fuelTypes,
			@PathVariable(value="fuelTypeList") String[] fuelTypeList) {
		return apiService.getMonthWiseSummary(filterField ,filterValue, minYear, minMonth, maxYear, maxMonth, fuelTypes, fuelTypeList);
	}
	
	@RequestMapping("/getFutureData/getFacilityData/{caseModel}/{filterField}/{filterValue}/{minYear}/{minMonth}/{maxYear}/{maxMonth}/{fuelTypes}/{fuelTypeList}")
	public String getFutureFacilityData(
			@PathVariable(value="caseModel") String caseModel, 
			@PathVariable(value="filterField") String filterField, 
			@PathVariable(value="filterValue") String filterValue, 
			@PathVariable(value="minYear") int minYear, @PathVariable(value="minMonth") int minMonth, 
			@PathVariable(value="maxYear") int maxYear, @PathVariable(value="maxMonth") int maxMonth,
			@PathVariable(value="fuelTypes") String fuelTypes,
			@PathVariable(value="fuelTypeList") String[] fuelTypeList) {
		return apiService.getFutureFacilityData(caseModel, filterField ,filterValue, minYear, minMonth, maxYear, maxMonth, fuelTypes, fuelTypeList);
	}
	
	@RequestMapping("/getFutureData/getMonthWiseSummary/{caseModel}/{climateModel}/{filterField}/{filterValue}/{minYear}/{minMonth}/{maxYear}/{maxMonth}/{fuelTypes}/{fuelTypeList}")
	public String getFutureMonthWiseSummary(
			@PathVariable(value="caseModel") String caseModel, 
			@PathVariable(value="climateModel") String climateModel,
			@PathVariable(value="filterField") String filterField, 
			@PathVariable(value="filterValue") String filterValue, 
			@PathVariable(value="minYear") int minYear, @PathVariable(value="minMonth") int minMonth, 
			@PathVariable(value="maxYear") int maxYear, @PathVariable(value="maxMonth") int maxMonth,
			@PathVariable(value="fuelTypes") String fuelTypes,
			@PathVariable(value="fuelTypeList") String[] fuelTypeList) {
		return apiService.getFutureMonthWiseSummary(caseModel, climateModel, filterField ,filterValue, minYear, minMonth, maxYear, maxMonth, fuelTypes, fuelTypeList);
	}
	
	@RequestMapping("/defaultViewData/{filterField}/{minYear}/{minMonth}/{maxYear}/{maxMonth}/{fuelTypes}/{fuelTypeList}")
	public String defaultViewData(
			@PathVariable(value="filterField") String filterField,  
			@PathVariable(value="minYear") int minYear,
			@PathVariable(value="minMonth") int minMonth,
			@PathVariable(value="maxYear") int maxYear,
			@PathVariable(value="maxMonth") int maxMonth,
			@PathVariable(value="fuelTypes") String fuelTypes,
			@PathVariable(value="fuelTypeList") String[] fuelTypeList){
		return apiService.defaultGEWData(filterField, minYear,minMonth, maxYear, maxMonth, fuelTypes, fuelTypeList);
	}
	
	@RequestMapping("/getFutureData/defaultViewData/{caseModel}/{filterField}/{minYear}/{minMonth}/{maxYear}/{maxMonth}/{fuelTypes}/{fuelTypeList}")
	public String futureDefaultViewData(
			@PathVariable(value="caseModel") String caseModel, 
			@PathVariable(value="filterField") String filterField,  
			@PathVariable(value="minYear") int minYear,
			@PathVariable(value="minMonth") int minMonth,
			@PathVariable(value="maxYear") int maxYear,
			@PathVariable(value="maxMonth") int maxMonth,
			@PathVariable(value="fuelTypes") String fuelTypes,
			@PathVariable(value="fuelTypeList") String[] fuelTypeList){
		return apiService.futureDefaultGEWData(caseModel, filterField, minYear,minMonth, maxYear, maxMonth, fuelTypes, fuelTypeList);
	}
	
	@RequestMapping("/getWaterAvailabilityData/{filterField}/{filterValue}/{minYear}/{minMonth}/{maxYear}/{maxMonth}")
	public String getWaterAvailabilityData(
			@PathVariable(value="filterField") String filterField, 
			@PathVariable(value="filterValue") String filterValue, 
			@PathVariable(value="minYear") int minYear, @PathVariable(value="minMonth") int minMonth, 
			@PathVariable(value="maxYear") int maxYear, @PathVariable(value="maxMonth") int maxMonth) {
		return apiService.returnWaterAvailabilityFromHUCs(filterField ,filterValue, minYear, minMonth, maxYear, maxMonth);
	}
	
	@RequestMapping("/getSummaryWithin/{filterField1}/{filterValue1}/{filterField2}/{minYear}/{minMonth}/{maxYear}/{maxMonth}/{fuelTypes}/{fuelTypeList}")
	public String getHUCSummaryWithinState(
			@PathVariable(value="filterField1") String filterField1, 
			@PathVariable(value="filterValue1") String filterValue1, 
			@PathVariable(value="filterField2") String filterField2, 
			@PathVariable(value="minYear") int minYear, @PathVariable(value="minMonth") int minMonth, 
			@PathVariable(value="maxYear") int maxYear, @PathVariable(value="maxMonth") int maxMonth,
			@PathVariable(value="fuelTypes") String fuelTypes,
			@PathVariable(value="fuelTypeList") String[] fuelTypeList) {
		return apiService.getSummaryWithin(filterField1, filterValue1, filterField2, minYear, minMonth, maxYear, maxMonth, fuelTypes, fuelTypeList);
		}
	
	@RequestMapping("/getFutureData/getSummaryWithin/{caseModel}/{filterField1}/{filterValue1}/{filterField2}/{minYear}/{minMonth}/{maxYear}/{maxMonth}/{fuelTypes}/{fuelTypeList}")
	public String getFutureHUCSummaryWithinState(
			@PathVariable(value="caseModel") String caseModel,
			@PathVariable(value="filterField1") String filterField1, 
			@PathVariable(value="filterValue1") String filterValue1, 
			@PathVariable(value="filterField2") String filterField2, 
			@PathVariable(value="minYear") int minYear, @PathVariable(value="minMonth") int minMonth, 
			@PathVariable(value="maxYear") int maxYear, @PathVariable(value="maxMonth") int maxMonth,
			@PathVariable(value="fuelTypes") String fuelTypes,
			@PathVariable(value="fuelTypeList") String[] fuelTypeList) {
		return apiService.getFutureSummaryWithin(caseModel, filterField1, filterValue1, filterField2, minYear, minMonth, maxYear, maxMonth, fuelTypes, fuelTypeList);
		}
	
	@RequestMapping("/processWaterAvailabilityFile/{fileName}/{startYear}/{endYear}")
	public String processWaterAvailabilityFile(
			@PathVariable(value="fileName") String fileName, 
			@PathVariable(value="startYear") int startYear, 
			@PathVariable(value="endYear") int endYear){
		return apiService.processWaterAvailabilityFile(fileName, startYear, endYear);
		}
}
