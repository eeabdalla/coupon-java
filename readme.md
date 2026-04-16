# Cleanup-Service

## Project description

This project is a simple implementation of all relevant technologies used in the STAR project.
It contains e.g. Docker, Bruno, Spring Web, Spring Data MongoDB and Spring Schedulers.

## Setup local development environment

### Precondition:

- Local Podman (https://podman.io) or Docker available
- Bruno installed (https://www.usebruno.com)
- MongoDB Compass (https://www.mongodb.com/products/tools/compass)

1. Provide the MongoDB through docker-compose with your preferred tool (Podman/Docker)
2. Run CouponServiceApplication (run configuration for IntelliJ provided, alternative: use profile local to start)
3. Import coupon-api as Bruno collection