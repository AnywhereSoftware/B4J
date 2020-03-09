
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

import org.jbox2d.dynamics.joints.WheelJoint;
import org.jbox2d.dynamics.joints.WheelJointDef;

import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.objects.B2Body;
import anywheresoftware.b4a.objects.B2Vec2;

/**
 * Wheel joint definition. This requires defining a line of motion using an axis and an anchor
 * point. The definition uses local anchor points and a local axis so that the initial configuration
 * can violate the constraint slightly.
 */
@ShortName("B2WheelJoint")
public class B2WheelJoint extends B2Joint{

	@ShortName("B2WheelJointDef")
	public static class B2WheelJointDef extends B2JointDef {
		private WheelJointDef getRev() {
			return (WheelJointDef)def;
		}
		/**
		 * Sets the bodies and the anchor based on the current positions.
		 */
		public void Initialize(B2Body BodyA, B2Body BodyB, B2Vec2 AnchorPoint, B2Vec2 Axis) {
			def = new WheelJointDef();
			getRev().initialize(BodyA.body, BodyB.body, AnchorPoint.vec, Axis.vec);
			getRev().frequencyHz = 4;
			getRev().dampingRatio = 0.7f;
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
	   * Suspension frequency, zero indicates no suspension.
	   */
		public float getFrequencyHz() {
			return getRev().frequencyHz;
		}
		public void setFrequencyHz(float f) {
			getRev().frequencyHz = f;
		}

	  /**
	   * Suspension damping ratio, one indicates critical damping.
	   */
		public float getDampingRatio() {
			return getRev().dampingRatio;
		}
		public void setDampingRatio(float f) {
			getRev().dampingRatio = f;
		}
	
		
	}
	private WheelJoint getRev() {
		return (WheelJoint)joint;
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
	 * Gets the current joint speed in radians per second.
	 */
	public float getJointSpeed() {
		return getRev().getJointSpeed();
	}
	public float getFrequencyHz() {
		return getRev().getSpringFrequencyHz();
	}
	public void setFrequencyHz(float f) {
		getRev().setSpringFrequencyHz(f);
	}
	public float getDampingRatio() {
		return getRev().getSpringDampingRatio();
	}
	public void setDampingRatio(float f) {
		getRev().setSpringDampingRatio(f);
	}
	
	
}
