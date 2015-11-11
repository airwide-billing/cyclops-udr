package ch.icclab.cyclops.schedule;

import ch.icclab.cyclops.util.APICallCounter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

/**
 * Author: Martin Skoviera
 * Created on: 10-Nov-15
 * Description: Implementation of endpoint for internal scheduler
 */
public class Endpoint extends ServerResource {

    final static Logger logger = LogManager.getLogger(Endpoint.class.getName());

    // We are going to be working with internal scheduler here
    private static Scheduler scheduler = Scheduler.getInstance();

    // Command that was received via API call
    private String command;

    // who am I?
    private String endpoint = "/scheduler";

    // used as counter
    private APICallCounter counter = APICallCounter.getInstance();

    /**
     * This method is invoked in order to get command from API URL
     */
    public void doInit() {
        command = (String) getRequestAttributes().get("command");
    }

    /**
     * This method will respond to API call
     * @return return message
     */
    @Get
    public String processCommand() {
        logger.trace("Received API command: " + command);
        counter.increment(endpoint);

        // what to do? determine based on command

        if (command.equalsIgnoreCase("start")) {

            if (scheduler.isRunning()) {
                return "Scheduler has already been started";
            } else {
                scheduler.start();

                return "Scheduler started";
            }

        } else if (command.equalsIgnoreCase("restart")) {

            scheduler.stop();
            scheduler.start();

            return "Scheduler restarted";

        } else if (command.equalsIgnoreCase("stop")) {

            if (scheduler.isRunning()) {
                scheduler.stop();

                return "Scheduler stopped";
            } else {
                return "Scheduler was not running";
            }

        } else if (command.equalsIgnoreCase("status")) {

            return (scheduler.isRunning()) ? "running" : "stopped";

        } else if (command.equalsIgnoreCase("force")) {

            scheduler.force();

            return "Records pulled manually";
        } else {
            return "Unsupported command - use either start, restart, stop, status or force";
        }
    }
}
