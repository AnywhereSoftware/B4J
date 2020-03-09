
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
 
 package org.jbox2d.pooling.arrays;

import java.util.HashMap;

import org.jbox2d.particle.VoronoiDiagram;

public class GeneratorArray {

  private final HashMap<Integer, VoronoiDiagram.Generator[]> map =
      new HashMap<Integer, VoronoiDiagram.Generator[]>();

  public VoronoiDiagram.Generator[] get(int length) {
    assert (length > 0);

    if (!map.containsKey(length)) {
      map.put(length, getInitializedArray(length));
    }

    assert (map.get(length).length == length) : "Array not built of correct length";
    return map.get(length);
  }

  protected VoronoiDiagram.Generator[] getInitializedArray(int length) {
    final VoronoiDiagram.Generator[] ray = new VoronoiDiagram.Generator[length];
    for (int i = 0; i < ray.length; i++) {
      ray[i] = new VoronoiDiagram.Generator();
    }
    return ray;
  }
}
