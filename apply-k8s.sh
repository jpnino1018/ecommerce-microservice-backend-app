#!/bin/bash

# Exit immediately if a command exits with a non-zero status.
set -e

RESOURCE_GROUP="ecommerce-rg"
AKS_NAME="ecommerce-aks"
K8S_NAMESPACE="dev" # Define your target namespace

# Get AKS credentials and configure kubectl
az aks get-credentials --resource-group "$RESOURCE_GROUP" --name "$AKS_NAME" --overwrite-existing

echo "Ensuring Kubernetes namespace '$K8S_NAMESPACE' exists..."

# Check if the namespace exists, if not, create it.
if ! kubectl get namespace "$K8S_NAMESPACE" &> /dev/null; then
  echo "Namespace '$K8S_NAMESPACE' does not exist. Creating it now..."
  kubectl create namespace "$K8S_NAMESPACE"
  echo "Namespace '$K8S_NAMESPACE' created successfully."

else
  echo "Namespace '$K8S_NAMESPACE' already exists. Skipping creation."
fi

echo "Applying Kubernetes manifests to the AKS cluster..."

# 1. Apply k8s/dev/service-discovery-deployment.yml
echo "Applying k8s/dev/service-discovery-deployment.yml..."
kubectl apply -f k8s/dev/service-discovery-deployment.yml -n "$K8S_NAMESPACE"

# Wait for service-discovery to be ready (more robust than just sleep)
echo "Waiting for service-discovery deployment to be ready..."
kubectl wait --for=condition=Available deployment/service-discovery --timeout=300s -n "$K8S_NAMESPACE" || { echo "Service Discovery deployment failed to become ready. Exiting."; exit 1; }

# 2. Apply all manifests in k8s/core/
echo "Applying manifests in k8s/core/..."
kubectl apply -f k8s/core/ -n "$K8S_NAMESPACE"

# Optional: Add a sleep after the second apply if you have other dependencies that need time
# echo "Sleeping for 10 seconds before applying remaining dev manifests..."
# sleep 10

# 3. Apply all manifests in k8s/dev/ (except service-discovery-deployment.yml)
echo "Applying manifests in k8s/dev/ (excluding service-discovery-deployment.yml)..."
find k8s/dev/ -maxdepth 1 -name "*.yml" ! -name "service-discovery-deployment.yml" -exec kubectl apply -f {} -n "$K8S_NAMESPACE" \;

# Wait for all deployments to be ready (important before running tests)
echo "Waiting for all deployments in namespace '$K8S_NAMESPACE' to be ready..."
# This loops through all deployments and waits for each.
# For a more robust wait, you might want to specifically target known critical deployments.
for deployment in $(kubectl get deployments -n "$K8S_NAMESPACE" -o jsonpath='{.items[*].metadata.name}'); do
  echo "  Waiting for deployment/$deployment to be available..."
  kubectl wait --for=condition=Available deployment/"$deployment" --timeout=300s -n "$K8S_NAMESPACE" || { echo "Deployment $deployment failed to become ready. Exiting."; exit 1; }
done

echo "All specified Kubernetes manifests have been applied and deployments are ready."

# Newman Integration Tests

echo "--- Starting Newman Integration Tests ---"

# Apply the Newman Job manifest
echo "Applying Newman Job manifest..."
kubectl apply -f k8s/newman/newman-job.yml -n "$K8S_NAMESPACE"

# Wait for the Newman Job to complete
echo "Waiting for Newman Job 'newman-integration-tests' to complete..."
kubectl wait --for=condition=complete job/newman-integration-tests --timeout=600s -n "$K8S_NAMESPACE" || { echo "Newman Job failed or timed out. Checking logs for details."; exit 1; }

# Get the pod name of the completed job to retrieve logs
NEWMAN_POD=$(kubectl get pods -n "$K8S_NAMESPACE" -l job-name=newman-integration-tests -o jsonpath='{.items[0].metadata.name}')

if [ -n "$NEWMAN_POD" ]; then
  echo "Newman Job completed. Fetching logs from pod: $NEWMAN_POD"
  kubectl logs "$NEWMAN_POD" -n "$K8S_NAMESPACE"
  # kubectl cp "$NEWMAN_POD":/newman-results/report.xml ./newman-report.xml -n "$K8S_NAMESPACE"
  # echo "JUnit report copied to ./newman-report.xml"
else
  echo "Could not find Newman Job pod to fetch logs."
fi

# Clean up the Newman Job (optional, but good for CI/CD)
echo "Deleting Newman Job 'newman-integration-tests'..."
kubectl delete job newman-integration-tests -n "$K8S_NAMESPACE"

echo "Deployment and Integration Tests complete."