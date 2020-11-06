CREATE TABLE public.ex_mainft (
	id varchar(100) NOT NULL,
	gmlid varchar(100) NULL,
	metadata varchar(100) NULL,
	localid varchar(100) NULL,
	"namespace" varchar(100) NULL,
	"version" varchar(100) NULL,
	ft_name varchar(100) NULL,
	code varchar(100) NULL,
	nested_dt_name varchar(100) NULL,
	counted_dt_name1 varchar(100) NULL,
	counted_dt_name2 varchar(100) NULL,
	nested_ft_name varchar(100) NULL,
	other_ft_id varchar(100) NULL,
	geom geometry(POINT, 4326) NULL,
	CONSTRAINT ex_mainft_gmlid UNIQUE (gmlid),
	CONSTRAINT ex_mainft_pk PRIMARY KEY (id)
);

CREATE TABLE public.ex_nesteddt (
	id varchar(100) NOT NULL,
	gmlid varchar(100) NULL,
	dt_name varchar(100) NULL,
	main_ft_id varchar(100) NULL,
	CONSTRAINT ex_nesteddt_pk PRIMARY KEY (id),
	CONSTRAINT ex_nesteddt_main_ft_id_fkey FOREIGN KEY (main_ft_id) REFERENCES ex_mainft(id)
);

CREATE TABLE public.ex_nestedft (
	id varchar(100) NOT NULL,
	gmlid varchar(100) NULL,
	ft_name varchar(100) NULL,
	main_ft_id varchar(100) NULL,
	CONSTRAINT ex_nestedft_pk PRIMARY KEY (id),
	CONSTRAINT ex_nestedft_main_ft_id_fkey FOREIGN KEY (main_ft_id) REFERENCES ex_mainft(id)
);

CREATE TABLE public.ex_otherdt (
	id varchar(100) NOT NULL,
	gmlid varchar(100) NULL,
	dt_name varchar(100) NULL,
	main_ft_id varchar(100) NULL,
	CONSTRAINT ex_otherdt_pk PRIMARY KEY (id),
	CONSTRAINT ex_otherdt_main_ft_id_fkey FOREIGN KEY (main_ft_id) REFERENCES ex_mainft(id)
);

CREATE TABLE public.ex_otherft (
	id varchar(100) NOT NULL,
	gmlid varchar(100) NULL,
	ft_name varchar(100) NULL,
	main_ft_id varchar(100) NULL,
	CONSTRAINT ex_otherft_gmlid UNIQUE (gmlid),
	CONSTRAINT ex_otherft_pk PRIMARY KEY (id)
);

CREATE TABLE public.ex_main_other_as (
	mainid varchar(100) NOT NULL,
	otherid varchar(100) NOT NULL,
	CONSTRAINT ex_main_ft_id_fkey FOREIGN KEY (mainid) REFERENCES ex_mainft(gmlid),
	CONSTRAINT ex_other_ft_id_fkey FOREIGN KEY (otherid) REFERENCES ex_otherft(gmlid)
);
