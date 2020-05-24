package com.columbia.vsimrti.applications.lasalle.vehicle;

import com.dcaiti.vsimrti.fed.applicationNT.ambassador.navigation.INavigationModule;
import com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applicationInterfaces.CommunicationApplication;
import com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applicationInterfaces.VehicleApplication;
import com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applications.AbstractApplication;
import com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.communication.AdHocModuleConfiguration;
import com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.operatingSystem.VehicleOperatingSystem;
import com.dcaiti.vsimrti.lib.routing.CandidateRoute;
import com.dcaiti.vsimrti.lib.routing.RoutingParameters;
import com.dcaiti.vsimrti.lib.routing.RoutingPosition;
import com.dcaiti.vsimrti.lib.routing.RoutingResponse;
import com.dcaiti.vsimrti.lib.routing.util.ReRouteSpecificConnectionsCostFunction;
import com.dcaiti.vsimrti.rti.enums.SensorType;
import com.dcaiti.vsimrti.rti.eventScheduling.Event;
import com.dcaiti.vsimrti.rti.geometry.GeoCircle;
import com.dcaiti.vsimrti.rti.geometry.GeoPoint;
import com.dcaiti.vsimrti.rti.network.AdHocChannel;
import com.dcaiti.vsimrti.rti.objects.Route;
import com.dcaiti.vsimrti.rti.objects.v2x.AckV2XMessage;
import com.dcaiti.vsimrti.rti.objects.v2x.MessageRouting;
import com.dcaiti.vsimrti.rti.objects.v2x.ReceivedV2XMessage;
import com.dcaiti.vsimrti.rti.objects.v2x.V2XMessage;
import com.dcaiti.vsimrti.rti.objects.v2x.denm.DENM;
import com.dcaiti.vsimrti.rti.objects.v2x.denm.DENMContent;

import java.awt.Color;
import java.util.Objects;

/**
 * Class implementing the application interface and fulfilling a re-routing
 * based on changing weather conditions.
 */
@SuppressWarnings("unused")
public class LasalleWeatherWarningApp extends AbstractApplication<VehicleOperatingSystem> implements VehicleApplication, CommunicationApplication {

    /**
     * Flag that is set if the route has already been changed.
     */
    private boolean routeChanged = false;

    /**
     * This is the speed for the DEN message sent for rerouting.
     */
    private final static float SPEED = 25 / 3.6f;


    /**
     * This method is called by VSimRTI when the vehicle that has been equipped with this application
     * enters the simulation.
     * It is the first method called of this class during a simulation.
     */
    @Override
    public void setUp() {
        getLog().infoSimTime(this, "Initialize application");
        if (useCellNetwork()) {
            getOperatingSystem().getCellModule().enable();
            getLog().infoSimTime(this, "Activated Cell Module");
        } else {
            getOperatingSystem().getAdHocModule().enable(new AdHocModuleConfiguration()
                .addRadio()
                    .channel(AdHocChannel.CCH)
                    .power(50)
                    .create());
            getLog().infoSimTime(this, "Activated AdHoc Module");
        }

        getOperatingSystem().requestVehicleParametersUpdate()
                .changeColor(Color.RED)
                .apply();
    }

    /**
     * This method is called by VSimRTI when the vehicle that has been equipped with this application
     * leaves the simulation.
     * It is the last method called of this class during a simulation.
     */
    @Override
    public void tearDown() {
        getLog().infoSimTime(this, "Shutdown application");
    }

    @Override
    public void receiveV2XMessage(ReceivedV2XMessage receivedV2XMessage) {
        final V2XMessage msg = receivedV2XMessage.getMessage();

        // Only DEN Messages are handled
        if (!(msg instanceof DENM)) {
            getLog().infoSimTime(this, "Ignoring message of type: {}", msg.getSimpleClassName());
            return;
        }

        // Message was received via cell from the WeatherServer
        if (msg.getRouting().getSourceAddressContainer().getSourceName().equals("rsu_1")) {
            getLog().infoSimTime(this, "Received message from cell from WeatherServer");
        }
        final DENM denm = (DENM)msg;
        getLog().infoSimTime(this, "Processing DEN message");

        getLog().debug("Handle Environment Warning Message. Processing...");

        if (routeChanged) {
            getLog().infoSimTime(this, "Route already changed");
        } else {
            reactUponDENMessageChangeRoute(denm);
        }
    }

