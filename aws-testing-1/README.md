Only intention of this repo is to experiment with aws sdks and localstack , it is not 
meant for public consumption

### Generate Certs
This requires `openssl` to be installed
```shell
./scripts/generate-cert.sh
```

### Setup Localstack
**Note Localstack must be running:** [Installing Localstack](https://docs.localstack.cloud/getting-started/installation/)
```shell
asdf plugin add awscli-local
python3 -m venv aws-venv
. ./aws-venv/bin/activate
pip install boto3
pip install terraform-local

tflocal -chdir=./infrastructure init
tflocal -chdir=./infrastructure apply -auto-approve

# check if cert bucket was created successfully 
awslocal s3 ls s3://
# certs bucket should be listed

# For cleanup afterwards
tflocal -chdir=./infrastructure destroy -auto-approve
```
### Upload 
```shell
mvn clean install -DskipTests

AWS_ACCESS_KEY_ID=test AWS_SECRET_ACCESS_KEY=test \
mvn clean install exec:java -Dexec.mainClass=org.aws.localstack.UploadToS3
```