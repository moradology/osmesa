resource "aws_ecs_service" "changeset_stream" {
  name            = "Changeset-Stream"
  cluster         = "${module.container_service_cluster.id}"
  task_definition = "${aws_ecs_task_definition.changeset_stream.arn}"
  desired_count   = 1
  depends_on      = ["module.container_service_cluster"]
}

resource "aws_ecs_task_definition" "changeset_stream" {
  family                = "changeset_stream"
  container_definitions = "${file("terraform/task-definitions/changeset_service.json")}"
}

resource "aws_ecr_repository" "changeset_stream" {
  name = "changeset_stream"
}
