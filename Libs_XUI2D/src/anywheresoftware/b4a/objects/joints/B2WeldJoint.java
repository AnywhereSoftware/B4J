
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

import org.jbox2d.dynamics.joints.WeldJointDef;

import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.objects.B2Body;
import anywheresoftware.b4a.objects.B2Vec2;

/**
 * A joint that strictly connects two bodies together.
 */
@ShortName("B2WeldJoint")
public class B2WeldJoint extends B2Joint{
	@ShortName("B2WeldJointDef")
	public static class B2WeldJointDef extends B2JointDef {
		private WeldJointDef getRev() {
			return (WeldJointDef)def;
		}
		/**
		 * Sets the bodies and the anchor based on the current positions.
		 */
		public void Initialize(B2Body BodyA, B2Body BodyB, B2Vec2 AnchorPoint) {
			def = new WeldJointDef();
			getRev().initialize(BodyA.body, BodyB.body, AnchorPoint.vec);
		}
	}
	
}
