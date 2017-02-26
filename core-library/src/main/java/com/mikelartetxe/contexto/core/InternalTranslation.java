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

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

class InternalTranslation {
    
    // TODO Extract the string from the examples instead of saving it explicitly
    private final long occurrences;
    private final byte string[];
    private final InternalPhrasePairExample examples[];
    
    private InternalTranslation(String s) {
        final String fields[] = s.split("\t");
        final String exampleCodes[] = fields[2].split(" ");
        string = fields[0].getBytes(Charset.forName("utf-8")); // TODO Hardcoded charset
        occurrences = Long.parseLong(fields[1]);
        examples = new InternalPhrasePairExample[exampleCodes.length];
        for (int i = 0; i < examples.length; i++) {
            examples[i] = InternalPhrasePairExample.parse(exampleCodes[i]);
        }
    }
    
    private InternalTranslation(InternalDataReader data) {
        // Read weight
        occurrences = data.readCompressedLong();
        
        // Read string
        final int stringLength = data.readCompressedInt();
        string = new byte[stringLength];
        for (int i = 0; i < stringLength; i++) {
            string[i] = data.readByte();
        }
        
        // Read examples
        final int examplesLength = data.readCompressedInt();
        examples = new InternalPhrasePairExample[examplesLength];
        for (int i = 0; i < examplesLength; i++) {
            examples[i] = data.readExample();
        }
    }
        
    public long write(InternalDataWriter data) throws IOException {
        // Write weight
        final long pointer = data.writeCompressedLong(occurrences);
        
        // Write string
        data.writeCompressedInt(string.length);
        for (byte b : string) data.writeByte(b);
        
        // Write examples
        data.writeCompressedInt(examples.length);
        for (final InternalPhrasePairExample e : examples) data.writePhrasePairExample(e);
        
        // Return the pointer to the first written byte
        return pointer;
    }
    
    public String getString() {
        return new String(string, Charset.forName("utf-8")); // TODO Hardcoded charset
    }
    
    public long getNumberOfOccurrences() {
        return occurrences;
    }
    
    public List<InternalPhrasePairExample> getExamples() {
        return Arrays.asList(examples);
    }
    
    public static InternalTranslation read(InternalDataReader data) {
        return new InternalTranslation(data);
    }
    
    public static InternalTranslation parse(String s) {
        return new InternalTranslation(s);
    }
    
}
