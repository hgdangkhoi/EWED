USE [EPA]
GO
/****** Object:  StoredProcedure [dbo].[fillEmissionsMonthlyNew]    Script Date: 10/23/2019 12:30:48 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		Tejaswini Bhorkar
-- Create date: 9/5/2019
-- Description:	Converts the raw yearly emissions to monthly emission using emissionCoeff table (SP-getEmissionsNew)
--				The procedure will convert emissions only if generation data is available for the plantcode-month-year
--				It will populate emissionsMonthlyNew table only if the record doesn't already exists
-- =============================================

ALTER PROCEDURE [dbo].[fillEmissionsMonthlyNew] 
	@startYear int, @endYear int
AS 

	SET NOCOUNT ON;

	DECLARE @plantCode varchar(30), @month int, @year int;

	set @year = @startYear; set @month = 1;

	DECLARE emissionsCursor CURSOR FOR
	select distinct ORISCode from emPerYearNew where ORISCode in (select plantCode from generation)

	OPEN emissionsCursor;

	FETCH NEXT FROM emissionsCursor INTO @plantCode;
	
	DECLARE	@emAmount real, @derived bit;

	WHILE @@FETCH_STATUS = 0
	BEGIN
		WHILE @year <= @endYear BEGIN
			SET @month = 1;
			WHILE @month <= 12 BEGIN
			-- check if generation available
			IF exists (select 1 from generation 
											where (plantCode=@plantCode and genYear=@year and genMonth=@month))
			BEGIN

				EXEC [dbo].[getEmissionsNew]
					@pgmSysId = @plantCode,
					@emMonth = @month,
					@emYear = @year,
					@emAmount = @emAmount OUTPUT,
					@derived = @derived OUTPUT

					-- check if emissions is present
					IF not exists (select 1 from emissionsMonthlyNew 
											where (plantCode=@plantCode and emYear=@year and emMonth=@month))
					BEGIN

						--SELECT	@regId, @year, @month, @emAmount as N'@emAmount', @derived as N'@derived'
						INSERT INTO emissionsMonthlyNew VALUES (@plantCode, @year, @month, @emAmount, @derived);
					END
			END
			SET @month = @month + 1;
			
			END
			SET @year = @year + 1;
		END
		FETCH NEXT FROM emissionsCursor INTO @plantCode;
		SET @year = @startYear;
	END   
	CLOSE emissionsCursor;  
	DEALLOCATE emissionsCursor; 
