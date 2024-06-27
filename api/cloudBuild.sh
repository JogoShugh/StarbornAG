#!/bin/sh

#docker buildx build --platform linux/amd64 -t starbornag:latest .
docker build --platform linux/amd64 -t starbornag:latest .

#docker build -t starbornag:latest .

# from web: https://us-east1-docker.pkg.dev/fresh-balancer-423321-v5/starbornag/starbornag@sha256:cbb3f54bfb009755510a7bbbc09b4dad2176f7653e64a070ec2309e17b303a8e

#docker tag starbornag:latest us-east1-docker.pkg.dev/fresh-balancer-423321-v5/starbornag:latest 
#docker push us-east1-docker.pkg.dev/fresh-balancer-423321-v5/starbornag:latest

docker tag starbornag:latest us-east1-docker.pkg.dev/fresh-balancer-423321-v5/starbornag/starbornag:latest 
docker push us-east1-docker.pkg.dev/fresh-balancer-423321-v5/starbornag/starbornag:latest

