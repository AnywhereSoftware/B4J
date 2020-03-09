
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

import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;

import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.objects.B2Shape.ConcreteB2Shape;

@ShortName("B2Fixture")
public class B2Fixture{
	@Hide
	public Fixture fixture;
	public Object Tag;
	public boolean getIsInitialized() {
		return fixture != null;
	}
	/**
	   * Get the child shape. You can modify the child shape, however you should not change the number
	   * of vertices because this will crash some collision caching mechanisms.
	   */
	public ConcreteB2Shape getShape() {
		return (ConcreteB2Shape) AbsObjectWrapper.ConvertToWrapper(new ConcreteB2Shape(), fixture.getShape());
	}
	/**
	 * Get the body that this fixture is attached to.
	 */
	public B2Body getBody() {
		Body b = fixture.getBody();
		if (b == null)
			return new B2Body();
		else
			return (B2Body)b.getUserData();
	}
	/**
	 * The friction coefficient, usually in the range [0,1].
	 */
	public float getFriction() {
		return fixture.getFriction();
	}
	public void setFriction(float f) {
		fixture.setFriction(f);
	}
	/**
	 * The restitution (elasticity) usually in the range [0,1].
	 */
	public float getRestitution() {
		return fixture.getRestitution();
	}
	public void setRestitution(float f) {
		fixture.setRestitution(f);
	}
	/**
	 * The density, usually in kg/m^2
	 */
	public float getDensity() {
		return fixture.getDensity();
	}
	public void setDensity(float f) {
		fixture.setDensity(f);
	}
	
	/**
	 * A sensor shape collects contact information but never generates a collision response.
	 */
	public boolean getIsSensor() {
		return fixture.isSensor();
	}
	public void setIsSensor(boolean b) {
		fixture.setSensor(b);
	}
	/**
	 *Limited to lower 16 bits.
	 * CategoryBits - A bit (or more) that defines the current fixture. Default value: 1.
	 * MaskBits - Bits defining the other fixtures that will collide with this fixture. Default value 0xFFFF.
	 */
	public void SetFilterBits(int CategoryBits, int MaskBits) {
		fixture.getFilterData().categoryBits = CategoryBits;
		fixture.getFilterData().maskBits = MaskBits;
		fixture.refilter();
	}
	
	/**
	 * Returns the next fixture. Returns Null if this is the last fixture.
	 */
	public B2Fixture NextFixture() {
		if (fixture.m_next == null)
			return null;
		return (B2Fixture)fixture.m_next.m_userData;
	}
	
	
	@ShortName("B2FixtureDef")
	public static class B2FixtureDef {
		@Hide
		public FixtureDef fd = new FixtureDef();
		/**
		 * The friction coefficient, usually in the range [0,1].
		 */
		public float getFriction() {
			return fd.friction;
		}
		public void setFriction(float f) {
			fd.friction = f;
		}
		/**
		 * The restitution (elasticity) usually in the range [0,1].
		 */
		public float getRestitution() {
			return fd.restitution;
		}
		public void setRestitution(float f) {
			fd.restitution = f;
		}
		/**
		 * The density, usually in kg/m^2
		 */
		public float getDensity() {
			return fd.density;
		}
		public void setDensity(float f) {
			fd.density = f;
		}
		/**
		 * The shape, this must be set. The shape will be cloned when the fixture is created.
		 */
		public ConcreteB2Shape getShape() {
			return (ConcreteB2Shape) AbsObjectWrapper.ConvertToWrapper(new ConcreteB2Shape(), fd.shape);
		}
		public void setShape(Shape Shape) {
			fd.shape = Shape;
		}
		/**
		 * A sensor shape collects contact information but never generates a collision response.
		 */
		public boolean getIsSensor() {
			return fd.isSensor;
		}
		public void setIsSensor(boolean b) {
			fd.isSensor = b;
		}
		/**
		 *Limited to lower 16 bits.
		 * CategoryBits - A bit (or more) that defines the current fixture. Default value: 1.
		 * MaskBits - Bits defining the other fixtures that will collide with this fixture. Default value 0xFFFF.
		 */
		public void SetFilterBits(int CategoryBits, int MaskBits) {
			fd.filter.categoryBits = CategoryBits;
			fd.filter.maskBits = MaskBits;
		}


	}
}
