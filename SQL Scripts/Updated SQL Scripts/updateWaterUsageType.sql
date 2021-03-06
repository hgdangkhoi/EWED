USE [EPA]
GO
/****** Object:  StoredProcedure [dbo].[updateWaterUsageType]    Script Date: 10/23/2019 12:33:55 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		Tejaswini Bhorkar
-- Create date: 8/28/2019
-- Description:	Insert of Update emissionCoeffTable for newly fetched emissions values
-- =============================================
ALTER PROCEDURE [dbo].[updateWaterUsageType] 
@startYear int, @endYear int
AS
BEGIN
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT ON;

	DECLARE @plantCode int,
		@year int,
		@waterType varchar(50),
		@waterSource varchar(50),
		@waterSourceName varchar(50);
	
	DECLARE facilityCursor CURSOR FOR

	SELECT distinct pgmSysId from 
	facility860C

	OPEN facilityCursor;

	FETCH NEXT FROM facilityCursor INTO @plantCode;

	WHILE @@FETCH_STATUS = 0
	BEGIN
		IF not exists (select 1 from WaterUsageType where (plantCode=@plantCode))
			BEGIN
			SET @year = @startYear;
			WHILE @year <= @endYear 
			BEGIN
				SET @waterType = 'Fresh';
				SET @waterSource  = 'Unknown';
				SET @waterSourceName  = 'Unknown';

				INSERT INTO WaterUsageType VALUES (@plantCode, @year, null, @waterType, @waterSource, @waterSourceName);
				SET @year = @year + 1;
			END
		END
	FETCH NEXT FROM facilityCursor INTO @plantCode;
	END   
	CLOSE facilityCursor;  
	DEALLOCATE facilityCursor; 
END
