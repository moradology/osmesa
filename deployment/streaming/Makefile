SBT_DIR="../../src"
JAR_DIR=$(SBT_DIR)/analytics/target/scala-2.11/
DOCKER_DIR="./docker"

clean:
	touch docker/osmesa-analytics.jar
	rm -f build
	rm -f tfplan
	rm -f $(DOCKER_DIR)/osmesa-analytics.jar
	cd $(SBT_DIR) && sbt "project analytics" clean

build:
	cd $(SBT_DIR) && sbt "project analytics" assembly
	cp $(JAR_DIR)/osmesa-analytics.jar $(DOCKER_DIR)/osmesa-analytics.jar
	touch build

tfplan: build
	terraform init ./terraform
	terraform plan -var-file=tfvars -out=tfplan ./terraform

apply: tfplan
	terraform apply tfplan

containers: build
	docker-compose -f $(DOCKER_DIR)/docker-compose.yml build

