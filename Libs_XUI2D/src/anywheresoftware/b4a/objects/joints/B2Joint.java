
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
 
 package anywheresoftware.b4a.objects.joints;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.joints.Joint;
import org.jbox2d.dynamics.joints.JointDef;
import org.jbox2d.dynamics.joints.JointType;

import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.objects.B2Body;
import anywheresoftware.b4a.objects.B2Vec2;

@ShortName("B2Joint")
public class B2Joint {
	@Hide
	public Joint joint;
	public Object Tag;
	public static final Object JOINT_UNKNOWN = JointType.UNKNOWN;
	public static final Object JOINT_REVOLUTE = JointType.REVOLUTE;
	public static final Object JOINT_PRISMATIC = JointType.PRISMATIC;
	public static final Object JOINT_DISTANCE = JointType.DISTANCE;
	public static final Object JOINT_PULLEY = JointType.PULLEY;
	public static final Object JOINT_MOUSE = JointType.MOUSE;
	public static final Object JOINT_GEAR = JointType.GEAR;
	public static final Object JOINT_WHEEL = JointType.WHEEL;
	public static final Object JOINT_WELD = JointType.WELD;
	public static final Object JOINT_FRICTION = JointType.FRICTION;
	public static final Object JOINT_ROPE = JointType.ROPE;
	public static final Object JOINT_MOTOR = JointType.MOTOR;
	public boolean getIsInitialized() {
		return joint != null;
	}
	public Object getJointType() {
		return joint.getType();
	}
	/**
	 * Gets the anchor point of BodyA in world coordinates.
	 */
	public B2Vec2 getAnchorA() {
		B2Vec2 v = new B2Vec2();
		v.vec = new Vec2();
		joint.getAnchorA(v.vec);
		return v;
	}
	/**
	 * Gets the anchor point of BodyB in world coordinates.
	 */
	public B2Vec2 getAnchorB() {
		B2Vec2 v = new B2Vec2();
		v.vec = new Vec2();
		joint.getAnchorB(v.vec);
		return v;
	}
	/**
	 * Gets the first attached body.
	 */
	public B2Body getBodyA() {
		return (B2Body) joint.getBodyA().getUserData();
	}
	/**
	 * Gets the second attached body.
	 */
	public B2Body getBodyB() {
		return (B2Body) joint.getBodyB().getUserData();
	}
	/**
	 * Returns the next joint. Returns Null if this is the last joint.
	 */
	public B2Joint NextJoint() {
		if (joint.m_next == null)
			return null;
		return (B2Joint)joint.m_next.m_userData;
	}
	
	@ShortName("B2JointDef")
	public static class B2JointDef {
		@Hide
		public JointDef def;
		/**
		 * Gets the first attached body.
		 */
		public B2Body getBodyA() {
			return (B2Body) def.bodyA.getUserData();
		}
		/**
		 * Gets the second attached body.
		 */
		public B2Body getBodyB() {
			return (B2Body) def.bodyB.getUserData();
		}
		/**
		 * Gets or sets whether the attached bodies can collide.
		 */
		public boolean getCollideConnected() {
			return def.collideConnected;
		}
		public void setCollideConnected(boolean b) {
			def.collideConnected = b;
		}
	}
	
}
