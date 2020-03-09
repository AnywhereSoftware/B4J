
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

import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.contacts.ContactEdge;

import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.objects.B2Fixture.B2FixtureDef;
import anywheresoftware.b4a.objects.collections.List;

@ShortName("B2Body")
public class B2Body {
	@Hide
	public Body body;
	public static final Object TYPE_STATIC = BodyType.STATIC;
	public static final Object TYPE_KINEMATIC = BodyType.KINEMATIC;
	public static final Object TYPE_DYNAMIC = BodyType.DYNAMIC;
	private Object tag;
	private B2Transform transform;
	private List ContactsList;
	@Hide
	public void init(Body body) {
		this.body = body;
		transform = new B2Transform();
		transform.t = body.m_xf;
		
	}
	public boolean getIsInitialized() {
		return body != null;
	}

	/**
	 * Get the world body origin position. Do not modify.
	 */
	public B2Vec2 getPosition() {
		return new B2Vec2(body.getPosition());
	}
	/**
	 * Get the world position of the center of mass. Do not modify.
	 */
	public B2Vec2 getWorldCenter() {
		return new B2Vec2(body.getWorldCenter());
	}
	/**
	 * Get the angle in radians.
	 */
	public float getAngle() {
		return body.getAngle();
	}
	/**
	 * Get the local position of the center of mass. Do not modify.
	 */
	public final B2Vec2 getLocalCenter() {
		return new B2Vec2(body.getLocalCenter());
	}
	/**
	 * Get or sets the linear velocity of the center of mass. Do not modify the returned vector.
	 */
	public B2Vec2 getLinearVelocity() {
		return new B2Vec2(body.getLinearVelocity());
	}
	public void setLinearVelocity(B2Vec2 vec) {
		body.setLinearVelocity(vec.vec);
	}
	/**
	 * Gets or sets the body type.
	 */
	public Object getBodyType() {
		return body.getType();
	}
	public void setBodyType(Object o) {
		body.setType((BodyType)o);
	}
	/**
	 * Gets or sets the angular velocity. Measured in radians/second.
	 */
	public float getAngularVelocity() {
		return body.getAngularVelocity();
	}
	public void setAngularVelocity(float f) {
		body.setAngularVelocity(f);
	}
	/**
	 * Gets or sets the gravity scale.
	 */
	public float getGravityScale() {
		return body.getGravityScale();
	}
	public void setGravityScale(float f) {
		body.setGravityScale(f);
	}
	/**
	 * Apply a force at a world point. If the force is not applied at the center of mass, it will
	 * generate a torque and affect the angular velocity. This wakes up the body.
	 * 
	 * Force - the world force vector, usually in Newtons (N).
	 * Point - the world position of the point of application.
	 */
	public void ApplyForce(B2Vec2 Force, B2Vec2 Point) {
		body.applyForce(Force.vec, Point.vec);
	}
	/**
	 * Apply a force to the center of mass. This wakes up the body.
	 * 
	 * Force - the world force vector, usually in Newtons (N).
	 */
	public void ApplyForceToCenter(B2Vec2 Force) {
		body.applyForceToCenter(Force.vec);
	}
	/**
	 * Apply a torque. This affects the angular velocity without affecting the linear velocity of the
	 * center of mass. This wakes up the body.
	 * 
	 * Torque - about the z-axis (out of the screen), usually in N-m.
	 */
	public void ApplyTorque(float Torque) {
		body.applyTorque(Torque);
	}
	/**
	 * Apply an impulse at a point. This immediately modifies the velocity. It also modifies the
	 * angular velocity if the point of application is not at the center of mass. This wakes up the
	 * body.
	 * 
	 * Impulse - the world impulse vector, usually in N-seconds or kg-m/s.
	 * Point - the world position of the point of application.
	 * Wake - also wake up the body
	 */
	public void ApplyLinearImpulse (B2Vec2 Impulse, B2Vec2 Point) {
		body.applyLinearImpulse(Impulse.vec, Point.vec, true);
	}
	/**
	 * Apply an angular impulse.
	 * 
	 *Impulse - the angular impulse in units of kg*m*m/s
	 */
	public void ApplyAngularImpulse (float Impulse) {
		body.applyAngularImpulse(Impulse);
	}
	/**
	 * Get the total mass of the body. Usually in kilograms (kg).
	 */
	public float getMass() {
		return body.m_mass;
	}
	/**
	 * Get the central rotational inertia of the body. Usually in kg-m^2
	 */
	public float getInertia() {
		return body.getInertia();
	}
	/**
	 * Get the world coordinates of a point given the local coordinates.
	 * 
	 * LocalPoint - a point on the body measured relative the the body's origin.
	 */
	public B2Vec2 GetWorldPoint(B2Vec2 LocalPoint) {
		return new B2Vec2(body.getWorldPoint(LocalPoint.vec));
	}
	/**
	 * Get the world coordinates of a vector given the local coordinates.
	 * 
	 * LocalVector - a vector fixed in the body.
	 */
	public final B2Vec2 GetWorldVector(B2Vec2 LocalVector) {
		return new B2Vec2(body.getWorldVector(LocalVector.vec));
	}
	/**
	 * Gets a local point relative to the body's origin given a world point.
	 */
	public final B2Vec2 GetLocalPoint(B2Vec2 WorldPoint) {
		return new B2Vec2(body.getLocalPoint(WorldPoint.vec));
	}
	/**
	 * Gets a local vector given a world vector.
	 */
	public final B2Vec2 GetLocalVector(B2Vec2 WorldVector) {
		return new B2Vec2(body.getLocalVector(WorldVector.vec));
	}
	/** Get or set the linear damping of the body. */
	public float getLinearDamping() {
		return body.getLinearDamping();
	}
	public void setLinearDamping(float f) {
		body.setLinearDamping(f);
	}
	/** Get or set the angular damping of the body. */
	public float getAngularDamping() {
		return body.getAngularDamping();
	}
	public void setAngularDamping(float f) {
		body.setAngularDamping(f);
	}
	/**
	 * Set the position of the body's origin and rotation. This breaks any contacts and wakes the
	 * other bodies. Manipulating a body's transform may cause non-physical behavior. Note: contacts
	 * are updated on the next call to World.TimeStep().
	 * 
	 *Position - the world position of the body's local origin.
	 *Angle - the world rotation in radians.
	 */
	public void SetTransform(B2Vec2 Position, float Angle) {
		body.setTransform(Position.vec, Angle);
	}
	/**
	 * Gets the body transformation.
	 */
	public B2Transform getTransform() {
		return transform;
	}
	/** Is this body treated like a bullet for continuous collision detection? */
	public boolean getIsBullet() {
		return body.isBullet();
	}
	public void setBullet(boolean b) {
		body.setBullet(b);
	}
	/**
	 * Get or set whether sleeping is allowed.
	 */
	public boolean getSleepingAllowed() {
		return body.isSleepingAllowed();
	}
	public void setSleepingAllowed(boolean b) {
		body.setSleepingAllowed(b);
	}
	/**
	 * Get or set the sleeping state of this body. A sleeping body has very low CPU cost.
	 */
	public boolean getAwake() {
		return body.isAwake();
	}
	public void setAwake(boolean b) {
		body.setAwake(b);
	}
	/**
	 * Get or set whether this body have fixed rotation. Setting causes the mass to be reset.
	 */
	public boolean getFixedRotation() {
		return body.isFixedRotation();
	}
	public void setFixedRotation(boolean b) {
		body.setFixedRotation(b);
	}

