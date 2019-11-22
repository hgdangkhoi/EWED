package com.epa.beans.SummaryData;

import javax.persistence.Column;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

@Component
@JsonAutoDetect(fieldVisibility=JsonAutoDetect.Visibility.ANY, getterVisibility=JsonAutoDetect.Visibility.NONE,
setterVisibility=JsonAutoDetect.Visibility.NONE, creatorVisibility=JsonAutoDetect.Visibility.NONE)
public class FacilityWithSummaryData {

	@JsonProperty("PGM_SYS_ID") String plantCode;
	
	@JsonProperty("PRIMARY_NAME") String primaryName;
	
	@JsonProperty("NAICS_CODE") String naicsCode;
	
	@JsonProperty("REGISTRY_ID") String registryId;

	@JsonProperty("LOCATION_ADDRESS") String facAddr;
	
	@JsonProperty("CITY_NAME") String cityName;
	
	@JsonProperty("STATE_NAME") String stateName;

	@JsonProperty("POSTAL_CODE") String postalCode;
	
	@JsonProperty("LATITUDE83") String latitude;
	
	@JsonProperty("LONGITUDE83") String longitude;
	
	@JsonProperty("GEOID") String GEOID;
	
	@JsonProperty("CountyState1") String CountyState1;
	
	@JsonProperty("CountyState2") String CountyState2;
	
	@JsonProperty("HUC8Code") String HUC8Code;
	
	@JsonProperty("HUC8Name") String HUC8Name;
	
	@JsonProperty("HUC8Acres") String HUC8Acres;
	
	private String WaterConsumptionSummary;
	private String EmissionSummary;
	private String GenerationSummary;
	private String WaterWithdrawalSummary;
	
	public String getPlantCode() {
		return plantCode;
	}
	public void setPlantCode(String plantCode) {
		this.plantCode = plantCode;
	}
	public String getPrimaryName() {
		return primaryName;
	}
	public void setPrimaryName(String primaryName) {
		this.primaryName = primaryName;
	}
	public String getNaicsCode() {
		return naicsCode;
	}
	public void setNaicsCode(String naicsCode) {
		this.naicsCode = naicsCode;
	}
	public String getRegistryId() {
		return registryId;
	}
	public void setRegistryId(String registryId) {
		this.registryId = registryId;
	}
	public String getFacAddr() {
		return facAddr;
	}
	public void setFacAddr(String facAddr) {
		this.facAddr = facAddr;
	}
	public String getCityName() {
		return cityName;
	}
	public void setCityName(String cityName) {
		this.cityName = cityName;
	}
	public String getStateName() {
		return stateName;
	}
	public void setStateName(String stateName) {
		this.stateName = stateName;
	}
	public String getPostalCode() {
		return postalCode;
	}
	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}
	public String getLatitude() {
		return latitude;
	}
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	public String getLongitude() {
		return longitude;
	}
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
	public String getGEOID() {
		return GEOID;
	}
	public void setGEOID(String gEOID) {
		GEOID = gEOID;
	}
	public String getCountyState1() {
		return CountyState1;
	}
	public void setCountyState1(String countyState1) {
		CountyState1 = countyState1;
	}
	public String getCountyState2() {
		return CountyState2;
	}
	public void setCountyState2(String countyState2) {
		CountyState2 = countyState2;
	}
	public String getHUC8Code() {
		return HUC8Code;
	}
	public void setHUC8Code(String hUC8Code) {
		HUC8Code = hUC8Code;
	}
	public String getHUC8Name() {
		return HUC8Name;
	}
	public void setHUC8Name(String hUC8Name) {
		HUC8Name = hUC8Name;
	}
	public String getHUC8Acres() {
		return HUC8Acres;
	}
	public void setHUC8Acres(String hUC8Acres) {
		HUC8Acres = hUC8Acres;
	}
	public String getWaterConsumptionSummary() {
		return WaterConsumptionSummary;
	}
	public void setWaterConsumptionSummary(String waterConsumptionSummary) {
		WaterConsumptionSummary = waterConsumptionSummary;
	}
	public String getEmissionSummary() {
		return EmissionSummary;
	}
	public void setEmissionSummary(String emissionSummary) {
		EmissionSummary = emissionSummary;
	}
	public String getGenerationSummary() {
		return GenerationSummary;
	}
	public void setGenerationSummary(String generationSummary) {
		GenerationSummary = generationSummary;
	}
	public String getWaterWithdrawalSummary() {
		return WaterWithdrawalSummary;
	}
	public void setWaterWithdrawalSummary(String waterWithdrawalSummary) {
		WaterWithdrawalSummary = waterWithdrawalSummary;
	}
	
	public FacilityWithSummaryData(String plantCode, String primaryName,
			String naicsCode, String registryId, String facAddr,
			String cityName, String stateName, String postalCode,
			String latitude, String longitude, String gEOID,
			String countyState1, String countyState2, String hUC8Code,
			String hUC8Name, String hUC8Acres, String waterConsumptionSummary,
			String emissionSummary, String generationSummary,
			String waterWithdrawalSummary) {
		super();
		this.plantCode = plantCode;
		this.primaryName = primaryName;
		this.naicsCode = naicsCode;
		this.registryId = registryId;
		this.facAddr = facAddr;
		this.cityName = cityName;
		this.stateName = stateName;
		this.postalCode = postalCode;
		this.latitude = latitude;
		this.longitude = longitude;
		GEOID = gEOID;
		CountyState1 = countyState1;
		CountyState2 = countyState2;
		HUC8Code = hUC8Code;
		HUC8Name = hUC8Name;
		HUC8Acres = hUC8Acres;
		WaterConsumptionSummary = waterConsumptionSummary;
		EmissionSummary = emissionSummary;
		GenerationSummary = generationSummary;
		WaterWithdrawalSummary = waterWithdrawalSummary;
	}
	@Override
	public String toString() {
		return "FacilityWithSummaryData [plantCode=" + plantCode
				+ ", primaryName=" + primaryName + ", naicsCode=" + naicsCode
				+ ", registryId=" + registryId + ", facAddr=" + facAddr
				+ ", cityName=" + cityName + ", stateName=" + stateName
				+ ", postalCode=" + postalCode + ", latitude=" + latitude
				+ ", longitude=" + longitude + ", GEOID=" + GEOID
				+ ", CountyState1=" + CountyState1 + ", CountyState2="
				+ CountyState2 + ", HUC8Code=" + HUC8Code + ", HUC8Name="
				+ HUC8Name + ", HUC8Acres=" + HUC8Acres
				+ ", WaterConsumptionSummary=" + WaterConsumptionSummary
				+ ", EmissionSummary=" + EmissionSummary
				+ ", GenerationSummary=" + GenerationSummary
				+ ", WaterWithdrawalSummary=" + WaterWithdrawalSummary + "]";
	}
	public FacilityWithSummaryData() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
