-- Notes --
-- Warning - there are no foreign key constraints on these tables to enable rapid prototyping. For an operational system, adding these would be advisable

-------------------------------------
-- Trigger function to create versioned IDs by concatenating the localid with the version
-------------------------------------
CREATE OR REPLACE FUNCTION public.set_id_tnw()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$

	BEGIN
		NEW.id := NEW.localid || '-' || NEW.version;
	RETURN NEW;
END 

$function$
;

-------------------------------------
-------------------------------------
-- core table for FT hy-n:HydroNode
-------------------------------------
-------------------------------------

CREATE TABLE public.hyn_hydronode (
	id varchar(100) NOT NULL,
	metadata varchar(100) NULL,
	localid varchar(100) NULL,
	"namespace" varchar(100) NULL,
	"version" varchar(100) NULL,
	beginlsv timestamptz NULL,
	endlsv timestamptz NULL,
	geographicalname varchar(200) NULL,
	nodecategory varchar(200) NULL,
	validfrom date NULL,
	validto date NULL,
	geomid varchar(100) NULL,
	geom geometry(POINT, 4326) NULL,
	idurl varchar(100) NULL,
	CONSTRAINT enforce_dims_geom CHECK ((st_ndims(geom) = 2)),
	CONSTRAINT enforce_geotype_geom CHECK (((geometrytype(geom) = 'POINT'::text) OR (geom IS NULL))),
	CONSTRAINT enforce_srid_geom CHECK ((st_srid(geom) = 4326)),
	CONSTRAINT hyn_hydronode_pkey PRIMARY KEY (id)
);

-- Table Triggers

create trigger id_update_hyn_hydronode before
insert or update on hyn_hydronode for each row execute procedure set_id_tnw();
	
-------------------------------------
-- network table for FT hy-n:HydroNode
-- FK is "link", links to id on hyn_hydronode
-------------------------------------	
	
CREATE TABLE public.hyn_hydronode_net (
	id serial NOT NULL,
	link varchar(100) NOT NULL,
	networklink varchar(100) NULL,
	CONSTRAINT hyn_hydronode_net_pkey PRIMARY KEY (id)
);

-------------------------------------
-------------------------------------
-- core table for FT hy-n:WatercourseLink
-------------------------------------
-------------------------------------
CREATE TABLE public.hyn_watercourselink (
	id varchar(100) NOT NULL,
	metadata varchar(100) NULL,
	localid varchar(100) NULL,
	"namespace" varchar(100) NULL,
	"version" varchar(100) NULL,
	flowdirection varchar(200) NULL,
	lengthval numeric NULL,
	lengthuom varchar(200) NULL,
	beginlsv timestamptz NULL,
	endlsv timestamptz NULL,
	geographicalname varchar(200) NULL,
	startnode varchar(200) NULL,
	endnode varchar(200) NULL,
	validfrom date NULL,
	validto date NULL,
	fict varchar(100) NULL,
	geomid varchar(100) NULL,
	centreline geometry NULL,
	idurl varchar(100) NULL,
	CONSTRAINT enforce_dims_centreline CHECK ((st_ndims(centreline) = 2)),
	CONSTRAINT enforce_srid_centreline CHECK ((st_srid(centreline) = 4326)),
	CONSTRAINT hyn_watercourselink_pkey PRIMARY KEY (id)
);

-- Table Triggers

create trigger id_update_hyn_watercourselink before
insert or update on hyn_watercourselink for each row execute procedure set_id_tnw();

-------------------------------------
-- network table for FT hy-n:WatercourseLink
-- FK is "link", links to id on hyn_watercourselink
-------------------------------------	
CREATE TABLE public.hyn_watercourselink_net (
	id serial NOT NULL,
	link varchar(100) NOT NULL,
	networklink varchar(100) NULL,
	CONSTRAINT hyn_watercourselink_net_pkey PRIMARY KEY (id)
);

-------------------------------------
-- hydroid table for FTs hy-n:HydroNode and hy-n:WatercourseLink
-- FK is "hyn_object", links to id on hyn_hydronode and hyn_watercourselink
-------------------------------------
CREATE TABLE public.hyn_hydroid (
	id serial NOT NULL,
	hyn_object varchar(100) NULL,
	hydroid varchar(100) NOT NULL,
	hydroidclass varchar(200) NULL,
	hydroidns varchar(100) NOT NULL
);

-------------------------------------
-------------------------------------
-- core table for FT tn-w:PortNode
-------------------------------------
-------------------------------------

CREATE TABLE public.tnw_portnode (
	id varchar(100) NOT NULL,
	metadata varchar(100) NULL,
	localid varchar(100) NULL,
	"namespace" varchar(100) NULL,
	"version" varchar(100) NULL,
	beginlsv timestamptz NULL,
	endlsv timestamptz NULL,
	geographicalname varchar(200) NULL,
	validfrom date NULL,
	validto date NULL,
	geomid varchar(100) NULL,
	geom geometry(POINT, 4326) NULL,
	innetwork varchar(100) NULL,
	idurl varchar(100) NULL,
	CONSTRAINT enforce_dims_geom CHECK ((st_ndims(geom) = 2)),
	CONSTRAINT enforce_geotype_geom CHECK (((geometrytype(geom) = 'POINT'::text) OR (geom IS NULL))),
	CONSTRAINT tnw_portnode_pkey PRIMARY KEY (id)
);

