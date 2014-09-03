/**
 * Copyright (C) 2014 Philip Helger (www.helger.com)
 * philip[at]helger[dot]com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.helger.schematron.pure.model;

import java.io.Serializable;

import javax.annotation.Nonnull;

import com.helger.commons.log.InMemoryLogger;
import com.helger.commons.microdom.IMicroElement;

/**
 * Base interface for a single Pure Schematron element
 * 
 * @author Philip Helger
 */
public interface IPSElement extends Serializable
{
  /**
   * @param aLogger
   *        The logger where the error details are stored. May not be
   *        <code>null</code>.
   * @return <code>true</code> if all mandatory fields are set and the element
   *         is valid, <code>false</code> otherwise.
   */
  boolean isValid (@Nonnull InMemoryLogger aLogger);

  /**
   * @return <code>true</code> if this element conforms to the Schematron
   *         minimal syntax, <code>false</code> otherwise.
   */
  boolean isMinimal ();

  /**
   * @return The XML representation of this element. Never <code>null</code>.
   */
  @Nonnull
  IMicroElement getAsMicroElement ();
}