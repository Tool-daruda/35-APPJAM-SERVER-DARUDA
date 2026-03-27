package com.daruda.darudaserver.global.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.oracle.bmc.auth.SimpleAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.ObjectStorageClient;

@Configuration
public class OciConfig {

	@Value("${oci.tenant-id}")
	private String tenantId;

	@Value("${oci.user-id}")
	private String userId;

	@Value("${oci.fingerprint}")
	private String fingerprint;

	@Value("${oci.private-key-path}")
	private String privateKeyPath;

	@Value("${oci.region}")
	private String region;

	@Value("${oci.bucket.namespace}")
	private String bucketNamespace;

	@Bean
	public ObjectStorage objectStorageClient() {
		SimpleAuthenticationDetailsProvider provider = createAuthenticationProvider();
		ObjectStorageClient client = createStorageClient(provider);

		String endpoint = String.format("https://%s.objectstorage.%s.oci.customer-oci.com", bucketNamespace, region);
		client.setEndpoint(endpoint);
		return client;
	}

	private SimpleAuthenticationDetailsProvider createAuthenticationProvider() {
		return SimpleAuthenticationDetailsProvider.builder()
			.tenantId(tenantId)
			.userId(userId)
			.fingerprint(fingerprint)
			.privateKeySupplier(createPrivateKeySupplier())
			.build();
	}

	private Supplier<InputStream> createPrivateKeySupplier() {
		return () -> {
			try {
				return new FileInputStream(privateKeyPath);
			} catch (FileNotFoundException e) {
				throw new RuntimeException("Private key file not found at " + privateKeyPath, e);
			}
		};
	}

	private ObjectStorageClient createStorageClient(SimpleAuthenticationDetailsProvider provider) {
		return ObjectStorageClient.builder().build(provider);
	}
}
