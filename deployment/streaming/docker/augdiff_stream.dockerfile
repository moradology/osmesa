FROM osmesa_analytics:latest

HEALTHCHECK --interval=5m --timeout=3s \
  CMD exit 0

ENTRYPOINT ["ls", "."]