    @Override
    public void afterUpdateVehicleInfo() {
        if (!isValidStateAndLog()) {
            return;
        }
        detectSensors();
    }


    /**
     * This method is used to request new data from the sensors and, in case of new data, react on it.
     */
    private void detectSensors() {
        // Enumeration of possible environment sensor types that are available in a vehicle
        SensorType[] types = SensorType.values();

        // Initialize sensor type
        SensorType type = null;
        // Initialize sensor strength
        int strength = 0;

        /*
         * The current strength of each environment sensor is examined here.
         * If one is higher than zero, we reason that we are in a hazardous area with the
         * given hazard.
         */
        for (SensorType currentType : types) {

            // The strength of a detected sensor
            strength = getOperatingSystem().getStateOfEnvironmentSensor(currentType);

            if (strength > 0) {
                type = currentType;
                // Method which is called to react on new or changed environment events
                reactOnEnvironmentData(type, strength);
                return;
            }
        }

        getLog().debugSimTime(this, "No Sensor/Event detected");
    }

    /**
     * Method which detects new or changing environment
     * data. It retrieves the strength, type and position of the
     * detected event. Later on all information is filled into a new
     * DEN Message and will be sent.
     *
     * @param type     sensor type
     * @param strength event strength
     */
    private void reactOnEnvironmentData(SensorType type, int strength) {
        // failsafe
        if (getOperatingSystem().getVehicleInfo() == null) {
            getLog().infoSimTime(this, "No vehicleInfo given, skipping.");
            return;
        }
        if (getOperatingSystem().getVehicleInfo().getRoadPosition() == null) {
            getLog().warnSimTime(this, "No road position given, skip this event");
            return;
        }

        // longLat of the vehicle that detected an event.
        GeoPoint vehicleLongLat = getOperatingSystem().getPosition();

        // roadId (connectionId) of the vehicle that detected an event.
        String roadId = getOperatingSystem().getVehicleInfo().getRoadPosition().getConnection().getId();

        getLog().infoSimTime(this, "Sensor {} event detected", type);

        getLog().debugSimTime(this, "Position: {}", vehicleLongLat);
        getLog().debugSimTime(this, "Event strength to: {}", strength);
        getLog().debugSimTime(this, "SensorType to: {}", type);
        getLog().debugSimTime(this, "RoadId on which the event take place: {}", roadId);

        // Region with a radius around the coordinates of the car.
        GeoCircle dest = new GeoCircle(vehicleLongLat, 3000);

        // A MessageRouting object contains a source and a target address for a message to be routed.
        MessageRouting mr;

        /*
         * Depending on our type of network, we get the network module, create a message routing object
         * with a builder and build a geoBroadCast for the circle area defined in dest.
         */
        if (useCellNetwork()) {
            mr = getOperatingSystem().getCellModule().createMessageRouting().geoBroadCast(dest);
        } else {
            mr = getOperatingSystem().getAdHocModule().createMessageRouting().geoBroadCast(dest);
        }

        /*
         * We want to send a DENM (Decentralized Environment Notification Message). A DENM, as a subclass of
         * V2XMessage, requires a MessageRouting (so VSimRTI knows source and destination of the message),
         * and a payload in the form of a DENMContent object. It contains fields such as the current timestamp
         * of the sending node, the geo position of the sending node, warning type and event strength.
         */
        DENM denm = new DENM(mr, new DENMContent(getOperatingSystem().getSimulationTime(), vehicleLongLat, roadId, type, strength, SPEED, 0.0f, vehicleLongLat, null, null));
        getLog().infoSimTime(this, "Sending DENM");

        /*
         * Depending on our type of network, we get the right network module and call its method to send V2X messages,
         * in this case the DENM.
         */
        if (useCellNetwork()) {
            getOperatingSystem().getCellModule().sendV2XMessage(denm);
        } else {
            getOperatingSystem().getAdHocModule().sendV2XMessage(denm);
        }
    }

