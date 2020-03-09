
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


public class StackQueue<T> {

  private T[] m_buffer;
  private int m_front;
  private int m_back;
  private int m_end;

  public StackQueue() {}

  public void reset(T[] buffer) {
    m_buffer = buffer;
    m_front = 0;
    m_back = 0;
    m_end = buffer.length;
  }

  public void push(T task) {
    if (m_back >= m_end) {
      System.arraycopy(m_buffer, m_front, m_buffer, 0, m_back - m_front);
      m_back -= m_front;
      m_front = 0;
      if (m_back >= m_end) {
        return;
      }
    }
    m_buffer[m_back++] = task;
  }

  public T pop() {
    assert (m_front < m_back);
    return m_buffer[m_front++];
  }

  public boolean empty() {
    return m_front >= m_back;
  }

  public T front() {
    return m_buffer[m_front];
  }
}
