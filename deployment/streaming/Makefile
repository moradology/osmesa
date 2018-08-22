SBT_DIR="../../src"
JAR_DIR=$(SBT_DIR)/analytics/target/scala-2.11/
DOCKER_DIR="./docker"

clean:
	touch docker/osmesa-analytics.jar
	rm -f docker/osmesa-analytics.jar
	rm -f build
	rm -f tfplan
	rm -f $(DOCKER_DIR)/osmesa-analytics.jar
	cd $(SBT_DIR) && sbt "project analytics" clean

build:
	cd $(SBT_DIR) && sbt "project analytics" assembly
	cp $(JAR_DIR)/osmesa-analytics.jar $(DOCKER_DIR)/osmesa-analytics.jar
	touch build

tfplan: build
	terraform init
	terraform plan -var-file=tfvars -out=tfplan

apply: tfplan
	terraform apply tfplan

refresh:
	terraform refresh -var-file=tfvars

destroy:
	terraform destroy -var-file=tfvars

validate:
	terraform validate -var-file=tfvars


containers: build
	docker-compose -f $(DOCKER_DIR)/docker-compose.yml build

