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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.DefaultSchemePortResolver;
import org.knime.base.filehandling.remote.connectioninformation.port.ConnectionInformation;
import org.knime.base.filehandling.remote.files.Connection;
import org.knime.base.filehandling.remote.files.RemoteFile;
import org.knime.base.filehandling.remote.files.RemoteFileHandlerRegistry;
import org.knime.core.node.ExecutionContext;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.impl.SardineImpl;

/**
 * The WebDAV remote file.
 * 
 * @author Fabrizio.Arosio
 */
public class WebDAVRemoteFile extends RemoteFile<Connection> {
	private final String actualScheme;
	private URI actualURI = null;
	private Boolean cachedExists = null;
	private DavResource cachedResource = null;
	
	/**
	 * Remove the user information from an URI.
	 * @param uri Source {@link URI}.
	 * @return {@link URI} without user information.
	 */
	private static final URI removeURIUserInfo(final URI uri) {
		try {
			return new URI(uri.getScheme(), null, uri.getHost(), uri.getPort(), uri.getPath(), uri.getQuery(), uri.getFragment());
		} catch (URISyntaxException e) {
			return null;
		}
	}
	
	/**
	 * Create a WEBDAV remote file.
	 * @param uri The uri with the scheme registered in KNIME {@link RemoteFileHandlerRegistry}, pointing to the file
	 * @param actualScheme The actual scheme, pointing to the file
	 * @param connectionInformation  Connection information to the file
	 */
	public WebDAVRemoteFile(final URI uri, final String actualScheme, final ConnectionInformation connectionInformation) {
		super(removeURIUserInfo(uri), connectionInformation, null);
		this.actualScheme = actualScheme;
		
	}
	
	/**
	 * Create Sardine implementation with current user name, password, connection port and timeout.
	 * @return A {@link Sardine} instance.
	 * @throws Exception
	 */
	private Sardine createSardine() throws Exception {
		ConnectionInformation info = getConnectionInformation();
		final int port = info.getPort();
		final int timeout = info.getTimeout();
		
		String username = info.getUser();
		final String password;
		if (username != null && username.length() > 0) 
			password = info.getPassword();
		else {
			username = null;
			password = null;
		}
		
		return new SardineImpl(username, password) {
			@Override
			protected HttpClientBuilder configure(ProxySelector selector, CredentialsProvider credentials) {
				HttpClientBuilder builder = super.configure(selector, credentials);
				return builder.setDefaultRequestConfig(RequestConfig.custom()
						// Only selectively enable this for PUT but not all entity enclosing methods
						.setExpectContinueEnabled(false)
						.setConnectTimeout(timeout)
						.build());
			}
						
			@Override
			protected DefaultSchemePortResolver createDefaultSchemePortResolver() {
				return new DefaultSchemePortResolver() {
					@Override
					public int resolve(HttpHost httpHost) {
						return port;
					}
				};
			}			
		};
	}
	
	private void resetCache() {
		cachedExists = null;
		cachedResource = null;
	}
	
	/**
	 * Get the WEBDAV resource for current URI.
	 * Cache the resource.
	 * @param sardine The {@link Sardine} instance.
	 * @return {@link DavReource} or null if resource have not been found.
	 * @throws Exception
	 */
	private DavResource getResource(Sardine sardine) throws Exception {
		if (cachedResource != null)
			return cachedResource;
		
		List<DavResource> resources = sardine.list(getActualURI().toString(), 0);
		if (resources.size() == 0)
			return null;
		else {
			DavResource resource = resources.get(0);
			if (DavResource.DEFAULT_STATUS_CODE == resource.getStatusCode())
				cachedResource = resource;
			else
				cachedResource = null;
			return cachedResource;
		}
	}
	
	/**
	 * Get the WEBDAV resource for current URI.
	 * Cache the resource.
	 * Discard Sardine HTTPClient resources.
	 * @return {@link DavReource} or null if resource have not been found.
	 * @throws Exception
	 */
	private DavResource getResource() throws Exception {
		if (cachedResource != null)
			return cachedResource;

		Sardine sardine = createSardine();
		try {
			return getResource(sardine);
		}
		finally {
			sardine.shutdown();
		}
	}
	
	/**
	 * Check whether a remote file is in current WebDAV host.
	 * @param file
	 * @return
	 */
	private boolean sameWebDAVHost(final RemoteFile<Connection> file) {
		return file instanceof WebDAVRemoteFile && getIdentifier().equals(file.getIdentifier());
	}

	/**
	 * The actual scheme of remote file, not the scheme returned by {@link #getType()}: the latter is used for 
	 * the remote file registration in KNIME {@link RemoteFileHandlerRegistry}.
	 * @return The actual scheme string. 
	 */
	public final String getScheme() {
		return actualScheme;
	}

