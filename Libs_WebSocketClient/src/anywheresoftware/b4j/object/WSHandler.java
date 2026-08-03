package anywheresoftware.b4j.object;

import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;

import org.eclipse.jetty.websocket.api.Callback;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.Session.Listener.AbstractAutoDemanding;

import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Hide;

@Hide
public class WSHandler extends AbstractAutoDemanding {
	final WebSocketClientWrapper parent;
	public WSHandler(WebSocketClientWrapper parent) throws NoSuchMethodException, SecurityException, IllegalAccessException {
		this.parent = parent;
		
	}
	@Override
	public void onWebSocketOpen(Session sess) {
		parent.ba.raiseEventFromDifferentThread(parent, null, 0, parent.eventName + "_connected", false, null);
	}
	@Override
	public void onWebSocketClose(int statusCode, String reason, Callback callback)
	{
		parent.ba.raiseEventFromDifferentThread(parent, null, 0, parent.eventName + "_closed", false, 
				new Object[] {BA.ReturnString(reason)});
		callback.succeed();
	}
	@Override
	public void onWebSocketError(Throwable cause) {
		if (cause instanceof ClosedChannelException)
			return;
		if (BA.debugMode)
			cause.printStackTrace();
		try {
			onWebSocketClose(-1, cause.getMessage(), Callback.NOOP);
			parent.Close();
		} catch (Exception e) {
			if (BA.debugMode)
				e.printStackTrace();
		}

	}
	@Override
	public void onWebSocketText(String message) {
		parent.ba.raiseEventFromDifferentThread(parent, null, 0, parent.eventName + "_textmessage",
				false, new Object[] {message});
	}
	@Override
	public void onWebSocketBinary(ByteBuffer bb, Callback callback)
	{
		byte[] payload = new byte[bb.remaining()];
		bb.get(payload);
		parent.ba.raiseEventFromDifferentThread(parent, null, 0, parent.eventName + "_binarymessage",
				false, new Object[] {payload});
		callback.succeed();
	}

}
