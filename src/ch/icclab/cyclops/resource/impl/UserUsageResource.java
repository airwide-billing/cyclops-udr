package ch.icclab.cyclops.resource.impl;

import ch.icclab.cyclops.model.udr.TSDBData;
import ch.icclab.cyclops.model.udr.UserUsageResponse;
import ch.icclab.cyclops.persistence.impl.TSDBResource;
import ch.icclab.cyclops.resource.interfc.UsageResource;
import ch.icclab.cyclops.util.Load;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Author: Srikanta
 * Created on: 21-Jan-15
 * Description:  Services the API GET request coming in for usage data for a given user ID.
 *
 * Change Log
 * Name        Date     Comments
 */
public class UserUsageResource extends ServerResource implements UsageResource {
    private String userId;
    public void doInit() {
        userId = (String) getRequestAttributes().get("userid");
    }
    /**
     *  Get the usage data for the userID mentioned in the URL string
     *  
     *  Pseudo Code
     *  1. Extract the QueryValues from the URL
     *  2. Add the meters name to the meter list
     *  3. Query the DB to get the usage data and add the response to an array list
     *  4. Add the array list with its source name into a HashMap
     *  5. Construct the response and return it in the JSON format
     *    
     * @return userUsageResponse Response consisting of the usage data for the request userID
     */
    @Get
    public Representation getData(){
        
        Representation userUsageResponse;
        TSDBData usageData;
        HashMap usageArr = new HashMap();
        ArrayList<TSDBData> meterDataArrList = new ArrayList<TSDBData>();
        TSDBResource dbResource = new TSDBResource();
        Load load = new Load();

        String fromDate = getQueryValue("from");
        String toDate = getQueryValue("to");
        ArrayList cMeters = Load.openStackCumulativeMeterList;
        ArrayList gMeters = Load.openStackGaugeMeterList;

        //Load the meter list
        load.meterList();
        //Get the data for the OpenStack Cumulative Meters from the DB and create the arraylist consisting of hashmaps of meter name and usage value
        for(int i=0;i<cMeters.size(); i++){
            usageData = dbResource.getUsageData(fromDate, toDate, userId, cMeters.get(i), "openstack", "cumulative");
            if(usageData != null && usageData.getPoints().size() != 0){
                meterDataArrList.add(usageData);
            }
        }
        //Get the data for the OpenStack Gauge Meters from the DB and create the arraylist consisting of hashmaps of meter name and usage value
        for(int i=0;i<gMeters.size(); i++){
            usageData = dbResource.getUsageData(fromDate, toDate, userId, gMeters.get(i), "openstack", "gauge");
            if(usageData != null && usageData.getPoints().size() != 0){
                meterDataArrList.add(usageData);
            }
        }
        usageArr.put("openstack",meterDataArrList);
        //Construct the response in JSON string
        userUsageResponse = constructResponse(usageArr, userId, fromDate, toDate);
        
        return userUsageResponse;
    }

    /**
     * * Construct the JSON response consisting of the meter and the usage values
     * * 
     * * Pseudo Code
     * * 1. Create the HasMap consisting of time range
     * * 2. Create the response POJO
     * * 3. Convert the POJO to JSON
     * * 4. Return the JSON string
     *  
     * @param usageArr An arraylist consisting of metername and corresponding usage
     * @param userId UserID for which the usage details is to be returned.
     * @param fromDate DateTime from usage data needs to be calculated
     * @param toDate DateTime upto which the usage data needs to be calculated
     * @return responseJson The response object in the JSON format
     */
    public Representation constructResponse(HashMap usageArr, String userId, String fromDate, String toDate){

        String jsonStr;
        JsonRepresentation responseJson = null;
        
        UserUsageResponse responseObj = new UserUsageResponse();
        HashMap time = new HashMap();
        ObjectMapper mapper = new ObjectMapper();
        
        time.put("from",fromDate);
        time.put("to",toDate);
        
        //Build the response POJO
        responseObj.setUserid(userId);
        responseObj.setTime(time);
        responseObj.setUsage(usageArr);

        //Convert the POJO to a JSON string
        try {
            jsonStr = mapper.writeValueAsString(responseObj);
            responseJson = new JsonRepresentation(jsonStr);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return responseJson;
    }
    
}