#!/bin/sh

./mvnw package
docker build -f src/main/docker/Dockerfile.jvm -t quarkus/medusa-jvm .

SCRIPT_DIR="$(pwd)"
echo "start container on CLI:"
echo "docker run -i --rm -p 8085:8085 -v $SCRIPT_DIR/config:/deployments/config/  quarkus/medusa-jvm"
