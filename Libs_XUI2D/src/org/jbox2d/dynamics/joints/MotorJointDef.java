
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
 
 package org.jbox2d.dynamics.joints;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;

/**
* A motor joint is used to control the relative motion between two bodies. A typical usage is to
* control the movement of a dynamic body with respect to the ground.
*/
public class MotorJointDef extends JointDef {
  /**
   * Position of bodyB minus the position of bodyA, in bodyA's frame, in meters.
   */
  public final Vec2 linearOffset = new Vec2();

  /**
   * The bodyB angle minus bodyA angle in radians.
   */
  public float angularOffset;

  /**
   * The maximum motor force in N.
   */
  public float maxForce;

  /**
   * The maximum motor torque in N-m.
   */
  public float maxTorque;

  /**
   * Position correction factor in the range [0,1].
   */
  public float correctionFactor;

  public MotorJointDef() {
    super(JointType.MOTOR);
    angularOffset = 0;
    maxForce = 1;
    maxTorque = 1;
    correctionFactor = 0.3f;
  }

  public void initialize(Body bA, Body bB) {
    bodyA = bA;
    bodyB = bB;
    Vec2 xB = bodyB.getPosition();
    bodyA.getLocalPointToOut(xB, linearOffset);

    float angleA = bodyA.getAngle();
    float angleB = bodyB.getAngle();
    angularOffset = angleB - angleA;
  }
}
