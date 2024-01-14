FROM postgres:15.4-alpine3.18

ENV POSTGRES_USER postgres
ENV POSTGRES_PASSWORD postgres
ENV POSTGRES_DB house_db

COPY src/main/resources/db/*  /docker-entrypoint-initdb.d/