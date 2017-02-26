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

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class DictionaryBuilderMain {
    
    public static void main(String args[]) throws IOException {
        if (args.length != 2) {
            System.err.println("USAGE: java -jar core-library.jar src.txt trg.txt");
            System.exit(-1);
        }
        final OutputStream os = new BufferedOutputStream(System.out);
        ContextDictionary.buildContextDictionary(System.in,
                new FileInputStream(args[0]), new FileInputStream(args[1]), os);
        os.close();
    }
    
}
