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

package com.mikelartetxe.contexto.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


public class ListDictionariesResponse {
    
    private List<String> dictionaries;

    public List<String> getDictionaries() {
        return dictionaries;
    }

    public void setDictionaries(List<String> dictionaries) {
        this.dictionaries = Collections.unmodifiableList(new ArrayList<>(dictionaries));
    }
    
    public static ListDictionariesResponse fromCollection(Collection<String> dictionaries) {
        final ListDictionariesResponse response = new ListDictionariesResponse();
        response.setDictionaries(new ArrayList<>(dictionaries));
        return response;
    }
    
}
