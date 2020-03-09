
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

import org.jbox2d.collision.shapes.ChainShape;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.collision.shapes.ShapeType;
import org.jbox2d.common.Transform;

import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.objects.collections.List;

public class B2Shape<T extends Shape> extends AbsObjectWrapper<T>{
	public final Object SHAPE_CIRCLE = ShapeType.CIRCLE;
	public final Object SHAPE_POLYGON = ShapeType.POLYGON;
	public final Object SHAPE_CHAIN = ShapeType.CHAIN;
	public final Object SHAPE_EDGE = ShapeType.EDGE;
	/**
	 * Gets or sets the radius of the underlying shape. This can refer to different things depending on the
	 * shape type.
	 */
	public float getRadius() {
		return getObject().getRadius();
	}
	public void setRadius(float radius) {
		getObject().setRadius(radius);
	}
	/**
	 * Returns the shape type which is one of the SHAPE constants.
	 */
	public Object getShapeType() {
		return getObject().getType();
	}
	 /**
	   * Test a point for containment in this shape. This only works for convex shapes.
	   * 
	   * Transform - the shape world transform.
	   * Point - a point in world coordinates.
	   */
	public boolean TestPoint(B2Transform Transform, B2Vec2 Point) {
		return getObject().testPoint(Transform.t, Point.vec);
	}
	/**
	 * Computes the shape AABB based on the passed transform. The result is stored in the Output object.
	 */
	public void ComputeAABB(B2AABB Output, B2Transform Transform) {
		getObject().computeAABB(Output.aabb, Transform.t, 0);
	}
	@ShortName("B2Shape")
	public static class ConcreteB2Shape extends B2Shape<Shape> {

	}
	/**
	 * Circle shape.
	 */
	@ShortName("B2CircleShape")
	public static class B2CircleShape extends B2Shape<CircleShape> {
		public void Initialize(float Radius) {
			setObject(new CircleShape());
			getObject().setRadius(Radius);
		}
		public B2Vec2 getSupportVertex() {
			return new B2Vec2(getObject().m_p);
		}
	}
	/**
	 * A line segment (edge) shape. These can be connected in chains or loops to other edge shapes. The
	 * connectivity information is used to ensure correct contact normals.
	 */
	@ShortName("B2EdgeShape")
	public static class B2EdgeShape extends B2Shape<EdgeShape> {
		public void Initialize(B2Vec2 FromVec, B2Vec2 ToVec) {
			setObject(new EdgeShape());
			if (FromVec != null && ToVec != null)
				Set(FromVec, ToVec);
		}
		public void Set(B2Vec2 FromVec, B2Vec2 ToVec) {
			getObject().set(FromVec.vec, ToVec.vec);
		}
		public B2Vec2 getVertex1() {
			return new B2Vec2(getObject().m_vertex1);
		}
		public B2Vec2 getVertex2() {
			return new B2Vec2(getObject().m_vertex2);
		}
	}
	/**
	 * A chain shape is a free form sequence of line segments. The chain has two-sided collision, so you
	 * can use inside and outside collision. Therefore, you may use any winding order. Connectivity
	 * information is used to create smooth collisions. WARNING: The chain will not collide properly if
	 * there are self-intersections.
	 */
	@ShortName("B2ChainShape")
	public static class B2ChainShape extends B2Shape<ChainShape> {
		public void Initialize() {
			setObject(new ChainShape());
		}
		/**
		   * Create a chain with isolated end vertices.
		   */
		public void CreateChain(List Vertices) {
			getObject().createChain(B2Vec2.ListToArray(Vertices), Vertices.getSize());
		}
		/**
		   * Create a loop. This automatically adjusts connectivity.
		   */
		public void CreateLoop(List Vertices) {
			getObject().createLoop(B2Vec2.ListToArray(Vertices), Vertices.getSize());
		}
		/**
		 * Returns the number of edges.
		 */
		public int getEdgeCount() {
			return getObject().getChildCount();
		}
		/**
		 * Copies the edge to OutShape.
		 */
		public void GetEdge(int Index, B2EdgeShape OutShape) {
			getObject().getChildEdge(OutShape.getObject(), Index);
		}
		
	}
	/**
	 * A convex polygon shape. Polygons have a maximum number of vertices equal to 8.
	 * In most cases you should not need many vertices for a convex polygon.
	 */
	@ShortName("B2PolygonShape")
	public static class B2PolygonShape extends B2Shape<PolygonShape> {
		public void Initialize() {
			setObject(new PolygonShape());
		}
		/**
		 * Creates a box polygon. 
		 */
		public void SetAsBox(float HalfWidth, float HalfHeight) {
			getObject().setAsBox(HalfWidth, HalfHeight);
		}
		/**
		 * Creates a box polygon.
		 *HalfWidth / HalfHeight - Box dimensions.
		 *Center - Box center in local coordinates.
		 *Angle - Box rotation in local coordinates (radians).
		 */
		public void SetAsBox2(float HalfWidth, float HalfHeight, B2Vec2 Center, float Angle) {
			getObject().setAsBox(HalfWidth, HalfHeight, Center.vec, Angle);
		}
		/**
		 * Creates a convex polygon. Maximum number of vertices is 8.
		 */
		public void Set(List B2Vecs) {
			getObject().set(B2Vec2.ListToArray(B2Vecs), B2Vecs.getSize());
		}
		/**
		 * Returns the number of vertex.
		 */
		public int getVertexCount() {
			return getObject().getVertexCount();
		}
		/**
		 * Gets the vertex at the given index.
		 */
		public B2Vec2 GetVertex(int Index) {
			return new B2Vec2(getObject().getVertex(Index));
		}
	}
	
}
