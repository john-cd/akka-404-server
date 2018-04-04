A very simple 404 server similar to https://github.com/kubernetes/ingress-nginx/tree/master/images/404-server

* Serves a 404 page at /
* Serves 200 on /healthz


## Deployment Steps

- Generate the Dockerfile and environment prepared for creating a Docker image

```shell
sbt docker:stage
```

- Verify output under ``target/docker`` 

- Start minikube

```shell
minikube start
```

- Connect Docker to the Docker daemon in the K8s VM

```shell
eval $(minikube docker-env)
```

- Builds an image and publish it to Kubernetes' Docker server 

```shell
sbt docker:publishLocal
```


- To view the list of built images run ``docker images``
- If needed, remove the built image from the Docker server with ``sbt docker:clean`` 
or use ``docker rmi <image name>:<tag>`` to delete old images

- Deploy on Kubernetes

```shell
./helm install --dry-run --debug akka-404-server
```

and if that looks OK  

```shell
./helm install akka-404-server
```

- Verify that a service and deployment have been created

```shell
kubectl get service
kubectl get deployment
kubectl get pods
```

Or open the Kubernetes dashboard

```shell
minikube dashboard
```

- Verify that the correct port (8080) is open on the pods 

```shell
kubectl get pods

kubectl get pods <pod name>  --template='{{(index (index .spec.containers 0).ports 0).containerPort}}{{"\n"}}'
```

- Test the deployment

```shell
export POD_NAME=$(kubectl get pods --namespace default -l "app=akka-404-server,release=< release name >" -o jsonpath="{.items[0].metadata.name}")
echo "Visit http://127.0.0.1:8080 to use your application"
kubectl port-forward $POD_NAME 8080:8080
curl http://localhost/healthz  # should return a counter
curl http://localhost/         # should return "Unknown resource!"
```

The same should work at the service level

```shell
kubectl port-forward service/<service name> 8080:80
```
