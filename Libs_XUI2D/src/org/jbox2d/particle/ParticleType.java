
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

/**
 * The particle type. Can be combined with | operator. Zero means liquid.
 * 
 * @author dmurph
 */
public class ParticleType {
  public static final int b2_waterParticle = 0;
  /** removed after next step */
  public static final int b2_zombieParticle = 1 << 1;
  /** zero velocity */
  public static final int b2_wallParticle = 1 << 2;
  /** with restitution from stretching */
  public static final int b2_springParticle = 1 << 3;
  /** with restitution from deformation */
  public static final int b2_elasticParticle = 1 << 4;
  /** with viscosity */
  public static final int b2_viscousParticle = 1 << 5;
  /** without isotropic pressure */
  public static final int b2_powderParticle = 1 << 6;
  /** with surface tension */
  public static final int b2_tensileParticle = 1 << 7;
  /** mixing color between contacting particles */
  public static final int b2_colorMixingParticle = 1 << 8;
  /** call b2DestructionListener on destruction */
  public static final int b2_destructionListener = 1 << 9;
}
