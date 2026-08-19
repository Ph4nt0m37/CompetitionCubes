--
-- PostgreSQL database dump
--

-- Dumped from database version 15.13 (Debian 15.13-0+deb12u1)
-- Dumped by pg_dump version 15.13 (Debian 15.13-0+deb12u1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'LATIN1';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: public; Type: SCHEMA; Schema: -; Owner: postgres
--

-- *not* creating schema, since initdb creates it


ALTER SCHEMA public OWNER TO postgres;

--
-- Name: SCHEMA public; Type: COMMENT; Schema: -; Owner: postgres
--

COMMENT ON SCHEMA public IS '';


--
-- Name: fuzzystrmatch; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS fuzzystrmatch WITH SCHEMA public;


--
-- Name: EXTENSION fuzzystrmatch; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION fuzzystrmatch IS 'determine similarities and distance between strings';


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: banned_users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.banned_users (
    id bigint,
    expirationdate bigint,
    reason character varying(255)
);


ALTER TABLE public.banned_users OWNER TO postgres;

--
-- Name: invalid_solves; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.invalid_solves (
    id integer,
    username character varying(51),
    scramble character varying(255),
    single double precision,
    average double precision,
    event character varying(12),
    wca_single character varying(11),
    wca_average character varying(11)
);


ALTER TABLE public.invalid_solves OWNER TO postgres;

--
-- Name: reported_users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.reported_users (
    id bigint NOT NULL,
    reason character varying(50),
    info character varying(50)
);


ALTER TABLE public.reported_users OWNER TO postgres;

--
-- Name: threestats; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.threestats (
    userid bigint,
    elo bigint,
    single numeric,
    average numeric,
    old_singles double precision[],
    old_averages double precision[]
);


ALTER TABLE public.threestats OWNER TO postgres;

--
-- Name: user_settings; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_settings (
    id bigint NOT NULL,
    inspection_audio boolean NOT NULL,
    match_sounds boolean NOT NULL,
    accepts_challenge_requests boolean NOT NULL,
    hide_wca_profile boolean DEFAULT false
);


ALTER TABLE public.user_settings OWNER TO postgres;

--
-- Name: user_warnings; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_warnings (
    id bigint NOT NULL,
    expirationdate bigint NOT NULL,
    reason character varying(255)
);


ALTER TABLE public.user_warnings OWNER TO postgres;

--
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    userid bigint NOT NULL,
    wcaid character varying(10) NOT NULL,
    username character varying(50) NOT NULL,
    usersecret character varying(255) NOT NULL,
    matcheswon bigint,
    matcheslost bigint,
    badges integer[],
    strikes bigint,
    bans bigint,
    permlevel smallint,
    is_donor boolean
);


ALTER TABLE public.users OWNER TO postgres;

--
-- Name: users_userid_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.users_userid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.users_userid_seq OWNER TO postgres;

--
-- Name: users_userid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.users_userid_seq OWNED BY public.users.userid;


--
-- Name: users userid; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users ALTER COLUMN userid SET DEFAULT nextval('public.users_userid_seq'::regclass);

--
-- Name: users_userid_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.users_userid_seq', 1, true);


--
-- Name: user_settings user_settings_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_settings
    ADD CONSTRAINT user_settings_pkey PRIMARY KEY (id);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (userid);


--
-- Name: threestats threestats_userid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.threestats
    ADD CONSTRAINT threestats_userid_fkey FOREIGN KEY (userid) REFERENCES public.users(userid);


--
-- Name: SCHEMA public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE USAGE ON SCHEMA public FROM PUBLIC;


--
-- PostgreSQL database dump complete
--

