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
package de.muenchen.mobidam.eai.common.s3;

import de.muenchen.mobidam.eai.common.CommonConstants;
import de.muenchen.mobidam.eai.common.config.EnvironmentReader;
import de.muenchen.mobidam.eai.common.config.S3BucketCredentialConfig;
import de.muenchen.mobidam.eai.common.exception.ErrorResponseBuilder;
import de.muenchen.mobidam.eai.common.exception.MobidamException;
import java.util.Map;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.tooling.model.Strings;
import org.apache.http.HttpStatus;

/**
 * This class provides the credentials for S3 buckets.
 * It takes the configured environment variables from the properties, reads their content
 * and provides them as message headers.
 */
@RequiredArgsConstructor
public class S3CredentialProvider implements Processor {

    private static final String TENANT_CONFIG = "tenant-default";

    @NonNull
    private final S3BucketCredentialConfig properties;

    @Override
    public void process(Exchange exchange) throws Exception {
        String bucketName = verifyBucket(exchange);
        S3BucketCredentialConfig.BucketCredentialConfig credentials = verifyCredentials(bucketName, exchange);
        String accessKey = EnvironmentReader.getEnvironmentVariable(credentials.getAccessKeyEnvVar());
        String secretKey = EnvironmentReader.getEnvironmentVariable(credentials.getSecretKeyEnvVar());
        if (Strings.isNullOrEmpty(accessKey) || Strings.isNullOrEmpty(secretKey)) {
            exchange.getMessage()
                    .setBody(ErrorResponseBuilder.build(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Bucket not configured: " + bucketName));
            throw new MobidamException("Bucket not configured: " + bucketName);
        }
        exchange.getMessage().setHeader(CommonConstants.HEADER_ACCESS_KEY, accessKey);
        exchange.getMessage().setHeader(CommonConstants.HEADER_SECRET_KEY, secretKey);
    }

    private String verifyBucket(Exchange exchange) throws MobidamException {
        String bucketName = exchange.getMessage().getHeader(CommonConstants.HEADER_BUCKET_NAME, String.class);
        if (Strings.isNullOrEmpty(bucketName)) {
            exchange.getMessage().setBody(ErrorResponseBuilder.build(HttpStatus.SC_BAD_REQUEST, "Bucket name is missing"));
            throw new MobidamException("Bucket name is missing");
        }
        return bucketName;
    }

    private S3BucketCredentialConfig.BucketCredentialConfig verifyCredentials(String bucketName, Exchange exchange) throws MobidamException {
        Map<String, S3BucketCredentialConfig.BucketCredentialConfig> map = properties.getBucketCredentialConfigs();
        S3BucketCredentialConfig.BucketCredentialConfig envVars = map.get(bucketName);
        if (envVars == null) {
            envVars = map.get(TENANT_CONFIG);
            if (envVars == null) {
                exchange.getMessage()
                        .setBody(ErrorResponseBuilder.build(HttpStatus.SC_INTERNAL_SERVER_ERROR,
                                "Configuration for bucket and tenant not found: " + bucketName));
                throw new MobidamException("Configuration for bucket and tenant not found: " + bucketName);
            }
        }
        return envVars;
    }

}
