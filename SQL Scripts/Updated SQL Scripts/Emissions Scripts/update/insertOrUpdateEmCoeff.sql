USE [EPA]
GO
/****** Object:  StoredProcedure [dbo].[insertOrUpdateEmCoeff]    Script Date: 10/23/2019 12:31:23 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		Tejaswini Bhorkar
-- Create date: 8/28/2019
-- Description:	Insert of Update emissionCoeffTable for newly fetched emissions values
-- =============================================
ALTER PROCEDURE [dbo].[insertOrUpdateEmCoeff] 
	@year int
AS
BEGIN
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT ON;

	DECLARE @coeff float(5),
		@ORISCode int,
		@emYear int,
		@tempCoeff float(5);
	
	DECLARE emCoeffCursor CURSOR FOR

	SELECT e.ORISCode, e.emYear, (emission/generation) AS coeff 
	FROM emPerYearNew e, genPerYear g 
	WHERE e.ORISCode = g.plantCode AND e.emYear = @year AND g.genYear= @year AND generation !=0  
	ORDER BY ORISCode, emYear

	OPEN emCoeffCursor;

	FETCH NEXT FROM emCoeffCursor INTO @ORISCode, @emYear, @coeff ;

	WHILE @@FETCH_STATUS = 0
	BEGIN

		IF not exists (select 1 from emissionsCoeffNew 
											where (pgmSysId=@ORISCode and dataYear=@emYear))
			BEGIN
				INSERT INTO emissionsCoeffNew VALUES (@ORISCode, @emYear, @coeff)
				print 'inserted_row'
			END
		ELSE
			BEGIN
				Select @tempCoeff = emPerGen FROM [emissionsCoeffNew] 
									where (pgmSysId=@ORISCode and dataYear=@emYear)
				IF (@tempCoeff != @coeff)
				BEGIN
					UPDATE emissionsCoeffNew SET emPerGen = @coeff where pgmSysId = @ORISCode
					print 'updated_row'
				END
			END

	FETCH NEXT FROM emCoeffCursor INTO @ORISCode, @emYear, @coeff;
	END   
	CLOSE emCoeffCursor;  
	DEALLOCATE emCoeffCursor; 
END
