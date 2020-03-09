
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

import org.jbox2d.dynamics.joints.PrismaticJoint;
import org.jbox2d.dynamics.joints.PrismaticJointDef;
import org.jbox2d.dynamics.joints.RevoluteJoint;
import org.jbox2d.dynamics.joints.RevoluteJointDef;

import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.objects.B2Body;
import anywheresoftware.b4a.objects.B2Vec2;

/**
 * A prismatic joint. This joint provides one degree of freedom: translation along an axis fixed in
 * bodyA. Relative rotation is prevented. You can use a joint limit to restrict the range of motion
 * and a joint motor to drive the motion or to model joint friction.
 */
@ShortName("B2PrismaticJoint")
public class B2PrismaticJoint extends B2Joint{

	@ShortName("B2PrismaticJointDef")
	public static class B2PrismaticJointDef extends B2JointDef {
		private PrismaticJointDef getRev() {
			return (PrismaticJointDef)def;
		}
		/**
		 * Sets the bodies and the anchor based on the current positions.
		 */
		public void Initialize(B2Body BodyA, B2Body BodyB, B2Vec2 AnchorPoint, B2Vec2 Axis) {
			def = new PrismaticJointDef();
			getRev().initialize(BodyA.body, BodyB.body, AnchorPoint.vec, Axis.vec);
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
		 * Gets or sets the maximum motor force, usually in N-m.
		 */
		public float getMaxMotorForce() {
			return getRev().maxMotorForce;
		}
		public void setMaxMotorForce(float f) {
			getRev().maxMotorForce = f;
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
		 * Gets the lower limit in meters.
		 */
		public float getLowerLimit() {
			return getRev().lowerTranslation;
		}
		/**
		 * Gets the upper limit in meters.
		 */
		public float getUpperLimit() {
			return getRev().upperTranslation;
		}
		/**
		 * Sets the lower and upper limits measured in meters.
		 */
		public void SetLimits(float LowerLimit, float UpperLimit) {
			getRev().lowerTranslation = LowerLimit;
			getRev().upperTranslation = UpperLimit;
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
	private PrismaticJoint getRev() {
		return (PrismaticJoint)joint;
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
	 * Gets or sets the maximum motor force, usually in N-m.
	 */
	public float getMaxMotorForce() {
		return getRev().getMaxMotorForce();
	}
	public void setMaxMotorForce(float f) {
		getRev().setMaxMotorForce(f);
	}
	/**
	 * Gets or sets the motor speed in meters per second.
	 */
	public float getMotorSpeed() {
		return getRev().getMotorSpeed();
	}
	public void setMotorSpeed(float f) {
		getRev().setMotorSpeed(f);
	}
	/**
	 * Gets the lower limit in meters.
	 */
	public float getLowerLimit() {
		return getRev().getLowerLimit();
	}
	/**
	 * Gets the upper limit in meters.
	 */
	public float getUpperLimit() {
		return getRev().getUpperLimit();
	}
	/**
	 * Sets the lower and upper limits measured in meters.
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
	 * Gets the current joint translation in meters.
	 */
	public float getJointTranslation(){
		return getRev().getJointTranslation();
	}
	/**
	 * Gets the current joint speed in meters per second.
	 */
	public float getJointSpeed() {
		return getRev().getJointSpeed();
	}
	
	
}
