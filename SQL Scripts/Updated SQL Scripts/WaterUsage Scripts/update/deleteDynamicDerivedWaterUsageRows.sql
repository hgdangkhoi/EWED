USE [EPA]
GO
/****** Object:  StoredProcedure [dbo].[deleteDynamicDerivedWaterUsageRows]    Script Date: 10/23/2019 12:29:55 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		Tejaswini Bhorkar
-- Create date: 9/20/2019
-- Description:	Delete derived rows for the available year
-- =============================================
ALTER PROCEDURE [dbo].[deleteDynamicDerivedWaterUsageRows] 
	@startYear int, @endYear int
AS
BEGIN
	SET NOCOUNT ON;
	
	DELETE FROM waterUsageComplete 
	where ((usageYear BETWEEN @startYear and @endYear) and (derived=1));
END
