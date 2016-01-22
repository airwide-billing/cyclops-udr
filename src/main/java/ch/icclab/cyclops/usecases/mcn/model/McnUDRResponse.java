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
package ch.icclab.cyclops.usecases.mcn.model;

import java.util.ArrayList;

/**
 * @author Manu
 *         Created on 12.01.16.
 */
public class McnUDRResponse {
    private String time;
    private Double usage;
    private String productType;

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getTime() {
        return time;
    }

    public Double getUsage() {
        return usage;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setUsage(Double usage){
        this.usage = usage;
    }

    public void setFields(ArrayList<Object> value, ArrayList<String> columns) {
        this.setTime((String) value.get(columns.indexOf("time")));
        this.setUsage(getDoubleValue(value.get(columns.indexOf("usage"))));
        this.setProductType((String) value.get(columns.indexOf("productType")));
    }

    private Double getDoubleValue(Object number){
        try{
            return (Double) number;
        }catch (Exception e){
            return Double.parseDouble(number.toString());
        }
    }
}
