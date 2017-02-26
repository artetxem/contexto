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


class InternalPhrase {
    
    // TODO Extract the string from the examples instead of saving it explicitly
    private final long weight;
    private final byte string[];
    private final InternalTranslation translations[];
    
    private InternalPhrase(InternalDataReader data) {
        // Read weight
        weight = data.readCompressedLong();
        
        // Read string
        final int stringLength = data.readCompressedInt();
        string = new byte[stringLength];
        for (int i = 0; i < stringLength; i++) {
            string[i] = data.readByte();
        }
        
        // Read translations
        final int translationsLength = data.readCompressedInt();
        translations = new InternalTranslation[translationsLength];
        for (int i = 0; i < translationsLength; i++) {
            translations[i] = data.readTranslation();
        }
        
    }
    
    private InternalPhrase(String s) {
        final String fields[] = s.split("\t");
        string = fields[0].getBytes(Charset.forName("utf-8")); // TODO Hardcoded charset
        weight = Long.parseLong(fields[1]);
        translations = new InternalTranslation[(fields.length-2)/3];
        for (int i = 0; i < translations.length; i++) {
            translations[i] = InternalTranslation.parse(
                    fields[i*3+2] + "\t" + fields[i*3+3] + "\t" + fields[i*3+4]);
        }
    }
    
    public String getString() {
        return new String(string, Charset.forName("utf-8")); // TODO Hardcoded charset
    }
    
    public long getWeight() {
        return weight;
    }
    
    public List<InternalTranslation> getTranslations() {
        return Arrays.asList(translations);
    }

    @Override
    public String toString() {
        return getString() + " (" + getWeight() + ")";
    }
    
    public long write(InternalDataWriter data) throws IOException {        
        // Write weight
        final long pointer = data.writeCompressedLong(weight);
        
        // Write string
        data.writeCompressedInt(string.length);
        for (byte b : string) data.writeByte(b);
        
        // Write translations
        data.writeCompressedInt(translations.length);
        for (final InternalTranslation t : translations) data.writeTranslation(t);
                
        // Return the pointer to the first written byte
        return pointer;
    }
    
    public static InternalPhrase read(InternalDataReader data) {
        return new InternalPhrase(data);
    }
    
    public static InternalPhrase parse(String s) {
        return new InternalPhrase(s);
    }
    
}
