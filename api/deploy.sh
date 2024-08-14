#!/bin/sh

gcloud run services update starbornag \
--region us-east1 \
--image us-east1-docker.pkg.dev/fresh-balancer-423321-v5/starbornag/starbornag:latest
