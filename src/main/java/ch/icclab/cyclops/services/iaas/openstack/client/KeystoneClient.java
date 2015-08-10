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


package ch.icclab.cyclops.services.iaas.openstack.client;

import ch.icclab.cyclops.util.Load;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.identity.Token;
import org.openstack4j.openstack.OSFactory;
import org.restlet.resource.ClientResource;

/**
 * Author: Srikanta
 * Created on: 08-Oct-14
 * Description: Requests the keystone service for a token to be used in transaction with Telemetry service of OpenStack
 *
 * Change Log
 * Name        Date     Comments
 */
public class KeystoneClient extends ClientResource {

    /**
     * Generates the token from Keystone service of OpenStack.
     *
     * Pseudo Code
     * 1. Load the auth details of keystone from the configuration object
     * 2. Create a RESTLET client and set the header info
     * 3. Send a POST request to the Keystone client
     * 4. Extract the token info from the response header
     *
     * @return token A string consisting of Keystone token
     */
    public String generateToken(){
        System.out.println("Generating the Token");
        Load load = new Load();
        String keystoneURL = load.configuration.get("KeystoneURL");
        String keystoneUsername = load.configuration.get("KeystoneUsername");
        String keystonePassword = load.configuration.get("KeystonePassword");
        String keystoneTenantName = load.configuration.get("KeystoneTenantName");
        OSClient os = OSFactory.builder()
                .endpoint(keystoneURL)
                .credentials(keystoneUsername, keystonePassword)
                .tenantName(keystoneTenantName)
                .authenticate();
        Token token = os.getToken();
        System.out.println(token.getId());
        return token.getId();
    }
}