	/**
	 * Creates a fixture and attach it to this body. Use this function if you need to set some fixture
	 * parameters, like friction. Otherwise you can create the fixture directly from a shape with CreateFixture2. If the
	 * density is non-zero, this function automatically updates the mass of the body. Contacts are not
	 * created until the next time step.
	 * This function is locked during callbacks.
	 */
	public B2Fixture CreateFixture(B2FixtureDef FixtureDef) {
		B2Fixture f = new B2Fixture();
		f.fixture = body.createFixture(FixtureDef.fd);
		if (f.fixture == null)
			throw new RuntimeException("Failed to create fixture.");
		f.fixture.m_userData = f;
		return f;
	}
	/**
	 * Creates a fixture from a shape and attach it to this body. This is a convenience function. Use
	 * FixtureDef if you need to set parameters like friction, restitution, user data, or filtering.
	 * If the density is non-zero, this function automatically updates the mass of the body.
	 * 
	 * Shape - The fixture shape. Do not reuse this shape!
	 * Density - the shape density (set to zero for static bodies).
	 * This function is locked during callbacks.
	 */
	public B2Fixture CreateFixture2(Shape Shape, float Density) {
		B2Fixture f = new B2Fixture();
		f.fixture = body.createFixture(Shape, Density);
		if (f.fixture == null)
			throw new RuntimeException("Failed to create fixture.");
		f.fixture.m_userData = f;
		return f;
	}
	/**
	 * Destroy a fixture. This removes the fixture from the broad-phase and destroys all contacts
	 * associated with this fixture. This will automatically adjust the mass of the body if the body
	 * is dynamic and the fixture has positive density. All fixtures attached to a body are implicitly
	 * destroyed when the body is destroyed.
	 * 
	 *This function is locked during callbacks.
	 */
	public void DestroyFixture(B2Fixture Fixture) {
		if (Fixture.getIsInitialized()) {
			body.destroyFixture(Fixture.fixture);
		}
		Fixture.fixture = null;
	}
	/**
	 * Returns the first fixture.
	 */
	public B2Fixture getFirstFixture() {
		Fixture f = body.getFixtureList();
		if (f == null)
			return null;
		return (B2Fixture) f.getUserData();
	}
	/**
	 * Returns true if the contact list is not empty. Note that the list includes non-touching contacts.
	 */
	public boolean getIsColliding() {
		return body.getContactList() != null;
	}
	/**
	 * Returns a list with the body contacts.
	 *TouchingOnly - If False then the list will include non-touching collisions (based on the bodies AABBs).
	 */
	public List GetContactList(boolean TouchingOnly) {
		if (ContactsList == null) {
			ContactsList = new List();
			ContactsList.Initialize();
		} else {
			ContactsList.Clear();
		}
		for (ContactEdge ce = body.getContactList();ce != null;ce = ce.next) {
			if (TouchingOnly == false || ce.contact.isTouching()) {
				B2Contact b = new B2Contact();
				b.contact = ce.contact;
				ContactsList.Add(b);
			}
		}
		return ContactsList;
		
	}
	public Object getTag() {
		return tag;
	}
	public void setTag(Object o) {
		tag = o;
	}


