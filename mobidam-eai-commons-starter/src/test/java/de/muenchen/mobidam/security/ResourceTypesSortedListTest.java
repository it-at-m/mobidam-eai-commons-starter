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
package de.muenchen.mobidam.security;

import de.muenchen.mobidam.config.ResourceType;
import de.muenchen.mobidam.config.ResourceTypes;
import de.muenchen.mobidam.eai.common.exception.MobidamException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;

public class ResourceTypesSortedListTest {

    @Test
    @DirtiesContext
    public void testJsonSingle() throws MobidamException {

        ResourceTypes resourceTypes = new ResourceTypes();
        resourceTypes.setResourceTypes(
                Map.of("json", new ResourceType(List.of("application/json", "text/plain")),
                        "plain", new ResourceType(List.of("text/plain"))));
        Assertions.assertEquals(List.of("application/json", "text/plain"), resourceTypes.getResourceTypes(List.of("json")));
    }

    @Test
    public void testSelectManyJsonFirstList() throws MobidamException {

        ResourceTypes resourceTypes = new ResourceTypes();
        resourceTypes.setResourceTypes(
                Map.of("json", new ResourceType(List.of("application/json", "text/plain")),
                        "plain", new ResourceType(List.of("text/plain"))));
        Assertions.assertEquals(List.of("application/json", "text/plain"), resourceTypes.getResourceTypes(List.of("json", "plain")));
    }

    @Test
    public void testSelectManyPlainFirst() throws MobidamException {

        ResourceTypes resourceTypes = new ResourceTypes();
        resourceTypes.setResourceTypes(
                Map.of("json", new ResourceType(List.of("application/json", "text/plain")),
                        "plain", new ResourceType(List.of("text/plain"))));
        Assertions.assertEquals(List.of("text/plain", "application/json"), resourceTypes.getResourceTypes(List.of("plain", "json")));
    }

    @Test
    public void testSelectManyPlainFirstRemoveDuplicates() throws MobidamException {

        ResourceTypes resourceTypes = new ResourceTypes();
        resourceTypes.setResourceTypes(
                Map.of("json", new ResourceType(List.of("application/json", "text/plain")),
                        "plain", new ResourceType(List.of("text/plain"))));
        Assertions.assertEquals(List.of("text/plain", "application/json"), resourceTypes.getResourceTypes(List.of("plain", "json")));
    }

}
