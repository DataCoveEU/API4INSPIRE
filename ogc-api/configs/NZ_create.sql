-- NZ Config by datacove.eu
--drop TABLE public.nz_hazardarea

CREATE TABLE public.nz_hazardarea (
	id varchar(100) NOT NULL,
	gmlid varchar(100) NULL,
	metadata varchar(100) NULL,
	det_method varchar(100) NULL,
	localid varchar(100) NULL,
	"namespace" varchar(100) NULL,
	"version" varchar(100) NULL,
	beginlsv timestamptz NULL,
	endlsv timestamptz NULL,
	haz_category varchar(100) NULL,
	haz_type varchar(100) NULL,
	likelihood_qual varchar(100) NULL,
	likelihood_prob float NULL,
	likelihood_return integer  NULL,
	likelihood_name varchar(100) NULL,
	likelihood_date date NULL,
	likelihood_datetype varchar(100) NULL,
	likelihood_datetype_cl varchar(100) NULL,
	likelihood_link varchar(100) NULL,
	magnitude_qual varchar(100) NULL,
	magnitude_quant_val numeric  NULL,
	magnitude_quant_uom varchar(100) NULL,
	magnitude_name varchar(100) NULL,
	magnitude_date date NULL,
	magnitude_datetype varchar(100) NULL,
	magnitude_datetype_cl varchar(100) NULL,
	magnitude_link varchar(100) NULL,	
	CONSTRAINT nz_hazardarea_pk PRIMARY KEY (id)
);

SELECT AddGeometryColumn ('public','nz_hazardarea','geom',4326,'POLYGON',2);

INSERT INTO public.nz_hazardarea
(id, gmlid, metadata, det_method, localid, "namespace", "version", beginlsv, endlsv, haz_category, haz_type, 
likelihood_qual, likelihood_prob, likelihood_return, likelihood_name, likelihood_date, likelihood_datetype, likelihood_datetype_cl, likelihood_link, 
magnitude_qual, magnitude_quant_val, magnitude_quant_uom, magnitude_name, magnitude_date, magnitude_datetype, magnitude_datetype_cl, magnitude_link, geom)
VALUES('1', '_1', 'metadata', 'modelling', '1', 'namespace', 'version', '2017-11-10 00:00:00', '2019-11-10 00:00:00', 'haz_category', 'haz_type', 
'likelihood_qual', 0.25, 4, 'likelihood_name', '2017-11-10', 'likelihood_datetype', 'likelihood_datetype_cl', 'likelihood_link', 
'magnitude_qual', 123, 'magnitude_quant_uom', 'magnitude_name', '2017-11-10', 'magnitude_datetype', 'magnitude_datetype_cl', 'magnitude_link', 
ST_GeomFromText('POLYGON((-71.1776585052917 42.3902909739571,-71.1776820268866 42.3903701743239,
-71.1776063012595 42.3903825660754,-71.1775826583081 42.3903033653531,-71.1776585052917 42.3902909739571))', 4326));



