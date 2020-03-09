
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

import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.callbacks.DestructionListener;
import org.jbox2d.callbacks.QueryCallback;
import org.jbox2d.callbacks.RayCastCallback;
import org.jbox2d.collision.Manifold;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.dynamics.joints.Joint;
import org.jbox2d.dynamics.joints.JointType;

import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.RaisesSynchronousEvents;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.objects.B2Body.B2BodyDef;
import anywheresoftware.b4a.objects.B2WorldManifold.B2ContactImpulse;
import anywheresoftware.b4a.objects.B2WorldManifold.B2Manifold;
import anywheresoftware.b4a.objects.collections.List;
import anywheresoftware.b4a.objects.collections.Map;
import anywheresoftware.b4a.objects.joints.B2DistanceJoint;
import anywheresoftware.b4a.objects.joints.B2Joint;
import anywheresoftware.b4a.objects.joints.B2MotorJoint;
import anywheresoftware.b4a.objects.joints.B2PrismaticJoint;
import anywheresoftware.b4a.objects.joints.B2RevoluteJoint;
import anywheresoftware.b4a.objects.joints.B2RopeJoint;
import anywheresoftware.b4a.objects.joints.B2WeldJoint;
import anywheresoftware.b4a.objects.joints.B2WheelJoint;
import anywheresoftware.b4a.objects.joints.B2Joint.B2JointDef;

@Events(values={"BeginContact (Contact As B2Contact)", "EndContact (Contact As B2Contact)", 
		"PreSolve (Contact As B2Contact, OldManifold As B2Manifold)",
		"PostSolve (Contact As B2Contact, Impulse As B2ContactImpulse)",
		"RayCastCallback (Fixture As B2Fixture, Point As B2Vec2, Normal As B2Vec2, Fraction As Float) As Float"
})
@ShortName("B2World")
public class B2World implements DestructionListener{
	@Hide
	public World world;
	private BA ba;
	private String eventName;
	private final List AABQueryList = new List();
	private final Map AABQueryMap = new Map();
	private final List AllBodies = new List(), DynamicBodies = new List();
	private B2QueryCallback queryCallback = new B2QueryCallback();
	private boolean bodiesListDirty;
	private B2RayCastCallback raycastCallback;
	/**
	 * Initializes the world and sets its gravity.
	 */
	public void Initialize(BA ba, String EventName, B2Vec2 Gravity) {
		this.ba = ba;
		this.eventName = EventName.toLowerCase(BA.cul);
		world = new World(Gravity.vec);
		AABQueryList.Initialize();
		AllBodies.Initialize();
		DynamicBodies.Initialize();
		AABQueryMap.Initialize();
		if (ba.subExists(eventName + "_begincontact")) {
			world.setContactListener(new B2ContactListener());
		}
		if (ba.subExists(eventName + "_raycastcallback")) {
			raycastCallback = new B2RayCastCallback();
		}

	}
	/**
	 * Created a B2Vec2 vector.
	 */
	public B2Vec2 CreateVec2(float X, float Y) {
		return new B2Vec2(new Vec2(X, Y));
	}
	public B2Joint CreateJoint(B2JointDef JointDef) {
		Joint j = world.createJoint(JointDef.def);
		B2Joint wrapper;
		switch (JointDef.def.type) {
			case REVOLUTE:
				wrapper = new B2RevoluteJoint();
				break;
			case WELD:
				wrapper = new B2WeldJoint();
				break;
			case ROPE:
				wrapper = new B2RopeJoint();
				break;
			case WHEEL:
				wrapper = new B2WheelJoint();
				break;
			case MOTOR:
				wrapper = new B2MotorJoint();
				break;
			case PRISMATIC:
				wrapper = new B2PrismaticJoint();
				break;
			case DISTANCE:
				wrapper = new B2DistanceJoint();
				break;
			default:
				throw new RuntimeException("unknown type");
		}
		wrapper.joint = j;
		j.setUserData(wrapper);
		return wrapper;
	}
	public void DestroyJoint(B2Joint Joint) {
		if (Joint.joint == null)
			return;
		world.destroyJoint(Joint.joint);
		Joint.joint = null;
	}
	/**
	 * Create a rigid body given a definition.
	 *This function is locked during callbacks.
	 */
	public B2Body CreateBody(B2BodyDef BodyDef) {
		B2Body bbody = new B2Body();
		bbody.init(world.createBody(BodyDef.bd));
		bbody.body.setUserData(bbody);
		AllBodies.Add(bbody);
		if (bbody.body.getType() == BodyType.DYNAMIC)
			DynamicBodies.Add(bbody);
		return bbody;
	}
	private void checkBodiesDirty() {
		if (bodiesListDirty) {
			AllBodies.Clear();
			DynamicBodies.Clear();
			for (Body b = world.getBodyList();b != null;b = b.getNext()) {
				AllBodies.Add(b.getUserData());
				if (b.getType() == BodyType.DYNAMIC)
					DynamicBodies.Add(b.getUserData());
			}
			bodiesListDirty = false;
		}
	}
	/**
	 * Returns a list with all the bodies with dynamic body type.
	 */
	public List getDynamicBodies() {
		checkBodiesDirty();
		return DynamicBodies;
	}
	/**
	 * Returns a list with all the bodies.
	 */
	public List getAllBodies() {
		checkBodiesDirty();
		return AllBodies;
	}
	/**
	 * Gets or sets the world gravity.
	 */
	public B2Vec2 getGravity() {
		return new B2Vec2(world.getGravity());
	}
	public void setGravity(B2Vec2 v) {
		world.setGravity(v.vec);
	}
	/**
	 * Returns True if the world is currently locked for changes. 
	 */
	public boolean getLocked() {
		return world.isLocked();
	}
	/**
	 *This automatically deletes all associated shapes and joints.
	 * This function is locked during callbacks.
	 */
	public void DestroyBody(B2Body Body) {
		bodiesListDirty = true;
		if (Body.body == null)
			return;
		world.destroyBody(Body.body);
		Body.body = null;
	}
	/**
	 * Take a time step. This performs collision detection, integration, and constraint solution.
	 * 
	 * TimeStep - the amount of time to simulate, this should not vary.
	 * VelocityIterations - for the velocity constraint solver.
	 * PositionIterations - for the position constraint solver.
	 */
	@RaisesSynchronousEvents
	public void TimeStep(float TimeStep, int VelocityIterations, int PositionIterations) {
		world.step(TimeStep, VelocityIterations, PositionIterations);
	}

