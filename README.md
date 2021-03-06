##Run local
To understand the process:

    gradle docker
    docker images -- you'll the image ther 
    minukube ssh
    docker images -- your image is not there and THIS where we need the image 

Now the real work:
    
    - if mac/linux: eval $(minikube docker-env)
    - if windows: 
        - minikube docker-env
        - copy all the lines and run them
        - you''ll that the last line is commented (REM), so run this: @FOR /f "tokens=*" %i IN ('minikube -p minikube docker-env') DO @%i`
    
    - gradle docker
    - minukube ssh docker images -- the image is there!

    Remember: all the docker-env changes will vanish in anohter terminal and if you restart minikube. 
    Make sure you build the image in the same terminal where you ran all the docker-env commands

##Helm

`cd docker-k8s-helm-demo/k8s`

`helm install docker-k8s-helm-demo app`

`helm uninstall docker-k8s-helm-demo app`

`helm template app` 

`helm template app > the-chart.yaml`

**To check that everything is running: `kubectl get pods` and you should see the 3 pods running.**

**Alternatively, you can enable the dashboard: `minikube dashboard`**

##Ingress

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