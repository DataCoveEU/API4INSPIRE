drop TABLE public.hyp_watercourse

CREATE TABLE public.hyp_watercourse (
	id varchar(100) NOT NULL,
	gmlid varchar(100) NULL,
	metadata varchar(100) NULL,
	localid varchar(100) NULL,
	"namespace" varchar(100) NULL,
	"version" varchar(100) NULL,
	beginlsv timestamptz NULL,
	endlsv timestamptz NULL,
	geoname varchar(100) NULL,
	related varchar(100) NULL,
	detail integer NULL,
	detail_uom varchar(100) NULL,
	localtype varchar(100) NULL,
	localtype_url varchar(100) NULL,	
	origin varchar(100) NULL,
	persistence varchar(100) NULL,
	tidal boolean NULL,
	bank varchar(100) NULL,
	drains_basin varchar(100) NULL,
	neighbour varchar(100) NULL,
	conditionurl varchar(100) NULL,
	delineation_known boolean NULL,
	totlength integer NULL,
	totlength_uom varchar(100) NULL,
	level_val varchar(100) NULL,
	lower_width integer NULL,
	lower_width_uom varchar(100) NULL,
	upper_width integer NULL,
	upper_width_uom varchar(100) null,
	geom_id varchar(100) null,
	CONSTRAINT hyp_watercourse_pk PRIMARY KEY (id)
);

SELECT AddGeometryColumn ('public','hyp_watercourse','geom',4326,'LINESTRING',2);

CREATE TABLE public.hyn_hydroid (
	id serial NOT NULL,
	hyn_object varchar(100) NULL,
	hydroid varchar(100) NOT NULL,
	hydroidclass varchar(200) NULL,
	hydroidns varchar(100) NOT NULL
);

INSERT INTO public.hyp_watercourse
(id, gmlid, metadata, localid, "namespace", "version", beginlsv, endlsv, geoname, related, 
	detail, detail_uom, localtype, localtype_url, origin, persistence, tidal, bank, drains_basin, neighbour, conditionurl, delineation_known, 
	totlength, totlength_uom, level_val, lower_width, lower_width_uom, upper_width, upper_width_uom, geom_id, geom)
VALUES('id', 'gmlid', 'metadata', 'localid', 'namespace', 'version', '2017-11-10 00:00:00', '2019-11-10 00:00:00', 'geoname', 'related', 
	10, 'detail_uom', 'localtype', 'localtype_url', 'origin', 'persistence', false, 'bank', 'drains_basin', 'neighbour', 'conditionurl', true, 
	110, 'totlength_uom', 'level_val', 0, 'lower_width_uom', 0, 'upper_width_uom', 'geom_id', 
	ST_GeomFromText('LINESTRING (-1.525996 48.037707, -1.524567 48.037439, -1.5237 48.037513, -1.521175 48.037866, -1.519537 48.038415, -1.517548 48.038751, -1.514776 48.039382, -1.5131 48.039437, -1.511751 48.039347, -1.510255 48.039081, -1.50703 48.038106, -1.506746 48.03789, -1.506063 48.035887, -1.505762 48.035447, -1.5054 48.035099, -1.503096 48.033868, -1.50248 48.033709, -1.500241 48.033377, -1.499692 48.033215, -1.499405 48.032954, -1.499188 48.032736, -1.499021 48.032292, -1.499006 48.031167, -1.499129 48.030082, -1.499468 48.029216, -1.499719 48.028982, -1.499837 48.028753, -1.500971 48.027726, -1.503072 48.026172, -1.504087 48.025373, -1.50458 48.024772, -1.50504 48.023721, -1.505488 48.022491, -1.505727 48.022078)', 4326));