-- Table Triggers

create trigger id_update_tnw_portnode before
insert or update on tnw_portnode for each row execute procedure set_id_tnw();

-------------------------------------
-- network table for FTs tn-w:PortNode
-- FK is "node", links to id on tnw_portnode
-------------------------------------

CREATE TABLE public.tnw_portnode_net (
	id serial NOT NULL,
	node varchar(100) NOT NULL,
	networklink varchar(100) NULL,
	CONSTRAINT tnw_portnode_net_pkey PRIMARY KEY (id)
);

-------------------------------------
-------------------------------------
-- core table for FT tn-w:WaterwayLink
-------------------------------------
-------------------------------------

CREATE TABLE public.tnw_waterwaylink (
	id varchar(100) NOT NULL,
	metadata varchar(100) NULL,
	localid varchar(100) NULL,
	"namespace" varchar(100) NULL,
	"version" varchar(100) NULL,
	beginlsv timestamptz NULL,
	endlsv timestamptz NULL,
	geographicalname varchar(200) NULL,
	startnode varchar(200) NULL,
	endnode varchar(200) NULL,
	validfrom date NULL,
	validto date NULL,
	fictitious bool NULL,
	fict varchar(100) NULL,
	geomid varchar(100) NULL,
	centreline geometry NULL,
	innetwork varchar(200) NULL,
	idurl varchar(100) NULL,
	CONSTRAINT enforce_dims_centreline CHECK ((st_ndims(centreline) = 2)),
	CONSTRAINT tnw_waterwaylink_pkey PRIMARY KEY (id)
);

-- Table Triggers

create trigger id_update_tnw_waterwaylink before
insert or update on tnw_waterwaylink for each row execute procedure set_id_tnw();

-------------------------------------
-- network table for FTs tn-w:WaterwayLink
-- FK is "link", links to id on tnw_waterwaylink
-------------------------------------	

CREATE TABLE public.tnw_waterwaylink_net (
	id serial NOT NULL,
	link varchar(100) NOT NULL,
	networklink varchar(100) NULL,
	CONSTRAINT tnw_waterwaylink_net_pkey PRIMARY KEY (id)
);	

-------------------------------------
-------------------------------------
-- core table for FT tn-w:InlandWaterway
-------------------------------------
-------------------------------------	

CREATE TABLE public.tnw_inlandwaterway (
	id varchar(100) NOT NULL,
	metadata varchar(100) NULL,
	localid varchar(100) NULL,
	"namespace" varchar(100) NULL,
	"version" varchar(100) NULL,
	beginlsv timestamptz NULL,
	endlsv timestamptz NULL,
	geographicalname varchar(200) NULL,
	validfrom date NULL,
	validto date NULL,
	idurl varchar(100) NULL,
	CONSTRAINT tnw_inlandwaterway_pkey PRIMARY KEY (id)
);

-- Table Triggers

create trigger id_update_tnw_inlandwaterway before
insert or update on tnw_inlandwaterway for each row execute procedure set_id_tnw();

-------------------------------------
-- network table for FTs tn-w:InlandWaterway
-- FK is "inland", links to id on tnw_inlandwaterway
-------------------------------------	
CREATE TABLE public.tnw_inlandwaterway_net (
	id serial NOT NULL,
	inland varchar(100) NOT NULL,
	networklink varchar(100) NULL,
	CONSTRAINT tnw_inlandwaterway_net_pkey PRIMARY KEY (id)
);

-------------------------------------
-- link table connecting hy-n:WatercourseLink to tn-w:InlandWaterway
-- FK documented in SQL
-------------------------------------
CREATE TABLE public.tnw_inlandwaterwaylink_hy (
	id serial NOT NULL,
	waterwayid varchar(100) NULL,
	waterwaylinkid varchar(100) NULL,
	CONSTRAINT iwhyfk FOREIGN KEY (waterwayid) REFERENCES tnw_inlandwaterway(id) MATCH FULL,
	CONSTRAINT iwlhyfk FOREIGN KEY (waterwaylinkid) REFERENCES hyn_watercourselink(id) MATCH FULL
);

-------------------------------------
-- link table connecting tn-w:WaterwayLink to tn-w:InlandWaterway
-- FK waterwayid references tnw_inlandwaterway(id)
-- FK waterwaylinkid references tnw_waterwaylink(id)
-------------------------------------
CREATE TABLE public.tnw_inlandwaterwaylink_tn (
	id serial NOT NULL,
	waterwayid varchar(100) NULL,
	waterwaylinkid varchar(100) NULL
);
	