/*
 * Copyright (c) 2015. Zuercher Hochschule fuer Angewandte Wissenschaften
 *  All Rights Reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may
 *     not use this file except in compliance with the License. You may obtain
 *     a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *     WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *     License for the specific language governing permissions and limitations
 *     under the License.
 */

package ch.icclab.cyclops.application;

import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.schedule.Scheduler;
import ch.icclab.cyclops.services.iaas.openstack.client.UDRCollection;
import ch.icclab.cyclops.services.iaas.openstack.resource.impl.*;
import ch.icclab.cyclops.usecases.tnova.impl.TnovaEventToUDR;
import ch.icclab.cyclops.util.Load;

import java.util.concurrent.TimeUnit;

/**
 * @author Manu
 *         Created by root on 16.11.15.
 */
public class OpenstackUDRApplication extends AbstractApplication {

    @Override
    public void createRoutes() {
        router.attach("/api", TelemetryResource.class); //API used internally to trigger the data collection
        counter.registerEndpoint("/api");

        router.attach("/usage/users/{userId}", UserUsageResource.class); //API used for fetching the usage info for a user
        counter.registerEndpoint("/usage/users");

        router.attach("/usage/resources/{resourceid}", ResourceUsage.class);
        counter.registerEndpoint("/usage/resources");

        router.attach("/meters", MeterResource.class); //API used for saving and returning the information on selected meters for usage metrics collection
        counter.registerEndpoint("/meters");

        router.attach("/environment/meters", OpenstackMeterResource.class); //API used for saving and returning the information on selected meters for usage metrics collection
        counter.registerEndpoint("environment/meters");

        Load load = new Load();

        // but also start scheduler immediately
        startInternalScheduler();
    }

    private void startInternalScheduler() {
        Scheduler scheduler = Scheduler.getInstance();
        scheduler.addRunner(new UDRCollection(), 0, Loader.getSettings().getSchedulerSettings().getSchedulerFrequency(), TimeUnit.SECONDS);
        scheduler.start();
    }

    @Override
    public void initialiseDatabases() {
        //TODO which databases have to be created
        dbClient.createDatabases(settings.getInfluxDBSettings().getInfluxDBDatabaseName());
    }
}
