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
package it.arofab.filehandling.remote.connectioninformation.webdav;

import org.knime.base.filehandling.remote.connectioninformation.node.ConnectionInformationNodeDialog;
import org.knime.base.filehandling.remote.connectioninformation.node.ConnectionInformationNodeModel;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

import it.arofab.filehandling.remote.files.webdav.WebDAVRemoteFileHandler;

/**
 * @author Fabrizio.Arosio
 *
 */
public class HTTPSWebDAVConnectionInformationNodeFactory extends NodeFactory<ConnectionInformationNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public ConnectionInformationNodeModel createNodeModel() {
        return new ConnectionInformationNodeModel(WebDAVRemoteFileHandler.WEBDAVS_PROTOCOL);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNrNodeViews() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<ConnectionInformationNodeModel> createNodeView(final int viewIndex,
            final ConnectionInformationNodeModel nodeModel) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasDialog() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeDialogPane createNodeDialogPane() {
    	return new ConnectionInformationNodeDialog(WebDAVRemoteFileHandler.WEBDAVS_PROTOCOL);
    }
}
