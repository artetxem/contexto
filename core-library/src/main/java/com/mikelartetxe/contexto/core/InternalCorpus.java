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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;


class InternalCorpus {
    
    private static final Charset CHARSET = Charset.forName("utf-8");
    private final InternalDataReader reader;
    private final long id2ptr[];
    
    private InternalCorpus(InternalDataReader reader) {
        this.reader = reader;
        id2ptr = new long[reader.readCompressedInt()];
        for (int i = 0; i < id2ptr.length; i++) {
            id2ptr[i] = reader.readLong();
        }
    }
    
    private byte[] getBytes(long fromPtr, long toPtr) {
        return reader.seek(fromPtr).readBytes((int)(toPtr-fromPtr));
    }
    
    public byte[] getSentenceBytes(int id) {
        return getBytes(id2ptr[id], id2ptr[id+1]);
    }
    
    public byte[] getLeftContextBytes(int sentenceId, int boundary) {
        return getBytes(id2ptr[sentenceId], id2ptr[sentenceId] + boundary);
    }
    
    public byte[] getRightContextBytes(int sentenceId, int boundary) {
        return getBytes(id2ptr[sentenceId] + boundary, id2ptr[sentenceId+1]);
    }
    
    public byte[] getPhraseBytes(int sentenceId, int leftBoundary, int rightBoundary) {
        return getBytes(id2ptr[sentenceId] + leftBoundary, id2ptr[sentenceId] + rightBoundary);
    }
    
    public String getSentence(int id) {
        return bytes2string(getSentenceBytes(id));
    }
    
    public String getLeftContext(int sentenceId, int boundary) {
        return bytes2string(getLeftContextBytes(sentenceId, boundary));
    }
    
    public String getRightContext(int sentenceId, int boundary) {
        return bytes2string(getRightContextBytes(sentenceId, boundary));
    }
    
    public String getPhrase(int sentenceId, int leftBoundary, int rightBoundary) {
        return bytes2string(getPhraseBytes(sentenceId, leftBoundary, rightBoundary));
    }
    
    private static String bytes2string(byte b[]) {
        return new String(b, CHARSET);
    }
    
    public static long write(InputStream is, InternalDataWriter writer) throws IOException {
        // Write sentences
        final List<Long> pointers = new ArrayList<>();
        final BufferedReader in = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = in.readLine()) != null) {
            pointers.add(writer.writeBytes(line.getBytes(CHARSET)));
        }
        pointers.add(writer.writeByte(0));
        
        // Write sentence index/pointers
        final long ptr = writer.writeCompressedInt(pointers.size());
        for (long pointer : pointers) {
            writer.writeCompressedPointer(pointer);
        }
        return ptr;
    }
    
    public static InternalCorpus read(InternalDataReader reader) {
        return new InternalCorpus(reader);
    }
    
}
