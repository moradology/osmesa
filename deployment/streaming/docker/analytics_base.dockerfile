FROM mesosphere/spark:2.3.1-2.2.1-2-hadoop-2.7

COPY osmesa-analytics.jar /opt/osmesa-analytics.jar
WORKDIR /opt

