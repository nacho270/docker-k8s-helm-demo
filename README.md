# Using docker, k8s and helm with java

This is a simple app that exposes 2 endpoints to retrieve and increment a counter in mongodb.

Avoided spring/maven and went for gradle/[Javalin](https://javalin.io/)  for the sake of variety.

Used mongo to showcase kubernetes persistent volumes and secrets.

## Run locally

### From IDE

- Run a local
  mongo: `docker run --name mongo -p 27017:27017 -e MONGO_INITDB_ROOT_USERNAME=root -e MONGO_INITDB_ROOT_PASSWORD=root -d mongo:latest`

- Run `com.nacho.dockerk8shelm.demo.App` with an environment variable `profile=dev`

### Docker compose

Simply do `docker compose up` from the root directory of the app.

## Run K8S local

To understand the process, run the following commands from the root directory of the app:

    gradle docker
    docker images # you'll find the image there
    minukube ssh
    docker images ## the image is not there and THIS where we need the image. Kubernetes uses this docker repo to cache the images. 

Now the real work:

    - if on mac/linux: eval $(minikube docker-env)
    - if windows: 
        - minikube docker-env
        - copy all the lines and run them
        - you''ll see that the last line is commented (REM), so run this: @FOR /f "tokens=*" %i IN ('minikube -p minikube docker-env') DO @%i`
    
    - gradle docker
    - minukube ssh docker images ## the image is there!

    Remember: all the docker-env changes will vanish in anohter terminal and if you restart minikube. 
    Make sure you build the image in the same terminal where you previously ran all the docker-env commands

### Install mongodb

We don't want to create mongo every time in the real work. So we manage mongo independently of the app using plain
kubernetes.

- cd into `k8s`
- `kubectl create secret generic mongo-pass --from-literal mongoPass=root -n docker-k8s-helm-demo`
- `kubectl apply -f app/mongo-persistent-volume-claim.yaml`
- `kubectl apply -f app/mongo-deployment.yaml`
- `kubectl apply -f app/mongo-clusterIP.yaml`

## Helm

- cd into`k8s`
- To deploy the app:`helm install docker-k8s-helm-demo app`
- To undeploy the app: `helm uninstall docker-k8s-helm-demo app`
- To print the entire chart in the terminal: `helm template app`
- To print the entire chart to a file: `helm template app > the-chart.yaml`

**To check that everything is running: `kubectl get pods` and you should see the pod running + mongo.**

**Alternatively, you can enable the dashboard: `minikube dashboard` or you can download [Lens](https://k8slens.dev/).**

## Ingress

Once everything is running:

- `minikube addons enable ingress`

Then you can either go with:

- Option one

  `minikube service docker-k8s-helm-demo --url`

  Get url from the table:

  | NAMESPACE     | NAME                 |  TARGET PORT  | URL                    |
      | :------------:|:--------------------:|:-------------:|:----------------------:|
  | default       | docker-k8s-helm-demo |               | http://127.0.0.1:65451 |

  `curl http://127.0.0.1:65451`

or

- Option two

  `k port-forward deployment/docker-k8s-helm-demo 8080:8080`

  `curl localhost:8080`