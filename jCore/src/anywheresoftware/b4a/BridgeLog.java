
/*
 * Copyright 2010 - 2020 Anywhere Software (www.b4x.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
 package anywheresoftware.b4a;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ArrayBlockingQueue;

import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.IBridgeLog;

@Hide
public class BridgeLog implements IBridgeLog{
	private final ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<String>(20, false);
	public BridgeLog(final int port) {
		System.out.println("Starting BridgeLog on port: " + port);
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					DatagramSocket ds = new DatagramSocket();
					InetAddress ia = InetAddress.getLocalHost();
					while (true) {
						String msg = queue.take();
						byte[] b = msg.getBytes("UTF8");
						DatagramPacket dp = new DatagramPacket(b, b.length, ia, port);
						ds.send(dp);
					}
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		});
		t.setDaemon(true);
		t.start();
	}
	@Override
	public void offer(String msg) {
		queue.offer(msg);
	}
}
