/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.xwiki.extension.handler;

import org.xwiki.component.annotation.ComponentRole;

/**
 * Handle installed extensions initialization (during application startup, reinitialization...).
 *
 * @version $Id$
 */
@ComponentRole
public interface ExtensionInitializer
{
    /**
     * Initialize all installed extensions.
     */
    void initialize();

    /**
     * Initialize installed extensions of the provided namespace.
     * @param namespace namespace to initialize
     */
    void initialize(String namespace);

    /**
     * Initialize installed extensions of the provided type.
     * @param namespace namespace to initialize
     * @param type type of extension to initialize
     */
    void initialize(String namespace, String type);
}
