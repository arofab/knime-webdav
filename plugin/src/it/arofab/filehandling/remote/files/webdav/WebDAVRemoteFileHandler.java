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
package it.arofab.filehandling.remote.files.webdav;

import java.net.URI;

import org.knime.base.filehandling.remote.connectioninformation.port.ConnectionInformation;
import org.knime.base.filehandling.remote.files.Connection;
import org.knime.base.filehandling.remote.files.ConnectionMonitor;
import org.knime.base.filehandling.remote.files.Protocol;
import org.knime.base.filehandling.remote.files.RemoteFile;
import org.knime.base.filehandling.remote.files.RemoteFileHandler;

/**
 * @author Fabrizio.Arosio
 *
 */
public class WebDAVRemoteFileHandler implements RemoteFileHandler<Connection> {
	/** create fake WEBDAV , WEBDAVS protocol names as HTTP and HTTPS protocols are used */
    public static final Protocol WEBDAV_PROTOCOL =
            new Protocol("webdav", 80, true, false, false, true, true, true, true, false);

    public static final Protocol WEBDAVS_PROTOCOL =
            new Protocol("webdavs", 443, true, false, false, true, true, true, true, false);
    
	@Override
	public Protocol[] getSupportedProtocols() {
		return new Protocol[] { WEBDAV_PROTOCOL, WEBDAVS_PROTOCOL };
	}

	@Override
	public RemoteFile<Connection> createRemoteFile(URI uri, ConnectionInformation connectionInformation,
			ConnectionMonitor<Connection> connectionMonitor) throws Exception {
        
		//change protocol name to HTTP / HTTPS for URI schema
        final String scheme = uri.getScheme().replaceFirst("webdav", "http");
        return new WebDAVRemoteFile(uri, scheme, connectionInformation);
	}



}
