package com.epa.dataCollector;

import com.epa.beans.EIAGeneration.GenerationSeries;

public interface CollectionApiService {

	public String initiateCollectionImpl(String dbName, String returnFormat, String rowStart, String rowEnd,
			String filterField, String filterValue,  boolean clearAndAdd );
	
	public GenerationSeries getGenerationData(String plantCode);
	
	public GenerationSeries getGenerationDataEmmCode(String caseDescription, String fuelMover, String emmCode);
	
	public String getGreenhouseGasInfo(String gasId);
	
	public String getData();
	
	public boolean clearLists();
	
	public String getAllGeneration();
	
	public String getMissingGeneration();
	
	public String getFutureGeneration();
	
	public String getGenerationFromFile();
	
	public String getFacilityfromGen();
	
	public String getEmissions();
	
	public String getAllDominantType(int startYear, int endYear);
	
	public String updateDominantPlantType(int startYear, int endYear);
}
