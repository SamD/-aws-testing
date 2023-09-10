package org.aws.localstack;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class UploadToS3 {
    public static void main(String[] args) {
        final String projectBasedir = System.getProperty(Constants.PROJECT_DIR);
        final String resourcesDir = projectBasedir + "/src/main/resources/generated";

        // AWS S3 bucket details
        final String envRegion = System.getProperty(Constants.ENV_REGION);
        final String envCertS3Bucket = System.getProperty(Constants.ENV_CERT_S3_BUCKET);
        final String envSdtUrl = System.getProperty(Constants.ENV_SDT_URI);

        final String s3Endpoint = System.getProperty(Constants.S3_ENDPOINT);

        // Paths to your public key, private key, and certificate files
        final String publicKeyPath = resourcesDir + "/certificate.pub";
        final String privateKeyPath = resourcesDir + "/certificate.key";
        final String certificatePath = resourcesDir + "/certificate.crt";

        System.out.println("Project Base Directory: " + projectBasedir);
        System.out.println("Resource Directory: " + resourcesDir);
        System.out.println("Region: " + envRegion);
        System.out.println("S3CertBucket: " + envCertS3Bucket);
        System.out.println("Public Key: " + publicKeyPath);
        System.out.println("Private Key: " + privateKeyPath);
        System.out.println("Certificate: " + certificatePath);

        System.out.println("S3 Endpoint: " + s3Endpoint);

        // Initialize AWS S3 client
        S3Client s3Client = S3Client.builder()
                .endpointOverride(URI.create(s3Endpoint))
                .region(Region.of(envRegion))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .forcePathStyle(true)
                .build();

        setCorsInformation(s3Client, envCertS3Bucket);

        // Upload the public key
        uploadFile(s3Client, envCertS3Bucket, "certificate.pub", publicKeyPath);
        // Upload the private key
        uploadFile(s3Client, envCertS3Bucket, "certificate.key", privateKeyPath);
        // Upload the certificate
        uploadFile(s3Client, envCertS3Bucket, "certificate.crt", certificatePath);

        // Shutdown the S3 client
        s3Client.close();
    }

    private static void uploadFile(final S3Client s3Client, final String bucketName, final String objectKey, final String filePath) {
        final PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();
        s3Client.putObject(putObjectRequest, RequestBody.fromFile(new File(filePath)));
        System.out.println("Uploaded " + objectKey + " to S3 bucket: " + bucketName);
    }

    private static void setCorsInformation(final S3Client s3Client, final String bucketName) {
        final List<String> allowMethods = new ArrayList<>();
        allowMethods.add("GET");
        allowMethods.add("PUT");
        allowMethods.add("HEAD");
        allowMethods.add("POST");
        allowMethods.add("DELETE");

        final List<String> allowOrigins = new ArrayList<>();
        allowOrigins.add("*");
        try {
            // Define CORS rules.
            final CORSRule corsRule = CORSRule.builder()
                    .allowedMethods(allowMethods)
                    .allowedOrigins(allowOrigins)
                    .build();

            final List<CORSRule> corsRules = new ArrayList<>();
            corsRules.add(corsRule);
            final CORSConfiguration configuration = CORSConfiguration.builder()
                    .corsRules(corsRules)
                    .build();

            PutBucketCorsRequest putBucketCorsRequest = PutBucketCorsRequest.builder()
                    .bucket(bucketName)
                    .corsConfiguration(configuration)
                    .build();

            s3Client.putBucketCors(putBucketCorsRequest);

        } catch (final S3Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }
}
