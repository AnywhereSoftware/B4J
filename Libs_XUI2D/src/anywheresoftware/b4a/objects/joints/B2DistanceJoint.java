
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

import org.jbox2d.dynamics.joints.DistanceJoint;
import org.jbox2d.dynamics.joints.DistanceJointDef;

import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.objects.B2Body;
import anywheresoftware.b4a.objects.B2Vec2;

/**
 * A distance joint constrains two points on two bodies to remain at a fixed distance from each
 * other. You can view this as a massless, rigid rod.
 */
@ShortName("B2DistanceJoint")
public class B2DistanceJoint extends B2Joint{
	@ShortName("B2DistanceJointDef")
	public static class B2DistanceJointDef extends B2JointDef {
		private DistanceJointDef getRev() {
			return (DistanceJointDef)def;
		}
		/**
		 * Sets the bodies and the Connection points.
		 */
		public void Initialize(B2Body BodyA, B2Body BodyB, B2Vec2 WorldPointA, B2Vec2 WorldPointB) {
			def = new DistanceJointDef();
			DistanceJointDef d = getRev();
			d.initialize(BodyA.body, BodyB.body, WorldPointA.vec, WorldPointB.vec);

		}
		public float getLength() {
			return getRev().length;
		}
		public void setLength(float f) {
			getRev().length = f;
		}
		/**
		 * Spring frequency, zero indicates no suspension.
		 */
		public float getFrequencyHz() {
			return getRev().frequencyHz;
		}
		public void setFrequencyHz(float f) {
			getRev().frequencyHz = f;
		}

		/**
		 * Spring damping ratio, one indicates critical damping.
		 */
		public float getDampingRatio() {
			return getRev().dampingRatio;
		}
		public void setDampingRatio(float f) {
			getRev().dampingRatio = f;
		}
	}
	private DistanceJoint getRev() {
		return (DistanceJoint)joint;
	}
	public float getFrequencyHz() {
		return getRev().getFrequency();
	}
	public void setFrequencyHz(float f) {
		getRev().setFrequency(f);
	}
	public float getDampingRatio() {
		return getRev().getDampingRatio();
	}
	public void setDampingRatio(float f) {
		getRev().setDampingRatio(f);
	}
	public float getLength() {
		return getRev().getLength();
	}
	public void setLength(float f) {
		getRev().setLength(f);
	}
}
