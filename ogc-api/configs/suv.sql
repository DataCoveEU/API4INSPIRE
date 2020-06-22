CREATE TABLE public.suv_suvec (
	id varchar(100) NOT NULL,
	gmlid varchar(100) NOT NULL,
	localid varchar(100) NULL,
	"namespace" varchar(100) NULL,
	"version" varchar(100) NULL,
	country varchar(100) NULL,
	"name" varchar(100) NULL,
	beginlifespanversion date NULL,
	endlifespanversion date NULL,
	validfrom date NULL,
	validto date NULL,
	validto_status varchar(100) NULL,
	beginrefperiod date NULL,
	endrefperiod date NULL,
	endref_status varchar(100) NULL,
	geomid varchar(100) NULL,
	geomtype varchar(100) NULL,
	endlifespan_status varchar(100) NULL,
	validtpid varchar(100) NULL,
	reftpid varchar(100) NULL,
	geom geometry(MULTIPOLYGON, 4326) NULL,
	CONSTRAINT suv_suvec2_pk PRIMARY KEY (id)
);

-- Table Triggers

CREATE OR REPLACE FUNCTION public.set_id()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$

	BEGIN
		NEW.id := NEW.namespace || '-' || NEW.localid;
	RETURN NEW;
END 
$function$
;

-- DROP TRIGGER suv_suvec2_update ON public.suv_suvec;

create trigger suv_suvec2_update before
insert
    or
update
    on
    suv_suvec for each row execute procedure set_id();
