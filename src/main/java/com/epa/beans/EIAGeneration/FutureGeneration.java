package com.epa.beans.EIAGeneration;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FutureGeneration {
	@JsonProperty("series_id") String series_id;
	
	@JsonProperty("name") String name;
	
	@JsonProperty("units") String units;
	
	@JsonProperty("start") String startDate;
	
	@JsonProperty("end") String endDate;

	String genData;
	
	@JsonProperty("data") String data[][];
}
