
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

import org.jbox2d.dynamics.joints.RopeJoint;
import org.jbox2d.dynamics.joints.RopeJointDef;

import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.objects.B2Body;
import anywheresoftware.b4a.objects.B2Vec2;

/**
 *A rope joint enforces a maximum distance between two points on two bodies. It has no other
 * effect.
 */
@ShortName("B2RopeJoint")
public class B2RopeJoint extends B2Joint{
	@ShortName("B2RopeJointDef")
	public static class B2RopeJointDef extends B2JointDef {
		private RopeJointDef getRev() {
			return (RopeJointDef)def;
		}
		/**
		 * Sets the bodies and the local connections points.
		 */
		public void Initialize(B2Body BodyA, B2Body BodyB, B2Vec2 LocalPointA, B2Vec2 LocalPointB, float MaxLength) {
			def = new RopeJointDef();
			RopeJointDef rope = getRev();
			rope.bodyA = BodyA.body;
			rope.bodyB = BodyB.body;
			rope.maxLength = MaxLength;
			rope.localAnchorA.set(LocalPointA.vec);
			rope.localAnchorB.set(LocalPointB.vec);
		}
	}
	private RopeJoint getRev() {
		return (RopeJoint)joint;
	}
	/**
	 * Gets or sets the rope maximum length.
	 */
	public float getMaxLength() {
		return getRev().getMaxLength();
	}
	public void setMaxLength(float f) {
		getRev().setMaxLength(f);
	}
	
}
