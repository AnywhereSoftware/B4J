
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

import org.jbox2d.dynamics.joints.MotorJoint;
import org.jbox2d.dynamics.joints.MotorJointDef;

import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.objects.B2Body;
import anywheresoftware.b4a.objects.B2Vec2;

/**
 * A prismatic joint. This joint provides one degree of freedom: translation along an axis fixed in
 * bodyA. Relative rotation is prevented. You can use a joint limit to restrict the range of motion
 * and a joint motor to drive the motion or to model joint friction.
 */
@ShortName("B2MotorJoint")
public class B2MotorJoint extends B2Joint{

	@ShortName("B2MotorJointDef")
	public static class B2MotorJointDef extends B2JointDef {
		private MotorJointDef getRev() {
			return (MotorJointDef)def;
		}
		/**
		 * Sets the bodies. BodyB moves relatively to BodyA.
		 */
		public void Initialize(B2Body BodyA, B2Body BodyB) {
			def = new MotorJointDef();
			getRev().initialize(BodyA.body, BodyB.body);
		}
		/**
		 * Gets or sets the angular offset of BodyB relative to BodyA.
		 */
		public float getAngularOffset() {
			return getRev().angularOffset;
		}
		public void setAngularOffset(float f) {
			getRev().angularOffset = f;
		}
		/**
		 * Gets or sets the linear offset of BodyB relative to BodyA.
		 */
		public B2Vec2 getLinearOffset() {
			return new B2Vec2(getRev().linearOffset);
		}
		public void setLinearOffset(B2Vec2 b) {
			getRev().linearOffset.set(b.vec);
		}
		/**
		 * Gets or sets the maximum motor force, usually in N-m.
		 */
		public float getMaxMotorForce() {
			return getRev().maxForce;
		}
		public void setMaxMotorForce(float f) {
			getRev().maxForce = f;
		}
		/**
		 * Gets or sets the maximum motor torque, usually in N-m.
		 */
		public float getMaxMotorTorque() {
			return getRev().maxTorque;
		}
		public void setMaxMotorTorque(float f) {
			getRev().maxTorque = f;
		}
		/**
		 * Gets or sets the position correction factor [0, 1].
		 */
		public void setCorrectionFactor(float f) {
			getRev().correctionFactor = f;
		}
		public float getCorrectionFactor() {
			return getRev().correctionFactor;
		}
		
		
		
	}
	private MotorJoint getRev() {
		return (MotorJoint)joint;
	}
	
	
	/**
	 * Gets or sets the maximum motor force, usually in N-m.
	 */
	public float getMaxMotorForce() {
		return getRev().getMaxForce();
	}
	public void setMaxMotorForce(float f) {
		getRev().setMaxForce(f);
	}
	/**
	 * Gets or sets the maximum motor torque, usually in N-m.
	 */
	public float getMaxMotorTorque() {
		return getRev().getMaxTorque();
	}
	public void setMaxMotorTorque(float f) {
		getRev().setMaxTorque(f); 
	}
	/**
	 * Gets or sets the target angular offset of BodyB relative to BodyA.
	 */
	public float getAngularOffset() {
		return getRev().getAngularOffset();
	}
	public void setAngularOffset(float f) {
		getRev().setAngularOffset(f);
	}
	/**
	 * Gets or sets the target linear offset of BodyB relative to BodyA.
	 */
	public B2Vec2 getLinearOffset() {
		return new B2Vec2(getRev().getLinearOffset());
	}
	public void setLinearOffset(B2Vec2 b) {
		getRev().setLinearOffset(b.vec);
	}
	/**
	 * Gets or sets the position correction factor [0, 1].
	 */
	public void setCorrectionFactor(float f) {
		getRev().setCorrectionFactor(f);
	}
	public float getCorrectionFactor() {
		return getRev().getCorrectionFactor();
	}
	
	
	
	
}
