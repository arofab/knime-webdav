/**
 * ------------------------------------------------------------------------
 *
 * Copyright (C) 2018 Fabrizio Arosio
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * ------------------------------------------------------------------------
 */
package it.arofab.filehandling.remote;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * This is the eclipse bundle activator.
 *
 * @author Fabrizio.Arosio
 */
public class FileHandlingRemotePlugin extends Plugin {
    // The shared instance.
    private static FileHandlingRemotePlugin plugin;

    /**
     * The constructor.
     */
    public FileHandlingRemotePlugin() {
        super();
        plugin = this;
    }

    /**
     * This method is called upon plug-in activation.
     * 
     * @param context The OSGI bundle context
     * @throws Exception If this plugin could not be started
     */
    @Override
    public void start(final BundleContext context) throws Exception {
        super.start(context);

    }

    /**
     * This method is called when the plug-in is stopped.
     * 
     * @param context The OSGI bundle context
     * @throws Exception If this plugin could not be stopped
     */
    @Override
    public void stop(final BundleContext context) throws Exception {
        super.stop(context);
        plugin = null;
    }

    /**
     * Returns the shared instance.
     * 
     * @return Singleton instance of the Plugin
     */
    public static FileHandlingRemotePlugin getDefault() {
        return plugin;
    }

}

