CREATE SEQUENCE public.animes_id_seq
    INCREMENT 1
    START 16
    MINVALUE 1
    MAXVALUE 2147483647
    CACHE 1;

ALTER SEQUENCE public.animes_id_seq
    OWNER TO postgres;
    
CREATE SEQUENCE public.canciones_id_seq
    INCREMENT 1
    START 13
    MINVALUE 1
    MAXVALUE 2147483647
    CACHE 1;

ALTER SEQUENCE public.canciones_id_seq
    OWNER TO postgres;

-- Table: public.animes

-- DROP TABLE public.animes;

CREATE TABLE public.animes
(
    id integer NOT NULL DEFAULT nextval('animes_id_seq'::regclass),
    nombre text COLLATE pg_catalog."default" NOT NULL,
    sinopsis text COLLATE pg_catalog."default" NOT NULL,
    genero1 text COLLATE pg_catalog."default" NOT NULL,
    genero2 text COLLATE pg_catalog."default",
    genero3 text COLLATE pg_catalog."default",
    tipo text COLLATE pg_catalog."default" NOT NULL,
    imagen text COLLATE pg_catalog."default" NOT NULL,
    visitas integer NOT NULL DEFAULT 0,
    CONSTRAINT animes_pkey PRIMARY KEY (id)
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;

ALTER TABLE public.animes
    OWNER to postgres;
    
-- Table: public.canciones

-- DROP TABLE public.canciones;

CREATE TABLE public.canciones
(
    id integer NOT NULL DEFAULT nextval('canciones_id_seq'::regclass),
    nombre text COLLATE pg_catalog."default" NOT NULL,
    tipo text COLLATE pg_catalog."default" NOT NULL,
    banda text COLLATE pg_catalog."default" NOT NULL,
    anime text COLLATE pg_catalog."default" NOT NULL,
    descargas integer NOT NULL DEFAULT 0,
    usuario text COLLATE pg_catalog."default" NOT NULL,
    url text COLLATE pg_catalog."default",
    CONSTRAINT canciones_pkey PRIMARY KEY (id)
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;

ALTER TABLE public.canciones
    OWNER to postgres;
    
CREATE SEQUENCE public.usuarios_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE public.usuarios_id_seq
    OWNER TO postgres;
    
-- Table: public.usuarios

-- DROP TABLE public.usuarios;

CREATE TABLE public.usuarios
(
    id integer NOT NULL DEFAULT nextval('usuarios_id_seq'::regclass),
    username text COLLATE pg_catalog."default" NOT NULL,
    password text COLLATE pg_catalog."default" NOT NULL,
    session text COLLATE pg_catalog."default",
    email text COLLATE pg_catalog."default" NOT NULL,
    tipo text COLLATE pg_catalog."default" NOT NULL DEFAULT 'normal',
    CONSTRAINT usuarios_pkey PRIMARY KEY (id)
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;

ALTER TABLE public.usuarios
    OWNER to postgres;