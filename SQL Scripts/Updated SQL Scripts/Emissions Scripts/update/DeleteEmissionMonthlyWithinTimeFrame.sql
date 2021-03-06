USE [EPA]
GO
/****** Object:  StoredProcedure [dbo].[DeleteEmissionMonthlyWithinTimeFrame]    Script Date: 10/29/2019 6:36:45 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		Tejaswini Bhorkar
-- Create date: 8/28/19
-- Description:	This procedure checks for derived plantcode and year from emissionsMonthlyNew table 
--				in emissionCoeffNew table. The Coeff for that pair indicates that emission value 
--				is available and we don't need to derive it.
-- =============================================
ALTER   PROCEDURE [dbo].[DeleteEmissionMonthlyWithinTimeFrame]
	@startYear int, @endYear int
AS
BEGIN
	SET NOCOUNT ON;
	DECLARE @plantCode varchar(30), @emYear int;

	DECLARE emissionsCursor CURSOR FOR
	select distinct plantCode, emYear from emissionsMonthlyNew where derived=1 and emYear between @startYear and @endYear 

	OPEN emissionsCursor;
	FETCH NEXT FROM emissionsCursor INTO @plantCode, @emYear;

	WHILE @@FETCH_STATUS = 0
	BEGIN
		IF exists (select 1 from emissionsCoeffNew where (pgmSysId=@plantCode and dataYear=@emYear))
		BEGIN
		-- delete all the rows for it cz it shouldnt be derived
			DELETE FROM emissionsMonthlyNew where (plantCode=@plantCode and emYear=@emYear);
		END
	FETCH NEXT FROM emissionsCursor INTO @plantCode, @emYear ;
	END   
	CLOSE emissionsCursor;  
	DEALLOCATE emissionsCursor; 
END
