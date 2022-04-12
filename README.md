# Getting Started


```shell
docker login ghcr.io -u chxxx.txxx@gmail.com -p $PAT
```

```shell
./mvnw clean install -DskipTests
docker build -t ghcr.io/logaritex/stream-data-generator:latest .
docker push ghcr.io/logaritex/stream-data-generator:latest
```

```shell
./mvnw clean spring-boot:build-image -Dspring-boot.build-image.imageName=ghcr.io/logaritex/stream-data-generator -DskipTests
docker push ghcr.io/logaritex/stream-data-generator:latest
```