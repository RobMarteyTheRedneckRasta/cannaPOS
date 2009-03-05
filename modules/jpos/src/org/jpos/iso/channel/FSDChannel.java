/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2009 Alejandro P. Revilla
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jpos.iso.channel;

import java.io.IOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOException;
import org.jpos.util.FSDMsg;
import org.jpos.util.Logger;
import org.jpos.util.LogEvent;
import org.jpos.iso.FSDISOMsg;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;

public class FSDChannel extends NACChannel {
    String schema;

    public ISOMsg createMsg() {
        return new FSDISOMsg (new FSDMsg (schema));
    }
    public void setConfiguration (Configuration cfg)
        throws ConfigurationException 
    {
        super.setConfiguration (cfg);
        schema = cfg.get ("schema");
    }
    protected int getMessageLength() throws IOException, ISOException {
        int len = super.getMessageLength();
        LogEvent evt = new LogEvent (this, "fsd-channel-debug");
        evt.addMessage ("received message length: " + len);
        Logger.log (evt);
        return len;
    }
}