	/**
	 * Queries the world for all fixtures that potentially overlap the given AABB.
	 *Returns a List. Each item in the list is a B2Fixture.
	 */
	public List QueryAABBToListOfFixtures(B2AABB AABB) {
		AABQueryList.Clear();
		queryCallback.toList = true;
		world.queryAABB(queryCallback, AABB.aabb);
		return AABQueryList;
	}
	/**
	 * Queries the world for all fixtures that potentially overlap the given AABB.
	 *Returns a Map with the bodies as keys. The map values are not used.
	 */
	public Map QueryAABBToMapOfBodies(B2AABB AABB) {
		AABQueryMap.Clear();
		queryCallback.toList = false;
		world.queryAABB(queryCallback, AABB.aabb);
		return AABQueryMap;
	}
	/**
	 * Ray-cast the world for all fixtures in the path of the ray, going from FromVec to ToVec. Your callback controls whether you
	 * get the closest point, any point, or n-points. The ray-cast ignores shapes that contain the
	 * starting point.
	 * 
	 * The RayCastCallback event will be raised. The return value from this event determines the behavior:
	 * 0 - Terminate the ray cast.
	 * Fraction - Clip the ray to this point.
	 * 1 - Do not clip the ray and continue.
	 * -1 - Ignore this fixture.
	 */
	@RaisesSynchronousEvents
	public void RayCast(B2Vec2 FromVec, B2Vec2 ToVec) {
		world.raycast(raycastCallback, FromVec.vec, ToVec.vec);
	}
	@Hide
	public class B2QueryCallback implements QueryCallback {
		public boolean toList;
		@Override
		public boolean reportFixture(Fixture fixture) {
			if (toList)
				AABQueryList.Add(fixture.getUserData());
			else {
				AABQueryMap.Put(fixture.m_body.getUserData(), 0);
			}
			return true;
		}

	}
	@Hide
	public class B2RayCastCallback implements RayCastCallback {
		B2Vec2 wpoint = new B2Vec2(), wnormal = new B2Vec2();
		@Override
		public float reportFixture(Fixture fixture, Vec2 point, Vec2 normal,
				float fraction) {
			wpoint.vec = point;
			wnormal.vec = normal;
			Float f = (Float) ba.raiseEvent(B2World.this, eventName + "_raycastcallback", fixture.getUserData(), wpoint, wnormal, fraction);
			if (f == null)
				return 0;
			return f;
		}

	}
	@Hide
	public class B2ContactListener implements ContactListener {
		B2Contact contactListenerContact = new B2Contact();
		B2Manifold manifold = new B2Manifold();
		B2ContactImpulse wimpulse = new B2ContactImpulse();
		@Override
		public void beginContact(Contact contact) {
			contactListenerContact.contact = contact;
			ba.raiseEvent(B2World.this, eventName + "_begincontact", contactListenerContact);
		}

		@Override
		public void endContact(Contact contact) {
			contactListenerContact.contact = contact;
			ba.raiseEvent(B2World.this, eventName + "_endcontact", contactListenerContact);
		}

		@Override
		public void preSolve(Contact contact, Manifold oldManifold) {
			contactListenerContact.contact = contact;
			manifold.manifold = oldManifold;
			ba.raiseEvent(B2World.this, eventName + "_presolve", contactListenerContact, manifold);
		}

		@Override
		public void postSolve(Contact contact, ContactImpulse impulse) {
			contactListenerContact.contact = contact;
			wimpulse.impulse = impulse;
			ba.raiseEvent(B2World.this, eventName + "_postsolve", contactListenerContact, wimpulse);
		}

	}
	/**
	 * Returns the first contact. Note that the list of contacts includes non-touching contacts.
	 *Example:<code>
	 *Dim contact As B2Contact = world.FirstContact
	 *Do While contact <> Null
	 *	If contact.IsTouching Then
	 *		'...
	 *	End If
	 *	contact = contact.NextContact
	 *Loop</code>
	 */
	public B2Contact FirstContact() {
		Contact c = world.getContactList();
		if (c == null)
			return null;
		B2Contact b = new B2Contact();
		b.contact = c;
		return b;
	}
	public B2Joint FirstJoint() {
		Joint j = world.getJointList();
		if (j == null)
			return null;
		return (B2Joint)j.getUserData();
	}
	@Override
	public void sayGoodbye(Joint joint) {
		B2Joint wrapper = (B2Joint)joint.getUserData();
		if (wrapper != null)
			wrapper.joint = null;
		joint.setUserData(null);

	}
	@Override
	public void sayGoodbye(Fixture fixture) {
		B2Fixture wrapper = (B2Fixture)fixture.getUserData();
		if (wrapper != null)
			wrapper.fixture = null;
		fixture.setUserData(null);
	}
}
