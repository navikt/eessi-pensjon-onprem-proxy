FROM navikt/java:17-appdynamics

COPY build/libs/eessi-pensjon-onprem-proxy.jar /app/app.jar
COPY nais/export-vault-secrets.sh /init-scripts/

ENV APPD_NAME eessi-pensjon
ENV APPD_TIER onpremproxy
ENV APPD_ENABLED true
