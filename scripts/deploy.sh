#!/bin/bash
# Deployment script for Java microservices

# Check if environment is provided
if [ -z "$1" ]; then
  echo "Usage: $0 <environment>"
  echo "Environments: dev, staging, prod"
  exit 1
fi

ENV=$1
echo "Deploying to $ENV environment..."

# Set environment-specific variables
case $ENV in
  dev)
    SERVER="dev-server"
    PORT="8080"
    ;;
  staging)
    SERVER="staging-server"
    PORT="8080"
    ;;
  prod)
    SERVER="prod-server"
    PORT="80"
    ;;
  *)
    echo "Unknown environment: $ENV"
    exit 1
    ;;
esac

echo "Building services..."
cd ./auth-service && ./mvnw clean package -DskipTests
cd ./shopping-service && ./mvnw clean package -DskipTests

echo "Deploying services to $SERVER:$PORT..."
# This is a placeholder for actual deployment commands
# In a real scenario, you would use scp, rsync, or a CI/CD tool

echo "Deployment to $ENV completed successfully!"