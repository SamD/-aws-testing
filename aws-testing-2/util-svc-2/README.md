```shell
# quarkus dev local
mvn quarkus:dev 

# sam local, aws-sam-cli
quarkus build
sam local start-api --template target/sam.jvm.yaml

# native 
quarkus build --native --no-tests -Dquarkus.native.container-build=true -Dquarkus.native.builder-image=graalvm
sam local start-api --template target/sam.native.yaml

```