package com.columbia.vsimrti.applications.lasalle.rsu;

import com.columbia.vsimrti.applications.lasalle.message.InterVehicleMsg;
import com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applicationInterfaces.RoadSideUnitApplication;
import com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.applications.AbstractApplication;
import com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.communication.AdHocModuleConfiguration;
import com.dcaiti.vsimrti.fed.applicationNT.ambassador.simulationUnit.operatingSystem.RoadSideUnitOperatingSystem;
import com.dcaiti.vsimrti.rti.eventScheduling.Event;
import com.dcaiti.vsimrti.rti.network.AdHocChannel;
import com.dcaiti.vsimrti.rti.objects.TIME;
import com.dcaiti.vsimrti.rti.objects.address.DestinationAddressContainer;
import com.dcaiti.vsimrti.rti.objects.address.TopocastDestinationAddress;
import com.dcaiti.vsimrti.rti.objects.v2x.MessageRouting;

/**
* Road Side Unit Application used for VSimRTI Tiergarten Tutorial.
* Sends inter-application messages via broadcast in order to show
* how to differentiate between intra vehicle and inter vehicle application messages.
*/
public class LasalleRSU extends AbstractApplication<RoadSideUnitOperatingSystem> implements RoadSideUnitApplication {
   /**
    * Interval at which messages are sent (every 2 seconds).
    */
   private final static long TIME_INTERVAL = 2 * TIME.SECOND;

   private void sendAdHocBroadcast() {
       final MessageRouting routing =
               getOs().getAdHocModule().createMessageRouting().topoBroadCast(AdHocChannel.CCH);
       final InterVehicleMsg message = new InterVehicleMsg(routing, getOs().getPosition());
       getOs().getAdHocModule().sendV2XMessage(message);
   }

   public void sample() {
       final Event event = new Event(getOperatingSystem().getSimulationTime() + TIME_INTERVAL, this);
       getOperatingSystem().getEventManager().addEvent(event);
       getLog().infoSimTime(this, "Sending out AdHoc broadcast");
       sendAdHocBroadcast();
   }

   @Override
   public void setUp() {
       getLog().infoSimTime(this, "Initialize application");
       getOperatingSystem().getAdHocModule().enable(new AdHocModuleConfiguration()
               .addRadio()
                   .channel(AdHocChannel.CCH)
                   .power(50)
                   .create());
       
       getLog().infoSimTime(this, "Activated WLAN Module");
       sample();
   }

   @Override
   public void tearDown() {
       getLog().infoSimTime(this, "Shutdown application");

   }

   @Override
   public void processEvent(Event event) throws Exception {
       sample();
   }
}