    private void reactUponDENMessageChangeRoute(DENM denm) {
        /*
         * NOTE: The route change happens only once. Further rerouting is not
         * possible at the moment. The route information which could be used for
         * rerouting is static. It was requested only once by VSimRTI from
         * the traffic simulator. Otherwise the traffic simulator performance
         * would decrease dramatically. This may change in the future.
         * Until this, the routeChanged variable is used.
         */
        final String affectedRoadId = denm.getEventRoadId();
        final Route routeInfo = Objects.requireNonNull(getOs().getNavigationModule().getCurrentRoute());

        // Print some useful DEN message information
        if (getLog().isDebugEnabled()) {
            getLog().debugSimTime(this, "DENM content: Sensor Type: {}", denm.getWarningType().toString());
            getLog().debugSimTime(this, "DENM content: Event position: {}", denm.getEventLocation());
            getLog().debugSimTime(this, "DENM content: Event Strength: {}", denm.getEventStrength());
            getLog().debugSimTime(this, "DENM content: Road Id of the Sender: {}", denm.getEventRoadId());
            getLog().debugSimTime(this, "CurrVehicle: position: {}", getOperatingSystem().getNavigationModule().getRoadPosition());
            getLog().debugSimTime(this, "CurrVehicle: route: {}", routeInfo.getId());
        }

        // Retrieving whether the event we have been notified of is on the vehicle's route
        for (final String edges : routeInfo.getEdgeIdList()) {
            // Retrieve only the connection id and throw away the edge id
            // NOTE: a route info id has the format connectionId_edgeId
            final String v_roadId = edges.substring(0, edges.lastIndexOf("_"));
            //getLog().debugSimTime(this, "road id:" + v_roadId);
            if (v_roadId.equals(affectedRoadId)) {
                getLog().infoSimTime(this, "The Event is on the vehicle's route {} = {}", v_roadId, affectedRoadId);

                circumnavigateAffectedRoad(denm, affectedRoadId);
                routeChanged = true;
                return;
            }
        }

    }

    private void circumnavigateAffectedRoad(DENM denm, final String affectedRoadId) {
        ReRouteSpecificConnectionsCostFunction myCostFunction = new ReRouteSpecificConnectionsCostFunction();
        myCostFunction.setConnectionSpeedMS(affectedRoadId, denm.getCausedSpeed());

        /*
         * The vehicle on which this application has been deployed has a navigation module
         * that we need to retrieve in order to switch routes.
         */
        INavigationModule navigationModule = getOperatingSystem().getNavigationModule();

        /*
         * Routing parameters are used for route calculation. In our case, we want a specific cost function
         * to be used for getting the best route.
         */
        RoutingParameters routingParameters = new RoutingParameters().costFunction(myCostFunction);

        /*
         * To let the navigation module calculate a new route, we need a target position and routing parameters.
         * For the target position, we keep the one our navigation module currently has, i.e. the position our vehicle
         * is currently navigating to. This means that we do not want to change our destination, only calculate
         * a new route to circumvent the obstacle.
         */
        RoutingResponse response = navigationModule.calculateRoutes(new RoutingPosition(navigationModule.getTargetPosition()), routingParameters);

        /*
         * The navigation module has calculated a number of possible routes, of which we want to retrieve the best one
         * according to our specifically assigned cost function. If a best route exists, we call the navigation module
         * to switch to it.
         */
        CandidateRoute newRoute = response.getBestRoute();
        if (newRoute != null) {
            getLog().infoSimTime(this, "Sending Change Route Command at position: {}", denm.getSenderPosition());
            navigationModule.switchRoute(newRoute);
        }
    }

    protected boolean useCellNetwork() {
        return false;
    }

    @Override
    public void beforeSendCAM() {
    }

    @Override
    public void afterSendCAM() {
    }

    @Override
    public void beforeSendV2XMessage() {
    }

    @Override
    public void afterSendV2XMessage() {
    }

    @Override
    public void beforeUpdateConnection() {
    }

    @Override
    public void afterUpdateConnection() {
    }

    @Override
    public void beforeUpdateVehicleInfo() {
    }

    @Override
    public void receiveV2XMessageAcknowledgement(AckV2XMessage ackV2XMessage) {
    }

    @Override
    public void beforeGetAndResetUserTaggedValue() {
    }

    @Override
    public void afterGetAndResetUserTaggedValue() {
    }

    @Override
    public void processEvent(Event event) throws Exception {

    }
}

