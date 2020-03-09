
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
 
 package org.jbox2d.particle;

import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;

/**
 * A particle group definition holds all the data needed to construct a particle group. You can
 * safely re-use these definitions.
 */
public class ParticleGroupDef {

  /** The particle-behavior flags. */
  public int flags;

  /** The group-construction flags. */
  public int groupFlags;

  /**
   * The world position of the group. Moves the group's shape a distance equal to the value of
   * position.
   */
  public final Vec2 position = new Vec2();

  /**
   * The world angle of the group in radians. Rotates the shape by an angle equal to the value of
   * angle.
   */
  public float angle;

  /** The linear velocity of the group's origin in world co-ordinates. */
  public final Vec2 linearVelocity = new Vec2();

  /** The angular velocity of the group. */
  public float angularVelocity;

  /** The color of all particles in the group. */
  public ParticleColor color;

  /**
   * The strength of cohesion among the particles in a group with flag b2_elasticParticle or
   * b2_springParticle.
   */
  public float strength;

  /** Shape containing the particle group. */
  public Shape shape;

  /** If true, destroy the group automatically after its last particle has been destroyed. */
  public boolean destroyAutomatically;

  /** Use this to store application-specific group data. */
  public Object userData;

  public ParticleGroupDef() {
    flags = 0;
    groupFlags = 0;
    angle = 0;
    angularVelocity = 0;
    strength = 1;
    destroyAutomatically = true;
  }
}
