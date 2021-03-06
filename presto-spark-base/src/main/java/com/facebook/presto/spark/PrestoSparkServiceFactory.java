/*
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
package com.facebook.presto.spark;

import com.facebook.airlift.log.Logger;
import com.facebook.presto.spark.classloader_interface.IPrestoSparkService;
import com.facebook.presto.spark.classloader_interface.IPrestoSparkServiceFactory;
import com.facebook.presto.spark.classloader_interface.PrestoSparkConfiguration;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Injector;
import com.google.inject.Module;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

import static com.facebook.airlift.configuration.ConfigurationLoader.loadPropertiesFrom;

public class PrestoSparkServiceFactory
        implements IPrestoSparkServiceFactory
{
    private final Logger log = Logger.get(PrestoSparkServiceFactory.class);

    @Override
    public IPrestoSparkService createService(PrestoSparkConfiguration configuration)
    {
        ImmutableMap.Builder<String, String> properties = ImmutableMap.builder();
        try {
            properties.putAll(loadPropertiesFrom(configuration.getConfigFilePath()));
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        properties.putAll(configuration.getExtraProperties());
        properties.put("plugin.dir", configuration.getPluginsDirectoryPath());
        properties.put("plugin.config-dir", configuration.getPluginsConfigDirectoryPath());

        PrestoSparkInjectorFactory prestoSparkInjectorFactory = new PrestoSparkInjectorFactory(properties.build(), getAdditionalModules());

        Injector injector = prestoSparkInjectorFactory.create();
        PrestoSparkService prestoSparkService = injector.getInstance(PrestoSparkService.class);
        log.info("Initialized");
        return prestoSparkService;
    }

    protected List<Module> getAdditionalModules()
    {
        return ImmutableList.of();
    }
}
