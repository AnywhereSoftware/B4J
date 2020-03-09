
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
 
 package anywheresoftware.b4j.objects;

import java.io.IOException;
import java.io.InputStream;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.tasks.OnFailureListener;
import com.google.firebase.tasks.OnSuccessListener;

import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.DependsOn;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;

@Version(1.0f)
@ShortName("FirebaseServer")
@Events(values={"TokenVerified (TokenId As String, Success As Boolean, Token As FirebaseToken)"})
@DependsOn(values={"firebase-server-sdk"})
public class FirebaseServer {
	private String eventName;
	/**
	 * Initializes the object. Should be called once.
	 *EventName - Sets the subs that will handle the events.
	 *InputStream - An input stream from the json file downloaded from Firebase. The input stream will be closed.
	 */
	public void Initialize (String EventName, InputStream InputStream) throws IOException {
		this.eventName = EventName.toLowerCase(BA.cul);
		FirebaseOptions options = new FirebaseOptions.Builder().setServiceAccount(InputStream).build();
		FirebaseApp.initializeApp(options);
		InputStream.close();
	}
	/**
	 * Verifies the given TokenId. The TokenVerified event will be raised in the current module.
	 */
	public void VerifyToken(final BA ba, final String TokenId) {
		FirebaseAuth.getInstance().verifyIdToken(TokenId)
	    .addOnSuccessListener(new OnSuccessListener<FirebaseToken>() {
	        @Override
	        public void onSuccess(FirebaseToken decodedToken) {
	            ba.raiseEventFromDifferentThread(FirebaseServer.this, null, 0, eventName + "_tokenverified", true, new Object[] {TokenId, true, AbsObjectWrapper.ConvertToWrapper(new FirebaseTokenWrapper(), decodedToken)});
	        }
	    
	}).addOnFailureListener(new OnFailureListener() {
		
		@Override
		public void onFailure(Exception arg0) {
			arg0.printStackTrace();
			ba.setLastException(arg0);
			ba.raiseEventFromDifferentThread(FirebaseServer.this, null, 0, eventName + "_tokenverified", true, new Object[] {TokenId, false, AbsObjectWrapper.ConvertToWrapper(new FirebaseTokenWrapper(), null)});
		}
	});


	}
	@ShortName("FirebaseToken")
	public static class FirebaseTokenWrapper extends AbsObjectWrapper<FirebaseToken> {
		public String getEmail() {
			return getObject().getEmail();
		}
		public String getDisplayName() {
			return getObject().getName();
		}
		public String getUid() {
			return getObject().getUid();
		}
	}
}
