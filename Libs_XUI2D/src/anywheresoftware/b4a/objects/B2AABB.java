
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

import org.jbox2d.collision.AABB;

import anywheresoftware.b4a.BA.B4aDebuggable;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;

@ShortName("B2AABB")
public class B2AABB implements B4aDebuggable{
	@Hide
	public AABB aabb;
	private B2Vec2 bottomLeft, topRight;

	public void Initialize() {
		aabb = new AABB();
		bottomLeft = null;
		topRight = null;
	}
	public void Initialize2(B2Vec2 BottomLeft, B2Vec2 TopRight) {
		aabb = new AABB(BottomLeft.vec, TopRight.vec);
		bottomLeft = null;
		topRight = null;
	}
	/**
	 * Returns the bottom left point. You can modify it.
	 */
	public B2Vec2 getBottomLeft() {
		if (bottomLeft == null)
			bottomLeft = new B2Vec2(aabb.lowerBound);
		return bottomLeft;
	}
	/**
	 * Returns the top right point. You can modify it.
	 */
	public B2Vec2 getTopRight() {
		if (topRight == null)
			topRight = new B2Vec2(aabb.upperBound);
		return topRight;
	}
	public float getWidth() {
		return aabb.upperBound.x - aabb.lowerBound.x;
	}
	public float getHeight() {
		return aabb.upperBound.y - aabb.lowerBound.y;
	}
	public B2Vec2 getCenter() {
		return new B2Vec2(aabb.getCenter());
	}
	/**
	 * Combine this AABB with the Other AABB.
	 */
	public void Combine(B2AABB Other) {
		aabb.combine(Other.aabb);
	}
	/**
	 * Returns true if this AABB contains the Other AABB.
	 */
	public boolean Contains(B2AABB Other) {
		return aabb.contains(Other.aabb);
	}
	/**
	 * Returns true if this AABB overlaps the Other AABB.
	 */
	public boolean TestOverlap(B2AABB Other) {
		return AABB.testOverlap(aabb, Other.aabb);
	}

	@Hide
	@Override
	public String toString() {
		return String.valueOf(aabb);
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
