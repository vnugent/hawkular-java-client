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
package org.hawkular.client;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.DefaultHttpClient;
import org.hawkular.client.metrics.MetricsRestApi;
import org.jboss.resteasy.client.jaxrs.ProxyBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

public class RestFactory {

    private final ClassLoader classLoader;

    public RestFactory() {
        classLoader = null;
    }

    public RestFactory(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public MetricsRestApi createAPI(URI uri, String userName, String password) {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        httpClient.getCredentialsProvider().setCredentials(new AuthScope(uri.getHost(), uri.getPort()),
                new UsernamePasswordCredentials(userName, password));

        ApacheHttpClient4Engine engine = new ApacheHttpClient4Engine(httpClient);
        ResteasyClient client = new ResteasyClientBuilder().httpEngine(engine).build();
        client.register(JacksonJaxbJsonProvider.class);
        client.register(JacksonObjectMapperProvider.class);
        ProxyBuilder<MetricsRestApi> proxyBuilder = client.target(uri).proxyBuilder(MetricsRestApi.class);
        if (classLoader != null) {
            proxyBuilder = proxyBuilder.classloader(classLoader);
        }
        return proxyBuilder.build();
    }

    public MetricsRestApi createAPI(String url, String userName, String password) throws URISyntaxException {
        URI uri = new URI(url);
        return createAPI(uri, userName, password);
    }
}