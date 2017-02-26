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

import java.nio.ByteBuffer;


// TODO Add support for >2GB files
class InternalDataReader {
    
    private final ByteBuffer buffer;
    private long ptr;
    
    public InternalDataReader(ByteBuffer buffer) {
        this.buffer = buffer;
        ptr = 0;
    }
    
    private InternalDataReader(ByteBuffer buffer, long ptr) {
        this.buffer = buffer;
        this.ptr = ptr;
    }
    
    public InternalDataReader seek(long ptr) {
        return new InternalDataReader(buffer, ptr);
    }
    
    public byte readByte() {
        final byte res = buffer.get((int)ptr);
        ptr += 1;
        return res;
    }
    
    public byte[] readBytes(int count) {
        final byte res[] = new byte[count];
        for (int i = 0; i < count; i++) res[i] = readByte();
        return res;
    }
    
    public int readUnsignedByte() {
        final byte b = buffer.get((int)ptr);
        final int res = b & 0xFF;
        ptr += 1;
        return res;
    }
    
    public int readInt() {
        final int res = buffer.getInt((int)ptr);
        ptr += 4;
        return res;
    }
    
    public long readLong() {
        final long res = buffer.getLong((int)ptr);
        ptr += 8;
        return res;
    }
    
    public long readPointer() {
        return readLong();
    }
    
    // TODO Implement compression
    public int readCompressedInt() {
        return readInt();
    }
    
    // TODO Implement compression
    public long readCompressedLong() {
        return readLong();
    }
    
    // TODO Implement compression
    public long readCompressedPointer() {
        return readLong();
    }
    
    public InternalNode readNode() {
        return InternalNode.read(this);
    }
    
    public byte readFirstByteFromNode() {
        return InternalNode.readFirstByte(this);
    }
    
    public InternalPhrase readPhrase() {
        return InternalPhrase.read(this);
    }
    
    public InternalTranslation readTranslation() {
        return InternalTranslation.read(this);
    }
    
    public InternalPhrasePairExample readExample() {
        return InternalPhrasePairExample.read(this);
    }
    
    public InternalCorpus readCorpus() {
        return InternalCorpus.read(this);
    }
    
}
