/*
 * SonarQube, open source software quality management tool.
 * Copyright (C) 2008-2014 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * SonarQube is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * SonarQube is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.sonar.server.computation.component;

import static java.util.Objects.requireNonNull;

/**
 * A adapter of the {@link TypeAwareVisitor} to be able to visit only some component types
 */
public abstract class TypeAwareVisitorAdapter implements TypeAwareVisitor {

  protected final Component.Type maxDepth;
  protected final Order order;

  public TypeAwareVisitorAdapter(Component.Type maxDepth, Order order) {
    this.maxDepth = requireNonNull(maxDepth);
    this.order = requireNonNull(order);
  }

  @Override
  public Component.Type getMaxDepth() {
    return maxDepth;
  }

  @Override
  public Order getOrder() {
    return order;
  }

  /**
   * Called when encountering a Component of type {@link Component.Type#PROJECT}
   */
  @Override
  public void visitProject(Component project) {
    // empty implementation, meant to be override at will by subclasses
  }

  /**
   * Called when encountering a Component of type {@link Component.Type#MODULE}
   */
  @Override
  public void visitModule(Component module) {
    // empty implementation, meant to be override at will by subclasses
  }

  /**
   * Called when encountering a Component of type {@link Component.Type#DIRECTORY}
   */
  @Override
  public void visitDirectory(Component directory) {
    // empty implementation, meant to be override at will by subclasses
  }

  /**
   * Called when encountering a Component of type {@link Component.Type#FILE}
   */
  @Override
  public void visitFile(Component file) {
    // empty implementation, meant to be override at will by subclasses
  }

  /**
   * Called for any component, <strong>in addition</strong> to the methods specific to each type
   */
  @Override
  public void visitAny(Component any) {
    // empty implementation, meant to be override at will by subclasses
  }

}