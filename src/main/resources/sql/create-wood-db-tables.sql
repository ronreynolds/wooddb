-- create all the tables in the wood_db; delete them if they already exist
-- note, the default storage engine is already InnoDB so "engine=InnoDB" in create-table query is redundant
use wood_db;

-- only slightly cooked import table; consumed to populate all other tables by other ETL processes
drop table if exists wood_yaml_import;
create table wood_yaml_import (
    latinBinomial           varchar(500) unique not null comment 'YAML map key (so must be unique)',
    importDate              datetime not null default current_timestamp,
    sourceJson              text not null comment 'JSON from YAML data for the wood species',
    -- extracted fields
    pageUrl                 varchar(500),
    imageUrl                varchar(500),
    commonName              varchar(100),
    binomial                varchar(100),
    commonNamesTsv          varchar(1000),
    distribution            varchar(200),
    treeSize                varchar(200),
    colorAndAppearance      varchar(1000),
    grainAndTexture         varchar(1000),
    rotResistance           varchar(1000),
    workability             varchar(1000),
    odor                    varchar(1000),
    allergiesAndToxicity    varchar(1000),
    pricingAndAvailability  varchar(1000),
    sustainability          varchar(1000),
    commonUses              varchar(1000),
    comments                varchar(1000),
    endGrainImage           varchar(500),
    endGrain10xImage        varchar(500),
    edgeGrainImage          varchar(500),
    averageDryWeight        varchar(100),
    specificGravity         varchar(100),
    jankaHardness           varchar(100),
    ruptureModulus          varchar(100),
    elasticModulus          varchar(100),
    crushingStrength        varchar(100),
    shrinkage               varchar(100),
    porosity                varchar(1000),
    arrangement             varchar(1000),
    vessels                 varchar(1000),
    parenchyma              varchar(1000),
    rays                    varchar(1000),
    lookalikes              varchar(1000),
    notes                   varchar(1000),
    -- bookkeeping
    imported                boolean not null default false,
    -- indexes and constraints
    primary key (latinBinomial)
);

-- TODO - create derivative tables with better normalization
-- e.g.,
-- species, common_name, species_common_name (rel), distribution, image (to externalize BLOBs), material_props,
-- vessel_porosity (enum), vessel_arrangement (enum), parenchyma_* (enum), ray_width (enum), ...

-- information about a species group
create table taxon (
    id          int primary key auto_increment,
    rank        enum('family','genus','species') not null,
    name        varchar(255) character set latin1 not null,
    parentId    int null references taxon(id),
    acceptedName varchar(255) character set latin1 null,  -- for synonyms at any rank
    unique key uq_taxon (name)
);
-- information about a single wood species
create table species (
    id              bigint unsigned not null auto_increment,
    latinBinomial   varchar(255) unique not null,
    primary key (id)
);

-- other names by which a species is known; trade or common name or both; most likely regional
create table species_alias (
    id          bigint unsigned not null auto_increment,
    speciesId   bigint unsigned not null,
    name        varchar(255)    not null,
    type        varchar(100)    not null,
    -- indexes and constraints
    primary key (id),
    key idx_name (name),
    key idx_species (speciesId),
    constraint fk_names_species foreign key (speciesId) references species(id) on delete cascade
);


create table species_distribution (
    id          bigint unsigned not null auto_increment,
    speciesId   bigint unsigned not null,
    distribution varchar(255) not null,
    -- indexes and constraints
    primary key (id),
    key idx_distribution (distribution),
    key idx_species (speciesId),
    constraint fk_distribution_species foreign key (speciesId) references species(id) on delete cascade
);