	/**
	 * The actual URI, different than {@link #getURI()}, which is used for the remote 
	 * file registration in KNIME {@link RemoteFileHandlerRegistry}.
	 * @return A URI.
	 * @throws URISyntaxException
	 */
	private URI getActualURI() throws URISyntaxException {
		if (actualURI == null) {
			URI uri = getURI();
			actualURI = new URI(getScheme(), null, uri.getHost(), uri.getPort(), uri.getPath(), uri.getQuery(), uri.getFragment());
		}
			
		return this.actualURI;
	}
		
	@Override
	public String getName() throws Exception {
        String name;
        if (isDirectory()) {
            // Remove '/' from path and separate name
            String path = getPath();
            path = path.substring(0, path.length() - 1);
            name = FilenameUtils.getName(path);            	
        } else {
            // Use name from URI
            name = FilenameUtils.getName(getURI().getPath());
        }
        return name;
	}

	@Override
	protected boolean usesConnection() {
		// HTTP, by design, builds a new connection for every request
		return false;
	}

	@Override
	protected Connection createConnection() {
		// do not use a persistent connection
		return null;
	}

	@Override
	public String getType() {
		return getURI().getScheme();
	}

	@Override
	public boolean exists() throws Exception {
		if (cachedExists != null)
			return cachedExists;
		Sardine sardine = createSardine();
		try {
			cachedExists = sardine.exists(getActualURI().toString());
			return cachedExists;
		}
		finally {
			sardine.shutdown();	
		}
	}

	@Override
	public boolean isDirectory() throws Exception {
		DavResource res = getResource();
		return res == null ? false : res.isDirectory(); 
	}

	@Override
	public void move(RemoteFile<Connection> file, ExecutionContext exec) throws Exception {
		if (sameWebDAVHost(file)) {
			final WebDAVRemoteFile source = (WebDAVRemoteFile)file;
			Sardine sardine = createSardine();
			try {
				String sourceURI;
				if (source instanceof WebDAVRemoteFile)
					sourceURI = ((WebDAVRemoteFile)source).getActualURI().toString();
				else
					sourceURI = source.getURI().toString();

				sardine.move(sourceURI, getActualURI().toString());
			}
			catch (IOException e) {
				throw new Exception("Move operation failed.");
			}
			finally {
				resetCache();
				sardine.shutdown();
			}
		}
		else {
			super.move(file, exec);
			resetCache();
		}
	}

	@Override
	public void write(RemoteFile<Connection> file, ExecutionContext exec) throws Exception {
		Sardine sardine = createSardine();
		try (final InputStream in = file.openInputStream()) {
			final String uri = getActualURI().toString();
	        sardine.put(uri, in, "application/octet-stream");
		}
		finally {
			sardine.shutdown();
		}
	}
	
	@Override
	public InputStream openInputStream() throws Exception {
		Sardine sardine = createSardine();
		return sardine.get(getActualURI().toString());
	}

	/**
	 * Output stream not supported.
	 * @throws UnsupportedOperationException
	 */
	@Override
	public OutputStream openOutputStream() throws Exception {
		throw new UnsupportedOperationException(unsupportedMessage("openOutputStream"));
	}

	@Override
	public long getSize() throws Exception {
		DavResource res = getResource();
		return res == null ? 0L : res.getContentLength();
	}

	@Override
	public long lastModified() throws Exception {
		DavResource res = getResource();
		return res == null ? 0L : res.getModified().getTime() / 1000L;
	}

	@Override
	public boolean delete() throws Exception {
		if (!exists())
			return false;
		Sardine sardine = createSardine();
		try {
			sardine.delete(getActualURI().toString());
			return true;
		}
		finally {
			resetCache();
			sardine.shutdown();
		}
	}

	@Override
	public RemoteFile<Connection>[] listFiles() throws Exception {
		Sardine sardine = createSardine();
		try {
			final DavResource thisResource = getResource(sardine);
			if (!thisResource.isDirectory())
				return new WebDAVRemoteFile[0];
			final List<DavResource> resources = sardine.list(getActualURI().toString(), 1);
			final List<WebDAVRemoteFile> files = new ArrayList<>(resources.size());
			final URI thisUri = getURI();
			final String thisPath = getPath();
			final int cnt = resources.size();
			for(int i=0; i<cnt; i++) {
				final DavResource res = resources.get(i);
				String filename = res.getName();
				if (thisResource.getName().equals(filename))
					continue;
				
                // Build URI
                final URI uri = new URI(thisUri.getScheme(), null, thisUri.getHost(),
                    thisUri.getPort(), thisPath + res.getName(), thisUri.getQuery(), thisUri.getFragment());
                WebDAVRemoteFile file = new WebDAVRemoteFile(uri, getScheme(), getConnectionInformation());
                file.cachedResource = res;
                file.cachedExists = true;
                files.add(file);
			}
			files.sort(null);
			return files.toArray(new WebDAVRemoteFile[files.size()]);
		}
		finally {
			sardine.shutdown();
		}
	}

	@Override
	public boolean mkDir() throws Exception {
		Sardine sardine = createSardine();
		try {
			sardine.createDirectory(getActualURI().toString());
			return true;
		}
		catch (IOException e) {
			return false;
		}
		finally {
			resetCache();
			sardine.shutdown();
		}
	}
}
