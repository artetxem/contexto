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
import java.util.Collections;
import java.util.List;


public class AutocompleteResponse {
    
    private List<String> suggestions;

    public List<String> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = Collections.unmodifiableList(new ArrayList<>(suggestions));
    }
    
    public static AutocompleteResponse fromAPI(List<String> suggestions) {
        final AutocompleteResponse response = new AutocompleteResponse();
        response.setSuggestions(suggestions);
        return response;
    }
    
}
