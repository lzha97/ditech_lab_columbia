package com.columbia.vsimrti.applications.lasalle.message;

import com.dcaiti.vsimrti.rti.objects.v2x.EncodedV2XMessage;
import com.dcaiti.vsimrti.rti.objects.v2x.MessageRouting;
import com.dcaiti.vsimrti.rti.objects.v2x.V2XMessage;

import javax.annotation.Nonnull;

public class GreenWaveMsg extends V2XMessage {
	 private final String            message;
	    private final EncodedV2XMessage encodedV2XMessage;
	    private final static long        MIN_LEN = 8L;

	    public GreenWaveMsg(MessageRouting routing, String message) {
	        super(routing);
	        this.message = message;
	        encodedV2XMessage = new EncodedV2XMessage(message.length(), MIN_LEN);
	    }

	    public String getMessage() {
	        return message;
	    }

	    @Nonnull
	    @Override
	    public EncodedV2XMessage getEncodedV2XMessage() {
	        return encodedV2XMessage;
	    }

	    @Override
	    public String toString() {
	        final StringBuffer sb = new StringBuffer("GreenWaveMsg{");
	        sb.append("message='").append(message).append('\'');
	        sb.append('}');
	        return sb.toString();
	    }
}


