# Can be overridden if necessary
variable "aws_region" { default = "us-east-1" }

# The name of your EC2 key
variable "key_name" { }

# Location to dump EMR logs
variable "s3_uri" { }

# The hbase root for s3
variable "s3_hbase_root_uri" { }

# Subnet for the analytics cluster
variable "analytics_subnet_id" { }

variable "slave_count" { }