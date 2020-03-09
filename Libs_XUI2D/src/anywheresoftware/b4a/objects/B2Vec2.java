
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

import org.jbox2d.common.Vec2;

import anywheresoftware.b4a.BA.B4aDebuggable;
import anywheresoftware.b4a.BA.CustomClass;
import anywheresoftware.b4a.BA.CustomClasses;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.objects.collections.List;
@CustomClasses(values = {
		@CustomClass(name = "Body Delegate Class (XUI2D)", fileNameWithoutExtension = "bodydelegate"),
		@CustomClass(name = "Game Class (XUI2D)", fileNameWithoutExtension = "game")
})
@Version(1.02f)
@ShortName("B2Vec2")
public class B2Vec2 implements B4aDebuggable{
	@Hide
	public Vec2 vec;
	public B2Vec2() {
		vec = new Vec2();
	}
	public B2Vec2(Vec2 v) {
		vec = v;
	}
	public float getX() {
		return vec.x;
	}
	public void setX(float x) {
		vec.x = x;
	}
	public float getY() {
		return vec.y;
	}
	public void setY(float y) {
		vec.y = y;
	}
	 /** Set the vector component-wise. */
	public void Set(float X, float Y) {
		vec.set(X, Y);
	}
	/** Add another vector to this one and returns result - alters this vector. */
	public void AddToThis(B2Vec2 Other) {
		vec.addLocal(Other.vec);
	}
	/** Subtract another vector from this one and return result - alters this vector. */
	public void SubtractFromThis(B2Vec2 Other) {
		vec.subLocal(Other.vec);
	}
	/** Return the negation of this vector; does not alter this vector. */
	public B2Vec2 Negate() {
		return new B2Vec2(vec.negate());
	}
	/** Creates a copy of the current vector. */
	public B2Vec2 CreateCopy() {
		return new B2Vec2(vec.clone());
	}
	 /** Multiply this vector by a number and return result - alters this vector. */
	public void MultiplyThis(float Scalar) {
		vec.mulLocal(Scalar);
	}
	 /** Normalize this vector and return the length before normalization. Alters this vector. */
	public float NormalizeThis() {
		return vec.normalize();
	}
	 /** Return the length of this vector. */
	public float getLength() {
		return vec.length();
	}
	 /** Return the squared length of this vector. */
	public float getLengthSquared() {
		return vec.lengthSquared();
	}
	/**
	 * Tests whether the two vectors are equal.
	 */
	public boolean Equals (B2Vec2 Other) {
		return vec.x == Other.getX() && vec.y == Other.getY();
	}
	@Hide
	public static Vec2[] ListToArray(List list) {
		Vec2[] v = new Vec2[list.getSize()];
		for (int i = 0;i < list.getSize();i++)
			v[i] = ((B2Vec2)list.Get(i)).vec;
		return v;
	}
	@Hide
	@Override
	public String toString() {
		return String.valueOf(vec);
	}
	@Hide
	@Override
	public Object[] debug(int limit, boolean[] outShouldAddReflectionFields) {
		Object[] res = new Object[2 * 2];
		res[0] = "ToString";
		res[1] = toString();
		outShouldAddReflectionFields[0] = false;
		return res;
	}
	
	
	
	
	
}
