-- create all the tables in the wood_db; delete them if they already exist
-- note, the default storage engine is already InnoDB so "engine=InnoDB" in create-table query is redundant
use wood_db;

-- only slightly cooked import table; consumed to populate all other tables by other ETL processes
drop table if exists wood_yaml_import;
create table wood_yaml_import (
    id                      int auto_increment primary key,
    latinBinomial           varchar(500) not null comment 'YAML map key (but not unique)',
    importDate              datetime not null default current_timestamp,
    sourceJson              text not null comment 'JSON from YAML data for the wood species',
    -- extracted fields
    woodDatabaseUrl         varchar(500),
    imageUrl                varchar(500),
    commonName              varchar(100),
    binomial                varchar(100),
    commonNamesJson         varchar(1000) check (json_valid(commonNamesJson)),
    distribution            varchar(200),
    treeSize                varchar(200),
    colorAndAppearance      varchar(1000),
    grainAndTexture         varchar(1000),
    rotResistance           varchar(1000),
    workability             varchar(1000),
    odor                    varchar(1000),
    allergiesAndToxicity    varchar(2000),
    pricingAndAvailability  varchar(1000),
    sustainability          varchar(1000),
    commonUses              varchar(1000),
    comments                varchar(2000),
    endGrainImage           varchar(500),
    endGrain10xImage        varchar(500),
    edgeGrainImage          varchar(500),
    averageDryWeight        varchar(100),
    specificGravity         varchar(100),
    jankaHardness           varchar(100),
    ruptureModulus          varchar(100),
    elasticModulus          varchar(100),
    crushingStrength        varchar(100),
    shrinkage               varchar(200),
    porosity                varchar(1000),
    arrangement             varchar(1000),
    vessels                 varchar(1000),
    parenchyma              varchar(1000),
    rays                    varchar(1000),
    lookalikes              varchar(1000),
    notes                   varchar(1000),
    -- bookkeeping
    processed               boolean not null default false
);

-- TODO - create derivative tables with better normalization
-- e.g.,
-- species, common_name, species_common_name (rel), distribution, image (to externalize BLOBs), material_props,
-- vessel_porosity (enum), vessel_arrangement (enum), parenchyma_* (enum), ray_width (enum), ...

-- information about a species group
create table taxon (
    id          int auto_increment primary key,
    rank        enum('family','genus','species') not null,
    name        varchar(255) not null,
    parentId    int null references taxon(id),
    acceptedName varchar(255) null,  -- for synonyms at any rank
    unique key uq_taxon (name)
);
-- information about a single wood species
create table species (
    id              bigint unsigned not null auto_increment primary key,
    latinBinomial   varchar(255) unique not null
);

-- other names by which a species is known; trade or common name or both; most likely regional
create table species_alias (
    id          bigint unsigned not null auto_increment primary key,
    speciesId   bigint unsigned not null,
    name        varchar(255)    not null,
    type        varchar(100)    not null,
    -- indexes and constraints
    key idx_name (name),
    key idx_species (speciesId),
    constraint fk_names_species foreign key (speciesId) references species(id) on delete cascade
);

create table species_distribution (
    id          bigint unsigned not null auto_increment primary key,
    speciesId   bigint unsigned not null,
    distribution varchar(255) not null,
    -- indexes and constraints
    key idx_distribution (distribution),
    key idx_species (speciesId),
    constraint fk_distribution_species foreign key (speciesId) references species(id) on delete cascade
);

create table porosity (
    id int auto_increment primary key,
    name char(20) not null
);
insert into porosity (name) values ('diffuse-porous'), ('ring-porous'), ('semi-ring-porous');

create table vessel_arrangement (
    id int auto_increment primary key,
    name char(20) not null
);
insert into vessel_arrangement (name) 
    values ('chains'), ('clusters'), ('dendritic'), ('diagonal/radial'), ('exclusively solitary'), ('radial multiples'), ('solitary and radial multiples'), ('tangential bands');

create table vessel_size (
    id int auto_increment primary key,
    name char(20) not null
);
insert into vessel_size (name) values ('small'), ('medium'), ('large'), ('very large');

create table vessel_frequency (
    id int auto_increment primary key,
    name char(20) not null
);
insert into vessel_frequency (name) values ('very few'), ('few'), ('moderately numerous'), ('numerous'), ('very numerous');

create table vessel_contents (
    id int auto_increment primary key,
    name char(20) not null
);
insert into vessel_contents (name) values ('dark-colored deposits'), ('light-colored deposits'), ('reddish deposits'), ('reddish/brown deposits'), ('tyloses');

create table parenchyma (
    id int auto_increment primary key,
    name char(20) not null
);
insert into parenchyma (name) 
    values ('diffuse-in-aggregates'), ('vasicentric'), ('winged'), ('lozenge'), ('confluent'), ('unilateral'), ('banded'), ('marginal'), ('reticulate'), ('scalariform');

create table ray_width (
    id int auto_increment primary key,
    name char(20) not null
);
insert into ray_width (name) values ('aggregate rays present'), ('medium'), ('narrow'), ('noded rays present'), ('very wide'), ('wide');

create table ray_spacing (
    id int auto_increment primary key,
    name char(20) not null
);
insert into ray_spacing (name) values ('wide'), ('normal'), ('close');

create table misc_property (
    id int auto_increment primary key,
    name char(20) not null
);
insert into misc_property (name) values ('distinctive odor'), ('froth test positive'), ('uv fluorescent');
