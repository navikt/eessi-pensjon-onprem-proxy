FROM navikt/java:11-appdynamics

COPY build/libs/eessi-pensjon-onprem-proxy.jar /app/app.jar

ENV APPD_NAME eessi-pensjon
ENV APPD_TIER onpremproxy
ENV APPD_ENABLED true
