DECLARE @ConstraintName nvarchar(200);
DECLARE @Sql nvarchar(max);

SELECT @ConstraintName = cc.name
FROM sys.check_constraints cc
INNER JOIN sys.columns c
    ON c.object_id = cc.parent_object_id
WHERE cc.parent_object_id = OBJECT_ID('dbo.Pages')
  AND c.name = 'Rotation'
  AND cc.definition LIKE '%Rotation%';

IF @ConstraintName IS NOT NULL
BEGIN
    SET @Sql = 'ALTER TABLE dbo.Pages DROP CONSTRAINT ' + QUOTENAME(@ConstraintName);
    EXEC sp_executesql @Sql;
END
GO

ALTER TABLE dbo.Pages
ADD CONSTRAINT CK_Pages_Rotation_0_359
CHECK (Rotation >= 0 AND Rotation <= 359);
GO
