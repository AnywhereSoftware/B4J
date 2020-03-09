
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
import org.jbox2d.collision.Manifold;
import org.jbox2d.collision.ManifoldPoint;
import org.jbox2d.collision.WorldManifold;

import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;

@ShortName("B2WorldManifold")
public class B2WorldManifold {
	@Hide
	public WorldManifold wm = new WorldManifold();
	public int PointCount;
	public B2Vec2 getNormal() {
		return new B2Vec2(wm.normal);
	}
	 /**
	   * World contact point (point of intersection)
	   */
	public B2Vec2 GetPoint(int Index) {
		return new B2Vec2(wm.points[Index]);
	}
	@ShortName("B2Manifold")
	public static class B2Manifold {
		@Hide
		public Manifold manifold;
		public int getPointCount() {
			return manifold.pointCount;
		}
		public B2ManifoldPoint GetManifoldPoint(int Index) {
			B2ManifoldPoint m = new B2ManifoldPoint();
			m.manifoldPoint = manifold.points[Index];
			return m;
		}
		
	}
	@ShortName("B2ManifoldPoint")
	public static class B2ManifoldPoint {
		@Hide
		public ManifoldPoint manifoldPoint;
		public B2Vec2 getLocalPoint() {
			return new B2Vec2(manifoldPoint.localPoint);
		}
		public float getNormalImpulse() {
			return manifoldPoint.normalImpulse;
		}
		public float getTangentImpulse() {
			return manifoldPoint.tangentImpulse;
		}
	}
	@ShortName("B2ContactImpulse")
	public static class B2ContactImpulse {
		@Hide
		public ContactImpulse impulse;
		public int getPointCount() {
			return impulse.count;
		}
		public float GetNormalImpulse(int Index) {
			return impulse.normalImpulses[Index];
		}
		public float GetTangentImpulse(int Index) {
			return impulse.tangentImpulses[Index];
		}
	}
}