	@ShortName("B2BodyDef")
	public static class B2BodyDef {
		@Hide
		public BodyDef bd = new BodyDef();
		public final Object TYPE_STATIC = BodyType.STATIC;
		public final Object TYPE_KINEMATIC = BodyType.KINEMATIC;
		public final Object TYPE_DYNAMIC = BodyType.DYNAMIC;
		/**
		 * Get or set the body type.
		 */
		public Object getBodyType() {
			return bd.type;
		}
		public void setBodyType(Object o) {
			bd.type = (BodyType) o;
		}
		 /**
		   * The world angle of the body in radians.
		   */
		public float getAngle() {
			return bd.angle;
		}
		public void setAngle(float a) {
			bd.angle = a;
		}
		 /**
		   * The linear velocity of the body in world co-ordinates.
		   */
		public B2Vec2 getLinearVelocity() {
			return new B2Vec2(bd.linearVelocity);
		}
		public void setLinearVelocity(B2Vec2 b) {
			bd.linearVelocity = b.vec;
		}
		 /**
		   * The world position of the body. Avoid creating bodies at the origin since this can lead to many
		   * overlapping shapes.
		   */
		public B2Vec2 getPosition() {
			return new B2Vec2(bd.position);
		}
		public void setPosition(B2Vec2 b) {
			bd.position = b.vec;
		}
		/**
		   * The angular velocity of the body.
		   */
		public float getAngularVelocity() {
			return bd.angularVelocity;
		}
		public void setAngularVelocity(float f) {
			bd.angularVelocity = f;
		}
		/**
		   * Linear damping is use to reduce the linear velocity. The damping parameter can be larger than
		   * 1.0f but the damping effect becomes sensitive to the time step when the damping parameter is
		   * large.
		   */
		public float getLinearDamping() {
			return bd.getLinearDamping();
		}
		public void setLinearDamping(float f) {
			bd.setLinearDamping(f);
		}
		 /**
		   * Angular damping is use to reduce the angular velocity. The damping parameter can be larger than
		   * 1.0f but the damping effect becomes sensitive to the time step when the damping parameter is
		   * large.
		   */
		public float getAngularDamping() {
			return bd.angularDamping;
		}
		public void setAngularDamping(float f) {
			bd.angularDamping = f;
		}
		public boolean getAllowSleep() {
			return bd.allowSleep;
		}
		/**
		   * Set this flag to false if this body should never fall asleep. Note that this increases CPU
		   * usage.
		   */
		public void setAllowSleep(boolean b) {
			bd.allowSleep = b;
		}
		 /**
		   * Is this body initially sleeping?
		   */
		public boolean getAwake() {
			return bd.awake;
		}
		public void setAwake(boolean b) {
			bd.awake = b;
		}
		/**
		   * Should this body be prevented from rotating? Useful for characters.
		   */
		public boolean getFixedRotation() {
			return bd.fixedRotation;
		}
		public void setFixedRotation(boolean b) {
			bd.fixedRotation = b;
		}
		/**
		   * Is this a fast moving body that should be prevented from tunneling through other moving bodies?
		   * Note that all bodies are prevented from tunneling through kinematic and static bodies. This
		   * setting is only considered on dynamic bodies.
		   * 
		   * You should use this flag sparingly since it increases processing time.
		   */
		public boolean getBullet() {
			return bd.bullet;
		}
		public void setBullet(boolean b) {
			bd.bullet = b;
		}
		/**
		   * Does this body start out active?
		   */
		public boolean getActive() {
			return bd.active;
		}
		public void setActive(boolean b) {
			bd.active = b;
		}

		  /**
		   * Experimental: scales the inertia tensor.
		   */
		public float getGravityScale() {
			return bd.gravityScale;
		}
		public void setGravityScale(float f) {
			bd.gravityScale = f;
		}


	}
}
