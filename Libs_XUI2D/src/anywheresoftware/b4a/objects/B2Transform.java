
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

import org.jbox2d.common.Rot;
import org.jbox2d.common.Transform;

import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;

@ShortName("B2Transform")
public class B2Transform {
	@Hide
	public Transform t;
	/**
	 * Creates a new identity transform.
	 */
	public void Initialize() {
		t = new Transform();
		t.setIdentity();
	}
	/** Gets or sets the translation caused by the transform */
	public B2Vec2 getTranslation() {
		return new B2Vec2(t.p);
	}
	public void setTranslation(B2Vec2 v) {
		t.p.set(v.getX(), v.getY());
	}
	/** Gets or sets the rotation caused by the transform */
	public float getAngle() {
		return t.q.getAngle();
	}
	public void setAngle(float f) {
		t.q.set(f);
	}
	/**
	 * Multiplies the vector with the transformation rotation matrix.
	 */
	public B2Vec2 MultiplyRot (B2Vec2 v) {
		B2Vec2 res = new B2Vec2();
		Rot.mulToOut(t.q, v.vec, res.vec);
		return res;
	}
	
}
