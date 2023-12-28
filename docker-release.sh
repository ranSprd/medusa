#!/bin/sh

./mvnw package
BUILD=$(cat target/classes/buildnumber)

docker build -f src/main/docker/Dockerfile.jvm -t quarkus/medusa-jvm .

SCRIPT_DIR="$(pwd)"
echo "BUILD ID = $BUILD"
echo "start container on CLI:"
echo "docker run -i --rm -p 8085:8085 -v $SCRIPT_DIR/config:/deployments/config/  quarkus/medusa-jvm"
