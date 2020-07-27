

CREATE TABLE public.bs2_contact (
	city varchar(100) NULL,
	address varchar(100) NULL,
	postcode varchar(100) NULL,
	email varchar(100) NULL,
	tel varchar(100) NULL,
	web varchar(200) NULL,
	conid varchar(100) NOT NULL,
	id serial NOT NULL,
	lang varchar(100) NULL,
	individualname varchar(100) NULL,
	organisationname varchar(100) NULL,
	positionname varchar(100) NULL,
	"role" varchar(100) NULL,
	CONSTRAINT bs2_con_pkey PRIMARY KEY (id),
);


CREATE TABLE public.am_mgmt_zone (
	id varchar(100) NOT NULL,
	gmlid varchar(100) NULL,
	metadata varchar(100) NULL,
	localid varchar(100) NULL,
	"namespace" varchar(100) NULL,
	"version" varchar(100) NULL,
	beginlsv timestamptz NULL,
	endlsv timestamptz NULL,
	themid varchar(100) NULL,
	themidscheme varchar(100) NULL,
	gnname varchar(100) NULL,
	zone_type varchar(100) NULL,
	spec_zone_type varchar(100) NULL,
	begindes date NULL,
	enddes date NULL,
	env_domain varchar(100) NULL,
	plan varchar(100) NULL,
	legal_name varchar(100) NULL,
	legal_date date NULL,
	legal_datetype varchar(100) NULL,
	legal_datetype_cl varchar(100) NULL,
	legal_link varchar(100) NULL,
	legal_level varchar(100) NULL,
	CONSTRAINT am_mgmt_zone_pk PRIMARY KEY (id)
);

SELECT AddGeometryColumn ('public','am_mgmt_zone','geom',4326,'POLYGON',2);