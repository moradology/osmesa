resource "aws_ecs_service" "augdiff_stream" {
  name            = "Augmented-Diff-Stream"
  cluster         = "${module.container_service_cluster.id}"
  task_definition = "${aws_ecs_task_definition.augdiff_stream.arn}"
  desired_count   = 1
  depends_on      = ["module.container_service_cluster"]
}

resource "aws_ecs_task_definition" "augdiff_stream" {
  family                = "augdiff_stream"
  container_definitions = "${file("task-definitions/augdiff_service.json")}"
}

resource "aws_ecr_repository" "augdiff_stream" {
  name = "augdiff_stream"
}

