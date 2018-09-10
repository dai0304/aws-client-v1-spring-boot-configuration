/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jp.xet.spring.aws.autoconfigure;

import java.util.HashMap;

import lombok.Data;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;

/**
 * Spring auto-configuration for AWS Clients.
 * 
 * <p>See {@code META-INF/aws.builders}</p>
 * 
 * <ul>
 *     <li>{@code aws.<service-package-name>[-async].client.<property>} - the {@link ClientConfiguration} to be used
 *         by the client. (ClientConfiguration)</li>
 *     <li>{@code aws.<service-package-name>[-async].endpoint.service-endpoint} - The service endpoint either with
 *         or without the protocol (e.g. https://sns.us-west-1.amazonaws.com or sns.us-west-1.amazonaws.com) (string)</li>
 *     <li>{@code aws.<service-package-name>[-async].endpoint.signing-region} - the region to use
 *         for SigV4 signing of requests (e.g. us-west-1) (string)</li>
 *     <li>{@code aws.<service-package-name>[-async].region} - the region to be used by the client.
 *         This will be used to determine both the service endpoint (eg: https://sns.us-west-1.amazonaws.com)
 *         and signing region (eg: us-west-1) for requests.
 *         This value is used only if any endpoint configuration is not set. (string)</li>
 *     <li>{@code aws.<service-package-name>[-async].enabled} - (boolean)</li>
 * </ul>
 * 
 * <h3>Default client configurations.</h3>
 * 
 * <ul>
 *     <li>{@code aws.default[-async].client.<property>} - the {@link ClientConfiguration} to be used
 *         by the client. (ClientConfiguration)</li>
 *     <li>{@code aws.default[-async].endpoint.service-endpoint} - The service endpoint either with
 *         or without the protocol (e.g. https://sns.us-west-1.amazonaws.com or sns.us-west-1.amazonaws.com) (string)</li>
 *     <li>{@code aws.default[-async].endpoint.signing-region} - the region to use
 *         for SigV4 signing of requests (e.g. us-west-1) (string)</li>
 *     <li>{@code aws.default[-async].region} - the region to be used by the client.
 *         This will be used to determine both the service endpoint (eg: https://sns.us-west-1.amazonaws.com)
 *         and signing region (eg: us-west-1) for requests.
 *         This value is used only if any endpoint configuration is not set. (string)</li>
 * </ul>
 * 
 * <h3>S3 client specific configurations.</h3>
 * 
 * <ul>
 *     <li>{@code aws.s3.path-style-access-enabled} - Configures the client to use path-style access
 *         for all requests. (boolean)</li>
 *     <li>{@code aws.s3.chunked-encoding-disabled} - Configures the client to disable chunked encoding
 *         for all requests. (boolean)</li>
 *     <li>{@code aws.s3.accelerate-mode-enabled} - Configures the client to use S3 accelerate endpoint
 *         for all requests. (boolean)</li>
 *     <li>{@code aws.s3.payload-signing-enabled} - Configures the client to sign payloads in all situations. (boolean)</li>
 *     <li>{@code aws.s3.dualstack-enabled} - Configures the client to use Amazon S3 dualstack mode
 *         for all requests. (boolean)</li>
 *     <li>{@code aws.s3.force-global-bucket-access-enabled} - Configure whether global bucket access is enabled
 *         for clients generated by this builder. (boolean)</li>
 * </ul>
 *
 * @author miyamoto.daisuke
 * @since #version#
 */
@Configuration
public class AwsAutoConfiguration {
	
	@Bean
	public static AwsClientBeanRegistrar awsClientRegisterer(ApplicationContext applicationContext,
			ConfigurableBeanFactory beanFactory, ConfigurableEnvironment environment) throws Exception { // NOPMD
		AwsClientPropertiesMap awsClientPropertiesMap = awsClientPropertiesMap();
		AwsS3ClientProperties awsS3ClientProperties = awsS3ClientProperties();
		AwsClientBuilderConfigurer awsClientBuilderConfigurer =
				new AwsClientBuilderConfigurer(beanFactory, awsClientPropertiesMap, awsS3ClientProperties);
		return new AwsClientBeanRegistrar(environment, awsClientBuilderConfigurer);
	}
	
	@Bean
	public static AwsClientPropertiesMap awsClientPropertiesMap() {
		return new AwsClientPropertiesMap();
	}
	
	@Bean
	public static AwsS3ClientProperties awsS3ClientProperties() {
		return new AwsS3ClientProperties();
	}
	
	
	@SuppressWarnings("serial")
	@ConfigurationProperties(value = "aws", ignoreInvalidFields = true)
	static class AwsClientPropertiesMap extends HashMap<String, AwsClientProperties> {
	}
	
	@Data
	static class AwsClientProperties {
		
		private ClientConfiguration client;
		
		private MutableEndpointConfiguration endpoint;
		
		private String region;
		
		private boolean enabled = true;
		
		
		EndpointConfiguration getEndpoint() {
			return endpoint == null ? null : endpoint.toEndpointConfiguration();
		}
	}
	
	/**
	 * @see <a href="https://github.com/spring-projects/spring-boot/issues/8762">spring-boot#8762</a>
	 */
	@Data
	static class MutableEndpointConfiguration {
		
		private String serviceEndpoint;
		
		private String signingRegion;
		
		
		EndpointConfiguration toEndpointConfiguration() {
			if (serviceEndpoint != null) {
				return new EndpointConfiguration(serviceEndpoint, signingRegion);
			}
			return null;
		}
	}
	
	@Data
	@ConfigurationProperties(value = "aws.s3", ignoreInvalidFields = true)
	static class AwsS3ClientProperties {
		
		private Boolean pathStyleAccessEnabled;
		
		private Boolean chunkedEncodingDisabled;
		
		private Boolean accelerateModeEnabled;
		
		private Boolean payloadSigningEnabled;
		
		private Boolean dualstackEnabled;
		
		private Boolean forceGlobalBucketAccessEnabled;
	}
}
