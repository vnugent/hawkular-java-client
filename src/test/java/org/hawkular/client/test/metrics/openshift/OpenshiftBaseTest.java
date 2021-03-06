/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hawkular.client.test.metrics.openshift;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.hawkular.client.metrics.model.GaugeDataPoint;
import org.hawkular.client.metrics.model.MetricDefinition;
import org.hawkular.client.test.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups={"openshift"})
public class OpenshiftBaseTest extends BaseTest {
    public enum METRIC_SUFFIX {
        CPU_USAGE("cpu/usage"), MEM_USAGE("mem/usage");
        private String value;
        private METRIC_SUFFIX(String value) {
            this.value = value;
        }
        public String hawkularValue() {
            return value;
        }
    };

    public static final String TENANT_ID="heapster";

    public OpenshiftBaseTest() throws Exception {
        super();
    }

    public String getMetricID(String podNamespace, String containerName, METRIC_SUFFIX metricSuffix) {
        List<MetricDefinition> defs = client().metrics().findMetricDefinitions(TENANT_ID,
                "gauge",
                "container_name:" + containerName + ",pod_namespace:" + podNamespace);
        Assert.assertNotNull(defs, "namespace: " + podNamespace + ", container: " + containerName);
        Assert.assertTrue(defs.size() > 1);
        String podId = defs.get(0).getTags().get("pod_id");
        return containerName + "/" + podId + "/" + metricSuffix.hawkularValue();
    }

    public static Map<Long, Integer> makeHistogram(List<GaugeDataPoint> rawData, Duration timeBucket) {
        long t = timeBucket.toMillis();
        Map<Long, Integer> histogram = new TreeMap<Long, Integer>();
        for(GaugeDataPoint dp : rawData) {
            long key = dp.getTimestamp() / t;
            key = key * t;
            int c = histogram.getOrDefault(key, 0);
            histogram.put(key, c+1);
        }

        return histogram;
    }

}
