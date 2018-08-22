FROM osmesa_analytics:latest

COPY healthcheck.sh /opt/healthcheck.sh

HEALTHCHECK --interval=5m --timeout=3s \
  CMD /opt/healthcheck

ENTRYPOINT ["spark-submit", \
            "--class", "osmesa.analytics.oneoffs.AugmentedDiffStreamProcessor.scala", \
            "/opt/osmesa-analytics.jar"]

