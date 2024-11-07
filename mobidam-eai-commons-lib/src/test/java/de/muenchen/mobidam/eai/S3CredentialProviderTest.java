/*
 * The MIT License
 * Copyright © 2024 Landeshauptstadt München | it@M
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package de.muenchen.mobidam.eai;

import de.muenchen.mobidam.eai.common.CommonConstants;
import de.muenchen.mobidam.eai.common.config.EnvironmentReader;
import de.muenchen.mobidam.eai.common.config.S3BucketCredentialConfig;
import de.muenchen.mobidam.eai.common.exception.MobidamException;
import de.muenchen.mobidam.eai.common.s3.S3CredentialProvider;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

public class S3CredentialProviderTest {

    private S3CredentialProvider s3CredentialProvider;
    private final CamelContext camelContext = new DefaultCamelContext();

    @Test
    public void test_processWithValidConfiguration() throws Exception {

        try (MockedStatic<EnvironmentReader> environmentReader = Mockito.mockStatic(EnvironmentReader.class)) {

            // Given
            String bucketName = "x-itmkm82k";
            String envVar = "FOO";
            String value = "BAR";

            configureEnvironment(bucketName, envVar, value);

            Exchange exchange = new DefaultExchange(camelContext);
            exchange.getMessage().setHeader(CommonConstants.HEADER_BUCKET_NAME, bucketName);

            environmentReader.when(() -> EnvironmentReader.getEnvironmentVariable(envVar)).thenReturn(value);

            // When
            s3CredentialProvider.process(exchange);

            // Then
            Assertions.assertEquals(value, exchange.getMessage().getHeader(CommonConstants.HEADER_ACCESS_KEY));
            Assertions.assertEquals(value, exchange.getMessage().getHeader(CommonConstants.HEADER_SECRET_KEY));
        }
    }

    @Test
    public void test_processWithMissingEnvVars() {
        // Given
        String bucketName = "x-itmkm82k";
        String envVar = "FOO";
        String value = "BAR";

        configureEnvironment(bucketName, envVar, value);
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getMessage().setHeader(CommonConstants.HEADER_BUCKET_NAME, bucketName);

        // Then
        Assertions.assertThrows(MobidamException.class, () -> {
            // When
            s3CredentialProvider.process(exchange);
        });
    }

    @Test
    public void test_processWithMissingBucketConfiguration() {
        // Given
        String bucketName = "x-itmkm82k";
        String envVar = "FOO";
        String value = "BAR";

        configureEnvironment(bucketName, envVar, value);
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getMessage().setHeader(CommonConstants.HEADER_BUCKET_NAME, "invalid_bucket");

        // Then
        Assertions.assertThrows(MobidamException.class, () -> {
            // When
            s3CredentialProvider.process(exchange);
        });
    }

    @Test
    public void test_processWithMissingBucketName() {
        // Given
        String bucketName = "x-itmkm82k";
        String envVar = "FOO";
        String value = "BAR";

        configureEnvironment(bucketName, envVar, value);
        Exchange exchange = new DefaultExchange(camelContext);

        // Then
        Assertions.assertThrows(MobidamException.class, () -> {
            // When
            s3CredentialProvider.process(exchange);
        });
    }

    private void configureEnvironment(String bucketName, String envVar, String value) {
        S3BucketCredentialConfig properties = Mockito.mock(S3BucketCredentialConfig.class);
        s3CredentialProvider = new S3CredentialProvider(properties);

        S3BucketCredentialConfig.BucketCredentialConfig envVars = new S3BucketCredentialConfig.BucketCredentialConfig();
        envVars.setAccessKeyEnvVar(envVar);
        envVars.setSecretKeyEnvVar(envVar);
        Map<String, S3BucketCredentialConfig.BucketCredentialConfig> map = new HashMap<>();
        map.put(bucketName, envVars);
        Mockito.when(properties.getBucketCredentialConfigs()).thenReturn(map);
    }
}
