package com.columbia.vsimrti.applications.lasalle.message;

import com.dcaiti.vsimrti.rti.geometry.GeoPoint;
import com.dcaiti.vsimrti.rti.objects.v2x.EncodedV2XMessage;
import com.dcaiti.vsimrti.rti.objects.v2x.MessageRouting;
import com.dcaiti.vsimrti.rti.objects.v2x.V2XMessage;

import javax.annotation.Nonnull;

public class InterVehicleMsg extends V2XMessage{

	/**
	    * Example payload. The sender puts its geo location
	    * inside the message and sends it to every possible receiver.
	    */
	   private final GeoPoint senderPosition;
	   private final EncodedV2XMessage encodedV2XMessage;
	   private final static long minLen = 128L;

	   public InterVehicleMsg(MessageRouting routing, GeoPoint senderPosition) {
	       super(routing);
	       encodedV2XMessage = new EncodedV2XMessage(16L, minLen);
	       this.senderPosition = senderPosition;
	   }

	   public GeoPoint getSenderPosition() {
	       return senderPosition;
	   }

	   @Nonnull
	   @Override
	   public EncodedV2XMessage getEncodedV2XMessage() {
	       return encodedV2XMessage;
	   }

	   @Override
	   public String toString() {
	       final StringBuffer sb = new StringBuffer("InterVehicleMsg{");
	       sb.append("senderPosition=").append(senderPosition);
	       sb.append('}');
	       return sb.toString();
	   }
	   
}








