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
package de.muenchen.mobidam.eai.integration.configuration;

import de.muenchen.mobidam.config.InterfaceDTO;
import de.muenchen.mobidam.config.Interfaces;
import de.muenchen.mobidam.eai.common.CommonConstants;
import de.muenchen.mobidam.scheduler.InterfaceJobExecute;
import jakarta.annotation.PostConstruct;
import java.util.Map;
import org.quartz.*;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnBean(Interfaces.class)
public class InterfaceBeansAutoConfiguration implements BeanFactoryAware {

    private static final String BEAN_NAME_TRIGGER = "interface_trigger_";
    private static final String BEAN_NAME_JOBDETAIL = "interface_jobdetail_";

    @Value("${de.muenchen.mobidam.integration.scheduler-group:schedulerDefaultGroup}")
    private String schedulerGroup;

    public InterfaceBeansAutoConfiguration(Interfaces interfaces) {
        this.interfaces = interfaces;
    }

    private final Interfaces interfaces;

    private BeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @PostConstruct
    public void onPostConstruct() {

        ConfigurableBeanFactory configurableBeanFactory = (ConfigurableBeanFactory) beanFactory;
        var iterator = interfaces.getInterfaces().entrySet().iterator();
        while (iterator.hasNext()) {
            var next = iterator.next();
            if (CronExpression.isValidExpression(next.getValue().getCronExpression())) {
                Trigger trigger = createTrigger(next);
                configurableBeanFactory.registerSingleton(BEAN_NAME_TRIGGER + next.getKey(), trigger);
                JobDetail jobDetail = createJobDetail(next);
                configurableBeanFactory.registerSingleton(BEAN_NAME_JOBDETAIL + next.getKey(), jobDetail);
            }
        }
    }

    private JobDetail createJobDetail(Map.Entry<String, InterfaceDTO> next) {

        return JobBuilder.newJob(InterfaceJobExecute.class)
                .withIdentity(next.getKey(), schedulerGroup)
                .usingJobData(CommonConstants.INTERFACE_TYPE, next.getKey())
                .storeDurably()
                .build();
    }

    private Trigger createTrigger(final Map.Entry<String, InterfaceDTO> next) {

        return TriggerBuilder.newTrigger()
                .forJob(next.getKey(), schedulerGroup)
                .withIdentity(next.getKey(), schedulerGroup)
                .withSchedule(CronScheduleBuilder.cronSchedule(next.getValue().getCronExpression()))
                .build();
    }

}
