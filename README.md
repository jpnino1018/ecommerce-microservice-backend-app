# Notas: IngesoftV Taller 2

## Microservicios

Hay que escoger, al menos, 6 microservicios **(que se comuniquen entre ellos)** para el desarrollo del taller.

El [proyecto](https://github.com/yuluka/ecommerce-microservice-backend-app) contiene 10 microservicios. Estos son:

1. api-gateway
2. cloud-config
3. favourite-service
4. order-service
5. payment-service
6. product-service
7. proxy-client
8. service-discovery
9. shipping-service
10. user-service

Para nuestra solución, **voy a escoger los siguientes**:

1. api-gateway
2. cloud-config
3. favourite-service
4. order-service
5. payment-service
6. product-service
7. proxy-client
8. service-discovery
9. shipping-service
10. user-service

Así es. Decidimos hacer el trabajo con todos los microservivios porque somos unos capos.

---

## Punto 1: Configuración

Hay que hacer varias cosas para hacer este taller. Primero, hay que tener en cuenta que este taller se debería hacer en local, y no en Azure como siempre. El problema con este enfoque, es que se necesita como un PC de la NASA para poder hacer la ejecución de todo el proyecto.

En vista de esta situación, decidimos hacer la solución en Azure.

La idea del taller es hacer que le proyecto es definir un conjunto de pruebas que se ejecuten antes de hacer el despliegue.


### Jenkins

Jenkins es la herramienta que usaremos para la ejecución de los pipelines. En lugar de definirlos en Actions de GitHub, pues irán acá.

Para la utilización de Jenkins, lo montaremos sobre un contenedor de Docker en local. El **comando** para esto es:

```bash
docker run -d --name jenkins -p 8080:8080 -p 50000:50000 -v jenkins_data:/var/jenkins_home jenkins/jenkins:lts
```

En este comando:

*   `docker run` lanza un nuevo contenedor desde una imagen (`jenkins/jenkins:lts`).
*   `-d` hace que la ejecución se haga en segundo plano y no me bloquee la terminal.
*   `--name jenkins` es la forma de asignarle el nombre al contenedor. Lo que hice fue crear un contenedor que se llama "jenkins".
*   `-p 8080:8080` mapea el puerto 8080 del contenedor al puerto 8080 de mi máquina.
*   `-p 50000:50000` mapea el puerto 50000 del contenedor al 50000 de mi máquina. Este puerto sirve para la comunicación con agentes remotos (no es necesario por ahora, pero es buena práctica dejarlo abierto por si se llega a necesitar).
*   `-v jenkins_data:/var/jenkins_home` es para crear un volumen donde se guardan los datos importantes de Jenkins (así no inicia desde 0 cuando se para el contenedor). `jenkins_data` es el nombre del volumen, y `/var/jenkins_home` es el directorio interno en el contenedor.
*   `jenkins/jenkins:lts` es la imagen oficial de Jenkins desde Docker Hub. `jenkins/jenkins` es el nombre del repositorio. `lts` (Long Term Support) es la versión.

Los **resultados del comando** fueron los siguientes:

![](https://t9013833367.p.clickup-attachments.com/t9013833367/de9075ac-c692-4456-a310-8dc56af9c849/image.png)

![](https://t9013833367.p.clickup-attachments.com/t9013833367/ac362153-96ff-41fc-abb2-8028cb4dabc2/image.png)

![](https://t9013833367.p.clickup-attachments.com/t9013833367/6531a134-04bd-4969-9d23-6dd19bea631f/image.png)

![](https://t9013833367.p.clickup-attachments.com/t9013833367/db21c4f2-3e61-4159-a52c-986c92dca18a/image.png)


Ahora es necesario hacer la configuración del usuario inicial para poder ingresar al panel de administración de Jenkins.

Primero, hay que poner la contraseña que generó:

![](https://t9013833367.p.clickup-attachments.com/t9013833367/3054615b-7ce1-4b1f-bd75-dfebf1523704/image.png)

Eso deja entrar. Ahora le puse "Instalar los plugins sugeridos":

![](https://t9013833367.p.clickup-attachments.com/t9013833367/1ea6291d-5ae6-4a8d-b561-6d7e0e838397/image.png)

![](https://t9013833367.p.clickup-attachments.com/t9013833367/ad8dc61a-9d67-49fb-b288-4cbfa2cc164b/image.png)

![](https://t9013833367.p.clickup-attachments.com/t9013833367/23c11ed5-07bf-4fee-aca6-b8dd5ba0db48/image.png)

![](https://t9013833367.p.clickup-attachments.com/t9013833367/b1fd9612-9b1a-4927-9aeb-233d002285af/image.png)

![](https://t9013833367.p.clickup-attachments.com/t9013833367/cb394821-728f-4226-a376-ad5824131732/image.png)

![](https://t9013833367.p.clickup-attachments.com/t9013833367/45d2a811-f13c-4395-872f-1fc63a3e58ba/image.png)

Además de esto, los pipeliines que usaremos para el despliegue dentro de los ambientes que tenemos necesitan de alunas cosas para poder ejectuarse:

- Kubectl
- az CLI
- docker.io

Para evitar tener que hacer configuraciones muy extensas, optamos por instalar estas herramientas dentro de nuestro contenedor de Jenkins. Para esto, entramos al contendor:

```bash
docker exec -u 0 -it 472838481c5c bash                                                                      
```

Ya estando dentro del contenedor, procedimos a hacer la instalación.

**Ya quedó listo el Jenkins para poder usarlo**.

### Docker

Docker ya está instalado. Algunas cosas que sí que valdría la pena mencionar es que, en caso de querer hacer la solución en local, es necesario hacer un aprovisionamiento de recursos a Docker, de forma que cuente con los suficientes para no quedarse colgado con la carga de tener tantas cosas montadas dentro del clúster en su interior.

Para esto, creamos un archivo en `C:\Users\User` llamado `.wslconfig`:

```yaml
[wsl2]
memory=12GB
processors=4
swap=4GB
localhostForwarding=true
```

Esto nos da la posibilidad de contar con más recursos.


### Kubernetes

Para poder hacer la orquestación de los kubernetes de forma local usaríamos **_Minikube_**. Esta es una herramienta que permite hacer el manejo de los clúster y sus nodos, donde nuestro PC es el clúster.

Para poder hacer el manejo de esto, hay que usar `kubectl` que es la **herramienta de CLI oficial** para la interacción con Kubernetes. **Sirve para conectarse al cluster tanto local** (con Minikube) **como remoto** (en la nube).
  

#### Minikube

Para instalar Minikube, fuimos a la [sección de Inicio de su documentación](https://minikube.sigs.k8s.io/docs/start/?arch=%2Fwindows%2Fx86-64%2Fstable%2F.exe+download), e hice la instalación para Windows:

![](https://t9013833367.p.clickup-attachments.com/t9013833367/c8dcf315-bcae-4834-ba7d-10f2c4e196e6/image.png)

![](https://t9013833367.p.clickup-attachments.com/t9013833367/78bd9568-a9bb-4884-8fbb-0d8fac76b32b/image.png)

Para instalar `kubectl`, fuimos a la [sección de Binarios de la documentación de Kubernetes](https://kubernetes.io/releases/download/#binaries), e hice la instalación para Windows:

![](https://t9013833367.p.clickup-attachments.com/t9013833367/1b144685-7a3f-461e-a2f5-25b33233cf29/image.png)

![](https://t9013833367.p.clickup-attachments.com/t9013833367/08196344-3c07-4830-9417-3ee814f8eac6/image.png)

Ahora sí es momento de iniciar:

```bash
minikube start
```

![](https://t9013833367.p.clickup-attachments.com/t9013833367/a94b3260-b8aa-4c20-b75a-9220318def9f/image.png)

Esto crea un contenedor de Docker:

![](https://t9013833367.p.clickup-attachments.com/t9013833367/6477a5f5-e449-4feb-ac8f-c1e62d14073a/image.png)


#### AKS

A pesar de que mostramos cómo configuramos Minikube para trabajar con un clúster local, ya mencionamos que terminamos por hacer el taller usando Azure. Por tanto, optamos por crear una infraestructura que levantara un Azure Kubernetes Service (AKS), de forma que nos permitiera tener el clúster allí, y hacer el manejo de todo desde allí:

```yaml
resource "azurerm_kubernetes_cluster" "this" {
  name                = var.aks_name
  location            = var.location
  resource_group_name = var.resource_group_name
  dns_prefix          = "${var.aks_name}-dns"

  default_node_pool {
    name       = "default"
    node_count = var.node_count
    vm_size    = var.vm_size
    vnet_subnet_id = var.vnet_subnet_id
  }

  identity {
    type = "SystemAssigned"
  }

  network_profile {
    network_plugin    = "azure"
    load_balancer_sku = "standard"
    service_cidr      = "172.16.0.0/16"
    dns_service_ip    = "172.16.0.10"   
  }

  role_based_access_control_enabled = true

  lifecycle {
    ignore_changes = [
      default_node_pool[0].node_count,
    ]
  }
}
```

Para el manejo de este clúster seguiremos usando `kubectl`.

### Helm

Para facilitar la gestión que haremos del clúster, vamos a instalar Helm.

**Helm** es un **gestor de paquetes para Kubernetes** que facilita la **implementación, configuración y gestión de aplicaciones** en clústeres de Kubernetes. Funciona de forma similar a un gestor de paquetes como `apt` en Ubuntu o `yum` en CentOS, pero específicamente para Kubernetes.

Para instalarlo solo fuimos a su [página](https://helm.sh/docs/intro/install/) y descargué la versión de Windows. Luego solo agregué la ubicación del `.exe` a las variables de entorno.

![](https://t9013833367.p.clickup-attachments.com/t9013833367/7061eb25-991f-4ff4-857c-33163c09c0d5/image.png)

---

## Punto 2: Pipeline para `dev`

### Pipeline

En este punto se definirán los pipelines de construcción de los microservicios. En términos generales, **"construir"** una aplicación en CI/CD significa:

1. **Clonar el código fuente** desde el repositorio (en este caso GitHub).
2. **Instalar dependencias** (ej. Maven o Gradle si los servicios están en Java).
3. **Compilar el código**.
4. **Construir la imagen Docker** del microservicio.
5. **Publicar (opcional)** la imagen en un registry (ej. Docker Hub o GitHub Container Registry).

> Como estamos en el entorno **dev**, en este punto **no se hace aún el despliegue en Kubernetes** ni se ejecutan pruebas. Solo hay que asegurar que el código compila, construye y genera una imagen Docker correctamente.

Sin embargo, creemos que es pertinente tener los microservicios desplegados desde ya en el entorno de desarrollo, pues me permitirá ver si todo va bien, además de que me servirá para los siguientes puntos del taller.

Para la realización de este punto partimos de un script de bash que estábamos usando para el despliegue de todos los microservicios de una vez. Este lo creamos pensando en facilitarnos el trabajo, y terminamos por darnos cuenta de que es nuestros pipeline de despliegue en dev, solo que fuera de Jenkins:

```yaml
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
```

Con esto, creamos un pipeline de Jenkins que usamos para el mismo propósito. La razón de tenerlo en Jenkins es que esta más alineado con lo que pide el taller:

```yaml
pipeline {
    agent any

    environment {
        RESOURCE_GROUP = 'ecommerce-rg'
        AKS_NAME = 'ecommerce-aks'
        K8S_NAMESPACE = 'dev'
    }

    stages {

        stage('Login to Azure') {
            steps {
                withCredentials([azureServicePrincipal('azure-credentials')]) {
                    sh '''
                        echo "Logging into Azure CLI..."
                        az login --service-principal -u $AZURE_CLIENT_ID -p $AZURE_CLIENT_SECRET --tenant $AZURE_TENANT_ID
                        az account show
                    '''
                }
            }
        }

        stage('Login to Docker') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'docker-credentials', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh '''
                        echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                    '''
                }
            }
        }

        stage('Run Unit Tests') {
            steps {
                echo 'Running unit tests...'
                sh './mvnw test'
            }
        }

        stage('Configure AKS Credentials') {
            steps {
                sh '''
                    az aks get-credentials --resource-group "$RESOURCE_GROUP" --name "$AKS_NAME" --overwrite-existing
                '''
            }
        }

        stage('Ensure Namespace Exists') {
            steps {
                sh '''
                    pwd
                    ls -la
                    ls -la k8s
                    ls -la k8s/dev
                    kubectl apply -f k8s/dev/namespace.yml
                '''
            }
        }

        stage('Deploy Service Discovery') {
            steps {
                sh '''
                    kubectl apply -f k8s/dev/service-discovery-deployment.yml -n "$K8S_NAMESPACE"
                    kubectl wait --for=condition=Available deployment/service-discovery --timeout=300s -n "$K8S_NAMESPACE"
                '''
            }
        }

        stage('Deploy Core Services') {
            steps {
                sh 'kubectl apply -f k8s/core/ -n "$K8S_NAMESPACE"'
            }
        }

        stage('Deploy Remaining Dev Services') {
            steps {
                sh '''
                    for file in k8s/dev/*.yml; do
                        case "$file" in
                            *service-discovery-deployment.yml)
                                echo "Skipping $file..."
                                ;;
                            *)
                                echo "Applying $file..."
                                kubectl apply -f "$file" -n "$K8S_NAMESPACE"
                                ;;
                        esac
                    done
                '''
            }
        }


        stage('Wait for All Deployments') {
            steps {
                sh '''
                    echo "Waiting for all deployments to become ready..."
                    for deployment in $(kubectl get deployments -n "$K8S_NAMESPACE" -o jsonpath='{.items[*].metadata.name}'); do
                        echo "Waiting for $deployment..."
                        kubectl wait --for=condition=Available deployment/"$deployment" --timeout=300s -n "$K8S_NAMESPACE" || {
                            echo "Deployment $deployment failed."
                            exit 1
                        }
                    done
                '''
            }
        }

        stage('Run Newman Integration Tests') {
            steps {
                sh '''
                    echo "Waiting for 5 minutes before starting Newman integration tests..."
                    sleep 300
                    kubectl apply -f k8s/newman/newman-job.yml -n "$K8S_NAMESPACE"
                    kubectl wait --for=condition=complete job/newman-integration-tests --timeout=300s -n "$K8S_NAMESPACE" || {
                        echo "Newman tests failed or timed out."
                        exit 1
                    }

                    NEWMAN_POD=$(kubectl get pods -n "$K8S_NAMESPACE" -l job-name=newman-integration-tests -o jsonpath='{.items[0].metadata.name}')
                    if [ -n "$NEWMAN_POD" ]; then
                        echo "Fetching Newman test logs..."
                        kubectl logs "$NEWMAN_POD" -n "$K8S_NAMESPACE"
                    else
                        echo "Newman pod not found."
                    fi
                '''
            }
        }

        stage('Clean Up Newman Job') {
            steps {
                sh 'kubectl delete job newman-integration-tests -n "$K8S_NAMESPACE" || true'
            }
        }
    }

    post {
        always {
            echo 'Cleaning up workspace...'
            deleteDir()
        }

        success {
            echo '✅ Deployment and integration tests completed successfully.'
        }

        failure {
            echo '❌ Pipeline failed. Check logs above.'
        }
    }
}
```

En Jenkins, creamos el pipeline así:

![](https://t9013833367.p.clickup-attachments.com/t9013833367/2dc4577a-1532-4dca-899a-2231931411c8/image.png)![](https://t9013833367.p.clickup-attachments.com/t9013833367/335f0662-2507-480c-9b25-7acb5cfca7f6/image.png)![](https://t9013833367.p.clickup-attachments.com/t9013833367/abc971ae-4c7f-465d-98fa-4bcb77a56562/image.png)


Con esto, quedó perfecto:

![](https://t9013833367.p.clickup-attachments.com/t9013833367/44649b8b-ac0f-4a07-bb2e-696b2f95e75a/image.png)


### Eureka

Todo el sistema usa eureka para poder comunicarse entre sí. Por ende, es necesario hacer el despliegue del servicio, para que cada uno se pueda registrar acá.

Para eso, usamos esta configuración en `service-discovery-deployment.yml` :

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: service-discovery
spec:
  replicas: 1
  selector:
    matchLabels:
      app: service-discovery
  template:
    metadata:
      labels:
        app: service-discovery
    spec:
      containers:
      - name: service-discovery
        image: jpnino/service-discovery:latest
        ports:
        - containerPort: 8761
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: dev
        - name: SPRING_ZIPKIN_BASE_URL
          value: http://zipkin:9411
        - name: SPRING_CONFIG_IMPORT
          value: optional:configserver:http://cloud-config:9296/
        - name: EUREKA_INSTANCE
          value: "service-discovery"
        - name: EUREKA_INSTANCE_HOSTNAME
          value: service-discovery
        - name: EUREKA_INSTANCE_PREFER_IP_ADDRESS
          value: "false"
---
apiVersion: v1
kind: Service
metadata:
  name: service-discovery
spec:
  selector:
    app: service-discovery
  ports:
  - port: 8761
    targetPort: 8761
  type: LoadBalancer
```

Este es uno de los microservicios que se desplegaron con el script de shell, por lo que no es necesario hacer nada más.

Todos los servicios se conectaron al Eureka de forma satisfactoria:

![](https://t9013833367.p.clickup-attachments.com/t9013833367/dbf3f51f-607e-443c-bdbd-e74f03ca5160/image.png)

Teniendo esto, no está de más ver que sí esté bien desplegado:

![](https://t9013833367.p.clickup-attachments.com/t9013833367/85716384-7818-4dba-9d1a-53c312207864/image.png)

![](https://t9013833367.p.clickup-attachments.com/t9013833367/15c0a229-fc5c-401a-85fa-f00c49ae35a4/image.png)

![](https://t9013833367.p.clickup-attachments.com/t9013833367/acbfef5b-1159-464d-ad07-55337aa66b33/image.png)

---
  
## Punto 3: Pruebas

En este punto hay que implementar las pruebas del código para validar que todo vaya bien.

  
### Pruebas Unitarias

Inicialmente, realizaremos las pruebas unitarias para dos de los microservicios que desplegamos dentro del entorno de dev.


#### Servicio: user-service

Hay que decir, inicialmente, que cada microservicio tiene varios endpoints de CRUD para distintas cosas. Como debemos hacer 5 de cada tipo de prueba, entonces optamos por probar las servicios principales (al menos con las unitarias).

Para este microservicio haremos las pruebas unitarias que validen el buen funcionamiento de `UserServiceImpl`, que contiene el CRUD para los usuarios del sistema. Por ejemplo:

![](https://t9013833367.p.clickup-attachments.com/t9013833367/607dff46-c919-4e84-87e3-ee7d0378fbca/image.png)

Para esto, creamos `UserServiceImplTests` dentro de la carpeta `test\app\com\selimhorri\service\impl\`.

Test1:

```java
    @Test
    void findAll_shouldReturnListOfUsers() {
        // Arrange
        when(userRepository.findAll()).thenReturn(Arrays.asList(user));
        
        // Act
        List<UserDto> result = userService.findAll();
        
        // Assert
        assertNotNull(result, "Result list should not be null");
        assertEquals(1, result.size(), "Should return one user");
        assertEquals(DEFAULT_FIRST_NAME, result.get(0).getFirstName(), "First name should match");
        assertEquals(DEFAULT_LAST_NAME, result.get(0).getLastName(), "Last name should match");
        assertEquals(DEFAULT_EMAIL, result.get(0).getEmail(), "Email should match");
        
        // Verify credential mapping
        assertNotNull(result.get(0).getCredentialDto(), "Credential should not be null");
        assertEquals(DEFAULT_USERNAME, result.get(0).getCredentialDto().getUsername(), "Username should match");
        
        verify(userRepository, times(1)).findAll();
    }
```

Test2:

```java
    @Test
    void findById_whenUserExists_shouldReturnUser() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        
        // Act
        UserDto result = userService.findById(1);
        
        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(1, result.getUserId(), "User ID should match");
        assertEquals(DEFAULT_FIRST_NAME, result.getFirstName(), "First name should match");
        assertEquals(DEFAULT_LAST_NAME, result.getLastName(), "Last name should match");
        assertEquals(DEFAULT_EMAIL, result.getEmail(), "Email should match");
        
        // Verify credential mapping
        assertNotNull(result.getCredentialDto(), "Credential should not be null");
        assertEquals(DEFAULT_USERNAME, result.getCredentialDto().getUsername(), "Username should match");
        
        verify(userRepository, times(1)).findById(1);
    }
```

Test3:

```java
    @Test
    void findById_whenUserDoesNotExist_shouldThrowException() {
        // Arrange
        when(userRepository.findById(999)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(UserObjectNotFoundException.class, () -> 
            userService.findById(999),
            "Should throw UserObjectNotFoundException"
        );
        verify(userRepository, times(1)).findById(999);
    }
```

Test4:

```java
    @Test
    void save_shouldReturnSavedUser() {
        // Arrange
        when(userRepository.save(any(User.class))).thenReturn(user);
        
        // Act
        UserDto result = userService.save(userDto);
        
        // Assert
        assertNotNull(result, "Saved user should not be null");
        assertEquals(userDto.getUserId(), result.getUserId(), "User ID should match");
        assertEquals(userDto.getFirstName(), result.getFirstName(), "First name should match");
        assertEquals(userDto.getEmail(), result.getEmail(), "Email should match");
        
        // Verify credential mapping
        assertNotNull(result.getCredentialDto(), "Credential should not be null");
        assertEquals(DEFAULT_USERNAME, result.getCredentialDto().getUsername(), "Username should match");
        
        verify(userRepository, times(1)).save(any(User.class));
    }
```

Test5:

```java
    @Test
    void findByUsername_whenUserExists_shouldReturnUser() {
        // Arrange
        when(userRepository.findByCredentialUsername(DEFAULT_USERNAME)).thenReturn(Optional.of(user));
        
        // Act
        UserDto result = userService.findByUsername(DEFAULT_USERNAME);
        
        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(DEFAULT_FIRST_NAME, result.getFirstName(), "First name should match");
        assertEquals(DEFAULT_LAST_NAME, result.getLastName(), "Last name should match");
        assertEquals(DEFAULT_EMAIL, result.getEmail(), "Email should match");
        
        // Verify credential mapping
        assertNotNull(result.getCredentialDto(), "Credential should not be null");
        assertEquals(DEFAULT_USERNAME, result.getCredentialDto().getUsername(), "Username should match");
        
        verify(userRepository, times(1)).findByCredentialUsername(DEFAULT_USERNAME);
    }
```

Test6:

```java
    @Test 
    void deleteById_shouldCallRepositoryDeleteById() {
        // Arrange
        Integer userId = 1;
        doNothing().when(userRepository).deleteById(userId);
        
        // Act
        userService.deleteById(userId);
        
        // Assert
        verify(userRepository, times(1)).deleteById(userId);
    }
```

Haciendo las pruebas, notamos que el update de user-service estaba incorrecto, pues no estaba actualizando nada. Entonces lo corregí. Después, todo funcionó bien:

```bash
.\mvnw -Dtest=UserServiceImplTests test
```

![](https://t9013833367.p.clickup-attachments.com/t9013833367/25ff093b-f32f-4d90-b5b4-68838072f065/image.png)


#### Servicio: product-service

Para este microservicio voy a haremos pruebas unitarias que validen el buen funcionamiento de `ProductServiceImpl`, que contiene el CRUD para los productos del sistema. Por ejemplo:

![](https://t9013833367.p.clickup-attachments.com/t9013833367/0536ae94-4942-4c5b-ae07-e2f182680763/image.png)

Para esto, creamos `ProductServiceImplTest` dentro de la carpeta `test\app\com\selimhorri\service\impl`.

Test1:

```java
    @Test
    void findAll_shouldReturnListOfProducts() {
        // Arrange
        when(productRepository.findAll()).thenReturn(Arrays.asList(product));
        
        // Act
        List<ProductDto> result = productService.findAll();
        
        // Assert
        assertNotNull(result, "Result list should not be null");
        assertEquals(1, result.size(), "Should return one product");
        assertEquals(DEFAULT_PRODUCT_TITLE, result.get(0).getProductTitle(), "Product title should match");
        assertEquals(DEFAULT_SKU, result.get(0).getSku(), "SKU should match");
        assertEquals(DEFAULT_PRICE, result.get(0).getPriceUnit(), "Price should match");
        
        // Verify category mapping
        assertNotNull(result.get(0).getCategoryDto(), "Category should not be null");
        assertEquals(DEFAULT_CATEGORY_TITLE, result.get(0).getCategoryDto().getCategoryTitle(), "Category title should match");
        
        verify(productRepository, times(1)).findAll();
    }
```

Test2:

```java
    @Test
    void findById_whenProductExists_shouldReturnProduct() {
        // Arrange
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        
        // Act
        ProductDto result = productService.findById(1);
        
        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(1, result.getProductId(), "Product ID should match");
        assertEquals(DEFAULT_PRODUCT_TITLE, result.getProductTitle(), "Product title should match");
        assertEquals(DEFAULT_SKU, result.getSku(), "SKU should match");
        assertEquals(DEFAULT_PRICE, result.getPriceUnit(), "Price should match");
        
        // Verify category mapping
        assertNotNull(result.getCategoryDto(), "Category should not be null");
        assertEquals(DEFAULT_CATEGORY_TITLE, result.getCategoryDto().getCategoryTitle(), "Category title should match");
        
        verify(productRepository, times(1)).findById(1);
    }
```

Test3:

```java
    void findById_whenProductDoesNotExist_shouldThrowException() {
        // Arrange
        when(productRepository.findById(999)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ProductNotFoundException.class, () -> 
            productService.findById(999),
            "Should throw ProductNotFoundException"
        );
        verify(productRepository, times(1)).findById(999);
    }
```

Test4:

```java
    @Test
    void save_shouldReturnSavedProduct() {
        // Arrange
        when(productRepository.save(any(Product.class))).thenReturn(product);
        
        // Act
        ProductDto result = productService.save(productDto);
        
        // Assert
        assertNotNull(result, "Saved product should not be null");
        assertEquals(productDto.getProductId(), result.getProductId(), "Product ID should match");
        assertEquals(productDto.getProductTitle(), result.getProductTitle(), "Product title should match");
        assertEquals(productDto.getSku(), result.getSku(), "SKU should match");
        assertEquals(productDto.getPriceUnit(), result.getPriceUnit(), "Price should match");
        
        // Verify category mapping
        assertNotNull(result.getCategoryDto(), "Category should not be null");
        assertEquals(DEFAULT_CATEGORY_TITLE, result.getCategoryDto().getCategoryTitle(), "Category title should match");
        
        verify(productRepository, times(1)).save(any(Product.class));
    }
```

Test5:

```java
    @Test
    void update_shouldReturnUpdatedProduct() {
        // Arrange
        Product updatedProduct = Product.builder()
            .productId(1)
            .productTitle("Updated Product")
            .imageUrl(DEFAULT_IMAGE_URL)
            .sku(DEFAULT_SKU)
            .priceUnit(149.99)
            .quantity(DEFAULT_QUANTITY)
            .category(category)
            .build();
        updatedProduct.setCreatedAt(product.getCreatedAt());
        updatedProduct.setUpdatedAt(product.getUpdatedAt());
        
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);
        
        // Update the DTO
        productDto.setProductTitle("Updated Product");
        productDto.setPriceUnit(149.99);
        
        // Act
        ProductDto result = productService.update(productDto);
        
        // Assert
        assertNotNull(result, "Updated product should not be null");
        assertEquals("Updated Product", result.getProductTitle(), "Product title should be updated");
        assertEquals(149.99, result.getPriceUnit(), "Price should be updated");
        assertEquals(productDto.getSku(), result.getSku(), "SKU should remain unchanged");
        
        verify(productRepository, times(1)).save(any(Product.class));
    }
    
    @Test
    void deleteById_shouldCallRepositoryDeleteById() {
        // Arrange
        Integer productId = 1;
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        
        // Act
        productService.deleteById(productId);
        
        // Assert
        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, times(1)).delete(any(Product.class));
    }
```

Los resultados de las pruebas fueron:

```bash
.\mvnw -Dtest=ProductServiceImplTests test
```

![](https://t9013833367.p.clickup-attachments.com/t9013833367/4402f557-0c8d-487e-af20-94b14a52e426/image.png)


### Pruebas de Integración

Las pruebas de integración las realizamos para los mismos dos microservicios para los que hicimos unitarias: `user-service` y `produt-service`.

En estas pruebas de integración, nos aseguramos de que los controladores de estos dos servicios estén funcionando bien.


#### Servicio: user-service

Las pruebas que implementamos validan el comportamiento del **servicio** **`UserService`** con acceso real a la base de datos en un entorno de Spring Boot cargado con perfil `test`, utilizando un repositorio real (no mockeado).

Estas pruebas quedaron en `test\java\com\selimhorri\app\service\UserServiceIntegrationTests.java`.

Test1:

```java
    @Test
    void saveUser_shouldPersistUser() {
        UserDto savedUserDto = userService.save(userDto);

        assertThat(savedUserDto).isNotNull();
        assertThat(savedUserDto.getUserId()).isNotNull();
        assertThat(savedUserDto.getFirstName()).isEqualTo(DEFAULT_FIRST_NAME);
        assertThat(savedUserDto.getLastName()).isEqualTo(DEFAULT_LAST_NAME);
        assertThat(savedUserDto.getEmail()).isEqualTo(DEFAULT_EMAIL);
        assertThat(savedUserDto.getPhone()).isEqualTo(DEFAULT_PHONE);
        assertThat(savedUserDto.getImageUrl()).isEqualTo(DEFAULT_IMAGE_URL);
        assertThat(savedUserDto.getCredentialDto().getUsername()).isEqualTo(DEFAULT_USERNAME);

        Optional<User> repoUser = userRepository.findById(savedUserDto.getUserId());
        assertThat(repoUser).isPresent();
        assertThat(repoUser.get().getEmail()).isEqualTo(DEFAULT_EMAIL);
    }
```

Test2:

```java
    @Test
    void updateUser_shouldModifyExistingUser() {
        User savedEntity = userRepository.saveAndFlush(user);

        UserDto dtoToUpdate = UserDto.builder()
                .userId(savedEntity.getUserId())
                .firstName(UPDATED_FIRST_NAME)
                .lastName(UPDATED_LAST_NAME)
                .email(UPDATED_EMAIL)
                .phone(savedEntity.getPhone())
                .imageUrl(savedEntity.getImageUrl())
                .credentialDto(CredentialDto.builder()
                        .credentialId(savedEntity.getCredential().getCredentialId())
                        .username(savedEntity.getCredential().getUsername())
                        .password(savedEntity.getCredential().getPassword())
                        .isEnabled(savedEntity.getCredential().getIsEnabled())
                        .isAccountNonExpired(savedEntity.getCredential().getIsAccountNonExpired())
                        .isAccountNonLocked(savedEntity.getCredential().getIsAccountNonLocked())
                        .isCredentialsNonExpired(savedEntity.getCredential().getIsCredentialsNonExpired())
                        .build())
                .build();

        UserDto updatedDto = userService.update(dtoToUpdate);

        assertThat(updatedDto.getFirstName()).isEqualTo(UPDATED_FIRST_NAME);
        assertThat(updatedDto.getLastName()).isEqualTo(UPDATED_LAST_NAME);
        assertThat(updatedDto.getEmail()).isEqualTo(UPDATED_EMAIL);
    }
```

Test3:

```java
    @Test
    void deleteUserById_shouldRemoveUserFromDatabase() {
        User savedEntity = userRepository.saveAndFlush(user);
        Integer userIdToDelete = savedEntity.getUserId();

        assertThat(userRepository.findById(userIdToDelete)).isPresent();

        userService.deleteById(userIdToDelete);

        assertThat(userRepository.findById(userIdToDelete)).isNotPresent();
    }
```

Test4:

```java
    @Test
    void findUserById_whenExists_shouldReturnUser() {
        User savedEntity = userRepository.saveAndFlush(user);
        Integer userIdToFind = savedEntity.getUserId();

        UserDto foundDto = userService.findById(userIdToFind);

        assertThat(foundDto).isNotNull();
        assertThat(foundDto.getUserId()).isEqualTo(userIdToFind);
        assertThat(foundDto.getFirstName()).isEqualTo(DEFAULT_FIRST_NAME);
    }
```

Test5:

```java
    @Test
    void findUserById_whenNotExists_shouldThrowUserObjectNotFoundException() {
        Integer nonExistentId = -999;
        Exception exception = assertThrows(UserObjectNotFoundException.class, () -> {
            userService.findById(nonExistentId);
        });

        assertThat(exception.getMessage()).contains("User with id: " + nonExistentId + " not found");
    }

    @Test
    void findByUsername_whenExists_shouldReturnUser() {
        userRepository.saveAndFlush(user);

        UserDto foundDto = userService.findByUsername(DEFAULT_USERNAME);

        assertThat(foundDto).isNotNull();
        assertThat(foundDto.getCredentialDto().getUsername()).isEqualTo(DEFAULT_USERNAME);
    }

    @Test
    void findByUsername_whenNotExists_shouldThrowUserObjectNotFoundException() {
        String invalidUsername = "unknown_user";
        Exception exception = assertThrows(UserObjectNotFoundException.class, () -> {
            userService.findByUsername(invalidUsername);
        });

        assertThat(exception.getMessage()).contains("User with username: " + invalidUsername + " not found");
    }
```

Test6:

```java
    @Test
    void findAll_shouldReturnAllUsers() {
        userRepository.saveAndFlush(user); // User 1

        Credential credential2 = Credential.builder()
                .username("carlap")
                .password("pw")
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();

        User user2 = User.builder()
                .firstName("Carla")
                .lastName("Perez")
                .email("carla.perez@example.com")
                .phone("777888999")
                .imageUrl("http://example.com/carla.jpg")
                .credential(credential2)
                .build();

        credential2.setUser(user2);
        userRepository.saveAndFlush(user2); // User 2

        List<UserDto> users = userService.findAll();

        assertThat(users).isNotNull();
        assertThat(users.size()).isEqualTo(2);
        assertThat(users.stream().anyMatch(u -> u.getFirstName().equals(DEFAULT_FIRST_NAME))).isTrue();
        assertThat(users.stream().anyMatch(u -> u.getFirstName().equals("Carla"))).isTrue();
    }
```

El resultado de las pruebas fue:

```bash
.\mvnw -Dtest=UserServiceIntegrationTests test
```

![](https://t9013833367.p.clickup-attachments.com/t9013833367/4b3f7896-2b1f-4faa-8c43-deff43205ae9/image.png)


#### Servicio: product-service

Las pruebas que implementamos validan el comportamiento del **servicio** **`ProductService`** con acceso real a la base de datos en un entorno de Spring Boot cargado con perfil `test`, utilizando un repositorio real (no mockeado).

Estas pruebas quedaron en `test\java\com\selimhorri\app\service\`[`ProductServiceIntegrationTests.java`](http://ProductServiceIntegrationTests.java).

Test1:

```java
     @Test
    void saveProduct_shouldPersistProduct() {
        ProductDto savedProductDto = productService.save(productDto);

        assertThat(savedProductDto).isNotNull();
        assertThat(savedProductDto.getProductId()).isNotNull();
        assertThat(savedProductDto.getProductTitle()).isEqualTo(DEFAULT_PRODUCT_TITLE);
        assertThat(savedProductDto.getSku()).isEqualTo(DEFAULT_SKU);
        assertThat(savedProductDto.getPriceUnit()).isEqualTo(DEFAULT_PRICE);
        assertThat(savedProductDto.getQuantity()).isEqualTo(DEFAULT_QUANTITY);
        assertThat(savedProductDto.getCategoryDto().getCategoryTitle()).isEqualTo(DEFAULT_CATEGORY_TITLE);

        Optional<Product> repoProduct = productRepository.findById(savedProductDto.getProductId());
        assertThat(repoProduct).isPresent();
        assertThat(repoProduct.get().getSku()).isEqualTo(DEFAULT_SKU);
    }
```

Test2:

```java
    @Test
    void updateProduct_shouldModifyExistingProduct() {
        Product savedEntity = productRepository.saveAndFlush(product);

        ProductDto dtoToUpdate = ProductDto.builder()
                .productId(savedEntity.getProductId())
                .productTitle(UPDATED_PRODUCT_TITLE)
                .imageUrl(savedEntity.getImageUrl())
                .sku(savedEntity.getSku())
                .priceUnit(UPDATED_PRICE)
                .quantity(UPDATED_QUANTITY)
                .categoryDto(CategoryDto.builder()
                        .categoryId(savedEntity.getCategory().getCategoryId())
                        .categoryTitle(savedEntity.getCategory().getCategoryTitle())
                        .imageUrl(savedEntity.getCategory().getImageUrl())
                        .build())
                .build();

        ProductDto updatedDto = productService.update(dtoToUpdate);

        assertThat(updatedDto.getProductTitle()).isEqualTo(UPDATED_PRODUCT_TITLE);
        assertThat(updatedDto.getPriceUnit()).isEqualTo(UPDATED_PRICE);
        assertThat(updatedDto.getQuantity()).isEqualTo(UPDATED_QUANTITY);
    }
```

Test3:

```java
    @Test
    void deleteProductById_shouldRemoveProductFromDatabase() {
        Product savedEntity = productRepository.saveAndFlush(product);
        Integer productIdToDelete = savedEntity.getProductId();

        assertThat(productRepository.findById(productIdToDelete)).isPresent();

        productService.deleteById(productIdToDelete);

        assertThat(productRepository.findById(productIdToDelete)).isNotPresent();
    }
```

Test4:

```java
    @Test
    void findProductById_whenExists_shouldReturnProduct() {
        Product savedEntity = productRepository.saveAndFlush(product);
        Integer productIdToFind = savedEntity.getProductId();

        ProductDto foundDto = productService.findById(productIdToFind);

        assertThat(foundDto).isNotNull();
        assertThat(foundDto.getProductId()).isEqualTo(productIdToFind);
        assertThat(foundDto.getProductTitle()).isEqualTo(DEFAULT_PRODUCT_TITLE);
        assertThat(foundDto.getSku()).isEqualTo(DEFAULT_SKU);
    }
```

Test5:

```java
    @Test
    void findAll_shouldReturnAllProducts() {
        productRepository.saveAndFlush(product); // Product 1        
        Category category2 = Category.builder()
                .categoryTitle("Accessories")
                .imageUrl("http://example.com/accessories.jpg")
                .build();
        category2 = categoryRepository.save(category2);

        Product product2 = Product.builder()
                .productTitle("Gaming Mouse")
                .imageUrl("http://example.com/mouse.jpg")
                .sku("MOU-2023-001")
                .priceUnit(79.99)
                .quantity(100)
                .category(category2)
                .build();

        productRepository.saveAndFlush(product2); // Product 2

        List<ProductDto> products = productService.findAll();

        assertThat(products).isNotNull();
        assertThat(products.size()).isEqualTo(2);
        assertThat(products.stream().anyMatch(p -> p.getProductTitle().equals(DEFAULT_PRODUCT_TITLE))).isTrue();
        assertThat(products.stream().anyMatch(p -> p.getProductTitle().equals("Gaming Mouse"))).isTrue();
    }
```

  

Los resultados fueron:

```bash
.\mvnw -Dtest=ProductServiceIntegrationTests test
```

![](https://t9013833367.p.clickup-attachments.com/t9013833367/718fbcba-deba-4ef3-9af0-461a2a3b6112/image.png)

  

### Pruebas End2End

Para las pruebas End to End creamos una colección en Postman con el fluj completo de la aplicación, de forma que podamos ver que esté funcionando todo en conjnto.

  

La colección es:

![](https://t9013833367.p.clickup-attachments.com/t9013833367/5cd757d5-45e4-4d8b-8495-10ee099a2add/image.png)

![](https://t9013833367.p.clickup-attachments.com/t9013833367/05af3cc6-f37c-4eca-8cfb-871290ff93a9/image.png)

![](https://t9013833367.p.clickup-attachments.com/t9013833367/d0347a8b-14fb-4e60-a840-64b2473817bf/image.png)

  

El resultado de las pruebas fue:

![](https://t9013833367.p.clickup-attachments.com/t9013833367/05d7eadc-abd0-4047-9193-608089150cce/image.png)

  

### Pruebas de estrés

Para la ejecución de las pruebas de estrés, usamos Locust:

![](https://t9013833367.p.clickup-attachments.com/t9013833367/adb5f1fa-1249-40c8-b804-c3efa4a06a57/image.png)

  

Los charts nos indican el desempeño de la aplicación:

![](https://t9013833367.p.clickup-attachments.com/t9013833367/a1724272-3fe3-4b32-aa92-2fb420cd23d1/image.png)![](https://t9013833367.p.clickup-attachments.com/t9013833367/edb1e91e-9efe-404d-92cb-a568bd73cbaa/image.png)