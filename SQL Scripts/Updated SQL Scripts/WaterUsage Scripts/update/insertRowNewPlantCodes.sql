USE [EPA]
GO
/****** Object:  StoredProcedure [dbo].[insertRowNewPlantCodes]    Script Date: 10/23/2019 12:32:30 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		Tejaswini Bhorkar
-- Create date: 9/3/2019
-- Description:	Used after importing the new excel file into a staging table (@from_table_name param).
--				Inserts new rows from the staging table to the actual table (@insert_table_name param) and 
--				deletes the staging table 
-- =============================================
ALTER PROCEDURE [dbo].[insertRowNewPlantCodes]
	@insert_table_name varchar(50),
	@from_table_name varchar(50),
	@Drop_table varchar(3)
AS
BEGIN
	 
	SET NOCOUNT ON;
	DECLARE @Insert NVARCHAR(MAX);
	DECLARE @Drop NVARCHAR(MAX);



	SET @Insert = N' INSERT INTO ' 
            + QUOTENAME(@insert_table_name)
          + N' SELECT distinct plantCode FROM ' + QUOTENAME(@from_table_name)
		  + N' EXCEPT '
		  + N' SELECT * FROM ' + QUOTENAME(@insert_table_name)
	EXECUTE sp_executesql @Insert
	
	IF(@Drop_table = 'Y')
	BEGIN
		SET @Drop = N' DROP TABLE ' 
				+ QUOTENAME(@from_table_name)
		EXECUTE sp_executesql @Drop
	END
END
