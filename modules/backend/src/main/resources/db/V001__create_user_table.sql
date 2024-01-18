CREATE EXTENSION IF NOT EXISTS "pgcrypto" CASCADE;

CREATE TABLE users(
  id UUID DEFAULT gen_random_uuid() NOT NULL PRIMARY KEY,
  name CHARACTER VARYING(100) NOT NULL,
  surname CHARACTER VARYING(100) NOT NULL,
  birthPlace CHARACTER VARYING(100) NOT NULL,
  birthDate DATE NOT NULL,
  fiscalCode CHARACTER(16) NOT NULL,
  residence CHARACTER VARYING(100),
  phoneNumber CHARACTER VARYING(100) NOT NULL,
  email CHARACTER VARYING(100) NOT NULL,
  profession CHARACTER VARYING(100),
  memberSince DATE NOT NULL,
  membershipCardNumber CHARACTER VARYING(100) NOT NULL,
  donation INT8,
  pdfDocument BYTEA
);

CREATE INDEX users_name_idx ON users (LOWER(name));
CREATE INDEX users_surname_idx ON users (LOWER(surname));