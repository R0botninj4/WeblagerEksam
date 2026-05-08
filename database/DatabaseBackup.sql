create table Clients
(
    Id        uniqueidentifier default newid() not null
        primary key,
    Name      nvarchar(150)                    not null,
    Code      nvarchar(50)
        unique,
    CreatedAt datetime2        default getdate()
)
    go

create table Profiles
(
    Id               uniqueidentifier default newid() not null
        primary key,
    ClientId         uniqueidentifier                 not null
        constraint FK_Profiles_Clients
            references Clients,
    Name             nvarchar(150)                    not null,
    BarcodeSplitRule nvarchar(255),
    MetadataSchema   nvarchar(max),
    CreatedAt        datetime2        default getdate()
)
    go

create table Boxes
(
    Id        uniqueidentifier default newid() not null
        primary key,
    ClientId  uniqueidentifier                 not null
        constraint FK_Boxes_Clients
            references Clients,
    ProfileId uniqueidentifier
        constraint FK_Boxes_Profiles
            references Profiles,
    BoxNumber nvarchar(100)                    not null,
    Label     nvarchar(150),
    Status    nvarchar(50)     default 'READY'
        check ([Status] = 'ARCHIVED' OR [Status] = 'COMPLETED' OR [Status] = 'WAITING_FOR_QA' OR
               [Status] = 'IN_PROGRESS' OR [Status] = 'READY'),
    CreatedAt datetime2        default getdate()
)
    go

create index IX_Boxes_ClientId
    on Boxes (ClientId)
    go

create index IX_Boxes_ProfileId
    on Boxes (ProfileId)
    go

create table Documents
(
    Id             uniqueidentifier default newid() not null
        primary key,
    BoxId          uniqueidentifier                 not null
        constraint FK_Documents_Boxes
            references Boxes
            on delete cascade,
    DocumentNumber int                              not null,
    BarcodeValue   nvarchar(255),
    Status         nvarchar(50)     default 'SCANNED'
        check ([Status] = 'REJECTED' OR [Status] = 'QA_APPROVED' OR [Status] = 'READY_FOR_QA' OR
               [Status] = 'METADATA_PENDING' OR [Status] = 'SCANNED'),
    CreatedAt      datetime2        default getdate()
)
    go

create table DocumentMetadata
(
    Id         uniqueidentifier default newid() not null
        primary key,
    DocumentId uniqueidentifier                 not null
        constraint FK_DocumentMetadata_Documents
            references Documents
            on delete cascade,
    FieldName  nvarchar(150)                    not null,
    FieldValue nvarchar(max)
)
    go

create index IX_Documents_BoxId
    on Documents (BoxId)
    go

create table Pages
(
    Id                 uniqueidentifier default newid() not null
        primary key,
    DocumentId         uniqueidentifier                 not null
        constraint FK_Pages_Documents
            references Documents
            on delete cascade,
    ReferenceScanOrder int                              not null,
    UiOrder            int                              not null,
    FileName           nvarchar(255),
    MimeType           nvarchar(50)     default 'image/tiff',
    ImageData          varbinary(max)                   not null,
    FileSize           bigint,
    Checksum           nvarchar(128),
    Rotation           int              default 0
        check ([Rotation] = 270 OR [Rotation] = 180 OR [Rotation] = 90 OR [Rotation] = 0),
    Width              int,
    Height             int,
    IsBarcodePage      bit              default 0,
    CreatedAt          datetime2        default getdate()
)
    go

create index IX_Pages_DocumentId
    on Pages (DocumentId)
    go

create index IX_Profiles_ClientId
    on Profiles (ClientId)
    go

create table Roles
(
    Id   uniqueidentifier default newid() not null
        primary key,
    Name nvarchar(50)                     not null
        unique
)
    go

create table Users
(
    Id           uniqueidentifier default newid() not null
        primary key,
    Username     nvarchar(100)                    not null
        unique,
    PasswordHash nvarchar(500)                    not null,
    FullName     nvarchar(150),
    RoleId       uniqueidentifier                 not null
        constraint FK_Users_Roles
            references Roles,
    IsActive     bit              default 1,
    CreatedAt    datetime2        default getdate(),
    LastLogin    datetime2
)
    go

create table Logs
(
    Id        uniqueidentifier default newid() not null
        primary key,
    UserId    uniqueidentifier
        constraint FK_Logs_Users
            references Users,
    Action    nvarchar(150)                    not null,
    TableName nvarchar(100),
    RecordId  uniqueidentifier,
    OldValue  nvarchar(max),
    NewValue  nvarchar(max),
    CreatedAt datetime2        default getdate()
)
    go

create index IX_Logs_UserId
    on Logs (UserId)
    go

create index IX_Logs_CreatedAt
    on Logs (CreatedAt)
    go

create table UserBoxes
(
    UserId     uniqueidentifier not null
        constraint FK_UserBoxes_Users
            references Users
            on delete cascade,
    BoxId      uniqueidentifier not null
        constraint FK_UserBoxes_Boxes
            references Boxes
            on delete cascade,
    AssignedAt datetime2 default getdate(),
    constraint PK_UserBoxes
        primary key (UserId, BoxId)
)
    go

create index IX_Users_Username
    on Users (Username)
    go

CREATE PROCEDURE dbo.sp_CreateLog
    @UserId UNIQUEIDENTIFIER = NULL,
    @Action NVARCHAR(150),
    @TableName NVARCHAR(100) = NULL,
    @RecordId UNIQUEIDENTIFIER = NULL,
    @OldValue NVARCHAR(MAX) = NULL,
    @NewValue NVARCHAR(MAX) = NULL
AS
BEGIN
    SET NOCOUNT ON;

INSERT INTO dbo.Logs
(
    UserId,
    Action,
    TableName,
    RecordId,
    OldValue,
    NewValue
)
VALUES
    (
        @UserId,
        @Action,
        @TableName,
        @RecordId,
        @OldValue,
        @NewValue
    );
END;
go

