package org.jpos.iso;

import java.io.*;
import java.util.*;
import org.jpos.util.Logger;
import org.jpos.util.LogProducer;
import org.jpos.util.LogEvent;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

/*
 * $Log$
 * Revision 1.1  2000/03/05 02:16:37  apr
 * Added XMLPackager
 *
 */

/**
 * packs/unpacks ISOMsgs into XML representation
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOPackager
 */
public class XMLPackager extends HandlerBase
                         implements ISOPackager, LogProducer
{
    protected Logger logger = null;
    protected String realm = null;
    private ByteArrayOutputStream out;
    private PrintStream p;
    private Parser parser = null;
    private Stack stk;

    public static final String ISOMSG_TAG    = "isomsg";
    public static final String ISOFIELD_TAG  = "field";
    public static final String ID_ATTR       = "id";
    public static final String VALUE_ATTR    = "value";
    public static final String TYPE_ATTR     = "type";
    public static final String TYPE_BINARY   = "binary";

    public XMLPackager() throws ISOException {
	super();
	out = new ByteArrayOutputStream();
	p   = new PrintStream(out);
	stk = new Stack();
	try {
	    parser = ParserFactory.makeParser();
	    parser.setDocumentHandler (this);
	} catch (Exception e) {
	    throw new ISOException (e.toString());
	}
    }
    public byte[] pack (ISOComponent c) throws ISOException {
	LogEvent evt = new LogEvent (this, "pack");
	try {
	    if (!(c instanceof ISOMsg))
		throw new ISOException ("cannot pack "+c.getClass());
	    ISOMsg m = (ISOMsg) c;
	    byte[] b;
	    synchronized (this) {
		c.dump (p, "");
		b = out.toByteArray();
		out.reset();
	    }
	    if (logger != null)
		evt.addMessage (m);
	    return b;
	} catch (ISOException e) {
	    evt.addMessage (e);
	    throw e;
	} finally {
	    Logger.log(evt);
	}
    }

    public synchronized int unpack (ISOComponent c, byte[] b) 
	throws ISOException
    {
	LogEvent evt = new LogEvent (this, "unpack");
	try {
	    if (!(c instanceof ISOMsg))
		throw new ISOException 
		    ("Can't call packager on non Composite");

	    while (!stk.empty())    // purge from possible previous error
		stk.pop();

	    InputSource src = new InputSource (new ByteArrayInputStream(b));
	    parser.parse (src);
	    if (stk.empty())
		throw new ISOException ("error parsing");

	    ISOMsg m = (ISOMsg) c;
	    m.merge ((ISOMsg) stk.pop());

	    if (logger != null)	
		evt.addMessage (m);
	    return b.length;
	} catch (ISOException e) {
	    evt.addMessage (e);
	    throw e;
	} catch (IOException e) {
	    evt.addMessage (e);
	    throw new ISOException (e.toString());
	} catch (SAXException e) {
	    evt.addMessage (e);
	    throw new ISOException (e.toString());
	} finally {
	    Logger.log (evt);
	}
    }

    public void startElement (String name, AttributeList atts)
        throws SAXException
    {
	int fieldNumber = -1;
	try {
	    String id       = atts.getValue(ID_ATTR);
	    if (id != null)
		fieldNumber = Integer.parseInt (id);
	    if (name.equals (ISOMSG_TAG)) {
		if (fieldNumber >= 0) {
		    if (stk.empty())
			throw new SAXException ("inner without outter");

		    ISOMsg inner = new ISOMsg(fieldNumber);
		    ((ISOMsg)stk.peek()).set (inner);
		    stk.push (inner);
		} else {
		    stk.push (new ISOMsg(0));
		}
	    } else if (name.equals (ISOFIELD_TAG)) {
		ISOMsg m     = (ISOMsg) stk.peek();
		String value = atts.getValue(VALUE_ATTR);
		String type  = atts.getValue(TYPE_ATTR);
		if (id == null || value == null)
		    throw new SAXException ("invalid field");	
		if (TYPE_BINARY.equals (type)) {
		    m.set (new ISOBinaryField (
			fieldNumber, 
			    ISOUtil.hex2byte (
				value.getBytes(), 0, value.length()/2
			    )
			)
		    );
		}
		else
		    m.set (new ISOField (fieldNumber, value));
	    }
	} catch (ISOException e) {
	    throw new SAXException 
		("ISOException unpacking "+fieldNumber);
	}
    }
    public void endElement (String name) throws SAXException {
	if (name.equals (ISOMSG_TAG)) {
	    ISOMsg m = (ISOMsg) stk.pop();
	    if (stk.empty())
		stk.push (m); // push outter message
	}
    }

    public String getFieldDescription(ISOComponent m, int fldNumber) {
        return "<notavailable/>";
    }
    public void setLogger (Logger logger, String realm) {
	this.logger = logger;
	this.realm  = realm;
    }
    public String getRealm () {
	return realm;
    }
    public Logger getLogger() {
	return logger;
    }
}

