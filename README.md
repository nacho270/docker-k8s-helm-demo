# Using docker, k8s and helm with java

This is a simple app that exposes 2 endpoints to retrieve and increment a counter in mongodb.

Avoided spring/maven and went for [Gradle](https://gradle.org/install/) / [Javalin](https://javalin.io/) for the sake of variety.

The usage of mongo is only to showcase kubernetes persistent volume claims and secrets. The right tool for the app features would be redis.

## Requirements

- Java 11
- [Gradle](https://gradle.org/install/)
- [Docker](https://www.docker.com/products/docker-desktop)
- [Minikube](https://minikube.sigs.k8s.io/docs/start/)

## Run locally

### From IDE

- Run a local mongo in docker
  
`docker run --name mongo -p 27017:27017 -e MONGO_INITDB_ROOT_USERNAME=root -e MONGO_INITDB_ROOT_PASSWORD=root -d mongo:latest`

- Run the app
  
`com.nacho.dockerk8shelm.demo.App` with an environment variable `profile=dev`

### Docker compose

Simply do `docker compose up` from the root directory of the app.

### Api calls

- `curl localhost:9090/ping`
- `curl localhost:9090/counter`
- `curl -XPOST localhost:9090/counter`

## Run in K8S local

To understand the process, run the following commands from the root directory of the app:

    gradle build jar
    gradle docker
    docker images # you'll find the image there
    minukube ssh
    docker images # the image is not there and THIS where we need the image.

Minikube will attempt to pull any image from the docker hub if it's not present on its image cache. So it's necessary to
point minikube to the local docker and build the app image:

    - if on mac/linux: eval $(minikube docker-env)
    - if windows: 
        - minikube docker-env
        - copy all the lines and run them
        - you''ll see that the last line is commented (REM), so run this: @FOR /f "tokens=*" %i IN ('minikube -p minikube docker-env') DO @%i`
    - gradle build jar
    - gradle docker
    - minukube ssh 
    - docker images ## the image is there!

    Remember: all the docker-env changes will vanish in anohter terminal and if you restart minikube. 
    Make sure you build the image in the same terminal where you previously ran all the docker-env commands

### Basic configuration: Namespace and Secret

First, we need to create the namespace and the secret:

- cd into `k8s`
- `kubectl apply -f namespace.yaml`
- `kubectl config set-context --current --namespace=docker-k8s-helm-demo` <-- this will avoid you having to type `-n docker-k8s-helm-demo` on every kubernetes command
- `kubectl create secret generic mongo-pass --from-literal mongoPass=root` <-- create the mongo password as a secret to be retrieved in the mongo-deployment.

### Deploy mongodb

We don't want to create a mongodb node every time in the real world. For that reason, we manage it independently of the app.

You should run all of these commands to have mongodb running, or you can do the same with helm. This is only for illustration purposes.

- cd into `k8s`
- `kubectl apply -f database/templates/mongo-persistent-volume-claim.yaml`
- `kubectl apply -f database/templates/mongo-deployment.yaml`
- `kubectl apply -f database/templates/mongo-clusterIP.yaml`

### Deploy app

Helm helps to deploy the entire application kubernetes objects. One case where it's particularly useful is when you need
to create all the services with different values for different environments such as: `dev`, `test`, `nft`, `prod`.

By the default, the 'values.yaml' file is used, but a different file can be specified like this: `-f values-dev.yaml`.

- cd into`k8s`
- To deploy mongodb: `helm install docker-k8s-helm-mongo-demo database`
- To deploy the app: `helm install docker-k8s-helm-demo app`
- To undeploy the app: `helm uninstall docker-k8s-helm-demo app`
- To print the entire chart in the terminal: `helm template app`
- To print the entire chart to a file: `helm template app > the-chart.yaml`

**To check that everything is running: `kubectl get pods` and you should see the pod running + mongo.**

**Alternatively, you can enable the dashboard: `minikube dashboard` or you can download [Lens](https://k8slens.dev/).**

### Ingress

Ingress is what manages the external access to the services in a cluster by exposing a port and routing rules that will 
send a request a `ClusterIP` or `NodePort` and these will forward it to the container in a `deployment`.

It's worth mentioning that the fact that ingress manages external access, that doesn't make the application visible to your machine.
The exposed port -8080 for instance- is the port that kubernetes exposes but that has to be bind to a real port in your physical machine. 

Once you made sure everything is running:

- `minikube addons enable ingress`

Then you can either go with one of these options:

- Option one:
  
  ``` shell
  kubectl expose deployment docker-k8s-helm-demo --type=ClusterIP --port=8080
  minikube service  docker-k8s-helm-demo -n docker-k8s-helm-demo --url
  ```

  Get url from the table:

    | NAMESPACE     | NAME                 |  TARGET PORT  | URL                    |
    | :------------:|:--------------------:|:-------------:|:----------------------:|
    | default       | docker-k8s-helm-demo |               | http://127.0.0.1:65451 |

  - `curl localhost:65451/ping`
  - `curl localhost:65451/counter`
  - `curl -XPOST localhost:65451/counter`

or

- Option two: `kubectl port-forward deployment/docker-k8s-helm-demo 8080:8080`

  - `curl localhost:8080/ping`
  - `curl localhost:8080/counter`
  - `curl -XPOST localhost:8080/counter`
