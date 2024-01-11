FROM ghcr.io/navikt/baseimages/temurin:21-appdynamics

COPY init-scripts/ep-jvm-tuning.sh /init-scripts/

COPY build/libs/eessi-pensjon-onprem-proxy.jar /app/app.jar
COPY nais/export-vault-secrets.sh /init-scripts/

ENV APPD_NAME eessi-pensjon
ENV APPD_TIER onprem-proxy
ENV APPD_ENABLED true
