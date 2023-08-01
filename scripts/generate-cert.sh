#!/bin/bash
set -e

LAUNCH_DIR=$(pwd -P) # where ran from
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd -P)" # where the script resides
OUT_DIR=${SCRIPT_DIR}/../src/main/resources/generated

# Step 1: Create Root CA
openssl genpkey -algorithm RSA -out $OUT_DIR/rootca.key
openssl req -new -x509 -key $OUT_DIR/rootca.key -out $OUT_DIR/rootca.crt \
  -subj "/C=US/ST=CA/L=SF/O=Acme Inc /OU=Test Team/CN=acme.com"


# Step 2: Generate Private Key and Certificate Signing Request (CSR)
openssl genpkey -algorithm RSA -out $OUT_DIR/certificate.key
openssl req -new -key $OUT_DIR/certificate.key -out $OUT_DIR/certificate.csr \
  -subj "/C=US/ST=CA/L=SF/O=Acme Inc /OU=Test Team/CN=acme.com"

# Step 3: Sign the Certificate
openssl x509 -req -in $OUT_DIR/certificate.csr -CA $OUT_DIR/rootca.crt -CAkey $OUT_DIR/rootca.key -CAcreateserial -out $OUT_DIR/certificate.crt

# Step 4: Extract the Public Key
openssl rsa -pubout -in $OUT_DIR/certificate.key -out $OUT_DIR/certificate.pub

# Display success message
echo "Certificate and Key Generation Completed!"
