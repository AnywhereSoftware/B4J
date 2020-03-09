
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

import org.jbox2d.dynamics.joints.RevoluteJoint;
import org.jbox2d.dynamics.joints.RevoluteJointDef;

import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.objects.B2Body;
import anywheresoftware.b4a.objects.B2Vec2;

/**
 * A revolute joint constrains two bodies to share a common point while they are free to rotate
 * about the point. The relative rotation about the shared point is the joint angle. You can limit
 * the relative rotation with a joint limit that specifies a lower and upper angle. You can use a
 * motor to drive the relative rotation about the shared point. A maximum motor torque is provided
 * so that infinite forces are not generated.

 */
@ShortName("B2RevoluteJoint")
public class B2RevoluteJoint extends B2Joint{

	@ShortName("B2RevoluteJointDef")
	public static class B2RevoluteJointDef extends B2JointDef {
		private RevoluteJointDef getRev() {
			return (RevoluteJointDef)def;
		}
		/**
		 * Sets the bodies and the anchor based on the current positions.
		 */
		public void Initialize(B2Body BodyA, B2Body BodyB, B2Vec2 AnchorPoint) {
			def = new RevoluteJointDef();
			getRev().initialize(BodyA.body, BodyB.body, AnchorPoint.vec);
		}

		/**
		 * Gets or sets whether the motor is enabled.
		 */
		public boolean getMotorEnabled() {
			return getRev().enableMotor;
		}
		public void setMotorEnabled(boolean b) {
			getRev().enableMotor = b;
		}
		/**
		 * Gets or sets the maximum motor torque, usually in N-m.
		 */
		public float getMaxMotorTorque() {
			return getRev().maxMotorTorque;
		}
		public void setMaxMotorTorque(float f) {
			getRev().maxMotorTorque = f;
		}
		/**
		 * Gets or sets the motor speed in radians per second.
		 */
		public float getMotorSpeed() {
			return getRev().motorSpeed;
		}
		public void setMotorSpeed(float f) {
			getRev().motorSpeed = (f);
		}
		/**
		 * Gets the lower limit in radians.
		 */
		public float getLowerLimit() {
			return getRev().lowerAngle;
		}
		/**
		 * Gets the upper limit in radians.
		 */
		public float getUpperLimit() {
			return getRev().upperAngle;
		}
		/**
		 * Sets the lower and upper limits measured in radians.
		 */
		public void SetLimits(float LowerLimit, float UpperLimit) {
			getRev().lowerAngle = LowerLimit;
			getRev().upperAngle = UpperLimit;
		}
		/**
		 * Gets or sets whether the limits are enabled.
		 */
		public boolean getLimitEnabled() {
			return getRev().enableLimit;
		}
		public void setLimitEnabled(boolean b) {
			getRev().enableLimit = b;
		}
		
	}
	private RevoluteJoint getRev() {
		return (RevoluteJoint)joint;
	}
	
	public B2Vec2 getLocalAnchorA() {
		return new B2Vec2(getRev().getLocalAnchorA());
	}
	public B2Vec2 getLocalAnchorB() {
		return new B2Vec2(getRev().getLocalAnchorB());
	}
	/**
	 * Gets or sets whether the motor is enabled.
	 */
	public boolean getMotorEnabled() {
		return getRev().isMotorEnabled();
	}
	public void setMotorEnabled(boolean b) {
		getRev().enableMotor(b);
	}
	/**
	 * Gets or sets the maximum motor torque, usually in N-m.
	 */
	public float getMaxMotorTorque() {
		return getRev().getMaxMotorTorque();
	}
	public void setMaxMotorTorque(float f) {
		getRev().setMaxMotorTorque(f);
	}
	/**
	 * Gets or sets the motor speed in radians per second.
	 */
	public float getMotorSpeed() {
		return getRev().getMotorSpeed();
	}
	public void setMotorSpeed(float f) {
		getRev().setMotorSpeed(f);
	}
	/**
	 * Gets the lower limit in radians.
	 */
	public float getLowerLimit() {
		return getRev().getLowerLimit();
	}
	/**
	 * Gets the upper limit in radians.
	 */
	public float getUpperLimit() {
		return getRev().getUpperLimit();
	}
	/**
	 * Sets the lower and upper limits measured in radians.
	 */
	public void SetLimits(float LowerLimit, float UpperLimit) {
		getRev().setLimits(LowerLimit, UpperLimit);
	}
	/**
	 * Gets or sets whether the limits are enabled.
	 */
	public boolean getLimitEnabled() {
		return getRev().isLimitEnabled();
	}
	public void setLimitEnabled(boolean b) {
		getRev().enableLimit(b);
	}
	/**
	 * Gets the current joint angle in radians.
	 */
	public float getJointAngle() {
		return getRev().getJointAngle();
	}
	/**
	 * Gets the current joint speed in radians per second.
	 */
	public float getJointSpeed() {
		return getRev().getJointSpeed();
	}
	
	
}
