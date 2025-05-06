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
package de.muenchen.mobidam.scheduler;

import de.muenchen.mobidam.config.InterfaceDTO;
import de.muenchen.mobidam.config.Interfaces;
import de.muenchen.mobidam.eai.common.CommonConstants;
import java.io.IOException;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.ExchangeBuilder;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Getter
public class InterfaceJobExecute implements Job {

    @Value("${de.muenchen.mobidam.integration.job-execute-route:endpointUriNotFound}")
    private String endpointUri;

    private final CamelContext camelContext;

    private final Interfaces interfaces;

    public InterfaceJobExecute(CamelContext camelContext, Interfaces interfaces) {
        this.camelContext = camelContext;
        this.interfaces = interfaces;
    }

    public void execute(JobExecutionContext context) throws JobExecutionException {

        String identifier = (String) context.getJobDetail().getJobDataMap().get(CommonConstants.INTERFACE_TYPE);
        Optional<InterfaceDTO> identifiedSst = Optional.ofNullable(getInterfaces().getInterfaces().get(identifier));

        identifiedSst.ifPresent(sst -> {
            sst.setIdentifier(identifier);
            log.info("Scheduler starts mdl '{}' request at '{}'.", identifier, context.getFireTime().toString());

            var exchange = ExchangeBuilder.anExchange(getCamelContext())
                    .withHeader(CommonConstants.INTERFACE_TYPE, getInterfaces().getInterfaces().get(identifier))
                    .build();
            try (var producer = getCamelContext().createProducerTemplate()) {
                producer.send(endpointUri, exchange);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
