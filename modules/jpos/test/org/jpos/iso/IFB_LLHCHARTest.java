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

package org.jpos.iso;

import junit.framework.TestCase;

/**
 * @author joconnor
 */
public class IFB_LLHCHARTest extends TestCase
{
    public void testPack() throws Exception
    {
        ISOField field = new ISOField(12, "ABCDEFGHIJ");
        IFB_LLHCHAR packager = new IFB_LLHCHAR(20, "Should be ABCDEFGHIJ");
        TestUtils.assertEquals(new byte[] {0x0A, 0x41, 0x42, 0x43, 0x44,
                                            0x45, 0x46, 0x47, 0x48, 0x49, 0x4A},
            packager.pack(field));
    }

    public void testPackagerTooLong() throws Exception
    {
        try
        {
            new IFB_LLHCHAR(256, "Too long for this");
            fail("256 is too long and should have thrown an exception");
        } catch (Exception ignored)
        {
        }
    }

    public void testPackTooMuch() throws Exception
    {
        ISOField field = new ISOField(12, "ABCDEFGHIJ");
        IFB_LLHCHAR packager = new IFB_LLHCHAR(5, "Should be ABCDEFGHIJ");
        try
        {
            packager.pack(field);
            fail("field is too long and should have thrown an exception");
        } catch (Exception ignored)
        {
        }
    }

    public void testUnpack() throws Exception
    {
        byte[] raw = new byte[] {0x0A, 0x41, 0x42, 0x43, 0x44,
        0x45, 0x46, 0x47, 0x48, 0x49, 0x4A};
        IFB_LLHCHAR packager = new IFB_LLHCHAR(20, "Should be ABCDEFGHIJ");
        ISOField field = new ISOField(12);
        packager.unpack(field, raw, 0);
        assertEquals("ABCDEFGHIJ", (String) field.getValue());
    }

    public void testReversability() throws Exception
    {
        String origin = "Abc123:.-";
        ISOField f = new ISOField(12, origin);
        IFB_LLHCHAR packager = new IFB_LLHCHAR(10, "Should be Abc123:.-");

        ISOField unpack = new ISOField(12);
        packager.unpack(unpack, packager.pack(f), 0);
        assertEquals(origin, (String) unpack.getValue());
    }
}
