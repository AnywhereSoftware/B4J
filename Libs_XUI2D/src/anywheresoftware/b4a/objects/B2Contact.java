
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
 
 package anywheresoftware.b4a.objects;

import org.jbox2d.dynamics.contacts.Contact;

import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.objects.B2WorldManifold.B2Manifold;

/**
 * A contact between two bodies. Note that a contact is created whenever the bodies AABBs overlap.
 *Check IsTouching to find whether the two bodies are actually touching.
 */
@ShortName("B2Contact")
public class B2Contact {
	@Hide
	public Contact contact;
	/**
	 * Returns true if the two bodies are actually touching.
	 */
	public boolean getIsTouching() {
		return contact.isTouching();
	}
	/**
	 * Returns the first fixture. Note that the order of fixtures is not consistent.
	 */
	public B2Fixture getFixtureA() {
		return (B2Fixture) contact.m_fixtureA.getUserData();
	}
	/**
	 * Returns the second fixture.
	 */
	public B2Fixture getFixtureB() {
		return (B2Fixture) contact.m_fixtureB.getUserData();
	}
	 /**
	   * Enable/disable this contact. This can be used inside the pre-solve contact listener. The
	   * contact is only disabled for the current time step (or sub-step in continuous collisions).
	   */
	public void setIsEnabled(boolean b) {
		contact.setEnabled(b);
	}
	
	public boolean getIsEnabled() {
		return contact.isEnabled();
	}
	/**
	 * Returns the next contact or Null if none. Should only be used with contacts accessed with World.FirstContact.
	 *To avoid creating new objects the current object will point to the next contact.
	 */
	public B2Contact NextContact() {
		Contact c = contact.getNext();
		if (c == null)
			return null;
		contact = c;
		return this;
	}
	/**
	 * Gets the contact manifold.
	 */
	public B2Manifold GetManifold() {
		B2Manifold b = new B2Manifold();
		b.manifold = contact.m_manifold;
		return b;
	}
	/**
	   * Gets the world manifold.
	   */
	public void GetWorldManifold(B2WorldManifold OutManifold) {
		OutManifold.PointCount = contact.m_manifold.pointCount;
		contact.getWorldManifold(OutManifold.wm);
		
	}
}
