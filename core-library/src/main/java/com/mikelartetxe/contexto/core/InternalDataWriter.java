/*
 * Copyright (C) 2017  Mikel Artetxe <artetxem@gmail.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mikelartetxe.contexto.core;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;


class InternalDataWriter {
    
    private final DataOutputStream out;
    private long pos;
    
    public InternalDataWriter(final OutputStream os) {
        out = new DataOutputStream(os);
        pos = 0;
    }
    
    public long writeByte(byte b) throws IOException {
        out.writeByte(b);
        return pos++;
    }
    
    public long writeByte(int i) throws IOException {
        out.writeByte(i);
        return pos++;
    }
    
    public long writeBytes(byte[] b) throws IOException {
        out.write(b);
        pos += b.length;
        return pos - b.length;
    }
    
    public long writeInt(int i) throws IOException {
        out.writeInt(i);
        pos += 4;
        return pos - 4;
    }
    public long writeLong(long l) throws IOException {
        out.writeLong(l);
        pos += 8;
        return pos - 8;
    }
    
    public long writePointer(long p) throws IOException {
        return writeLong(p);
    }
    
    // TODO Implement compression
    public long writeCompressedInt(int i) throws IOException {
        return writeInt(i);
    }
    
    // TODO Implement compression
    public long writeCompressedLong(long l) throws IOException {
        return writeLong(l);
    }
    
    // TODO Implement compression
    public long writeCompressedPointer(long pointer) throws IOException {
        return writeLong(pointer);
    }
    
    public long writeNode(InternalNode node) throws IOException {
        return node.write(this);
    }
    
    public long writePhrase(InternalPhrase phrase) throws IOException {
        return phrase.write(this);
    }
    
    public long writeTranslation(InternalTranslation translation) throws IOException {
        return translation.write(this);
    }
    
    public long writePhrasePairExample(InternalPhrasePairExample example) throws IOException {
        return example.write(this);
    }
    
}
