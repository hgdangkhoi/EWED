USE [EPA]
GO
/****** Object:  StoredProcedure [dbo].[getEmissionsNew]    Script Date: 10/23/2019 12:30:25 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

ALTER PROCEDURE [dbo].[getEmissionsNew]
	@pgmSysId varchar(30),
	@emMonth int,
	@emYear int,
	@emAmount float(5) output,
	@derived bit output
AS
	--declare @regId as BIGINT;
	declare @emCoeff as float(5);
	declare @genYearMonth as float(5);
	declare @minYear as int;
	declare @maxYear as int;
	
BEGIN	
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT ON;

	(select @genYearMonth=genData from generation where plantCode = @pgmSysId
	and genYear = @emyear and genMonth = @emMonth);
	
	SET @emCoeff = (select emPerGen from emissionsCoeffNew where dataYear = @emYear and pgmSysId = @pgmSysId)
	SET @derived = 0;

	IF ((not EXISTS (select emPerGen from emissionsCoeffNew where dataYear = @emYear and pgmSysId = @pgmSysId)) or @emCoeff is null or @emCoeff = 0)
	begin
		set @derived = 1;
		set @minYear = (select top 1 dataYear from emissionsCoeffNew where pgmSysId = @pgmSysId order by dataYear)
		set @maxYear = (select top 1 dataYear from emissionsCoeffNew where pgmSysId = @pgmSysId order by dataYear desc)
		print @minYear
		print @maxYear	
		
		if (@emYear <= @minYear) begin
			set @emCoeff = (select emPerGen from emissionsCoeffNew where pgmSysId = @pgmSysId and dataYear = @minYear); 
			end
		else begin
			set @emCoeff = (select emPerGen from emissionsCoeffNew where pgmSysId = @pgmSysId and dataYear = @maxYear); 
			end
	end 
	print @emCoeff
	print @genYearMonth
	set @emAmount = (@emCoeff * @genYearMonth);
END
