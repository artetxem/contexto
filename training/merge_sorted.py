# Copyright (C) 2017  Mikel Artetxe <artetxem@gmail.com>
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.

import argparse
import random
import sys


# The maximum number of examples to keep per translation
# If more examples are found in the input file the remaining will be discarded at random
MAX_EXAMPLES_PER_TRANSLATION = 10

# The minimum number of examples per translation
# Translations with less than this number of examples will be grouped in a special $OTHERS$ group
MIN_EXAMPLES_PER_TRANSLATION = 3

# The maximum number of translation candidates that a phrase can have
# If more translation candidates are found in the input file the less frequent ones will be grouped in a special $OTHERS$ group
MAX_TRANSLATIONS_PER_PHRASE = 10


def process_entry(src, trglist, file=sys.stdout):
    if src is not None:
        # Count the number of examples for each translation
        trglist = [(len(examples), translation, examples) for translation, examples in trglist]

        # Sort translations by their number of examples
        trglist = sorted(trglist, reverse=True)

        # Count the total number of examples for the phrase
        total = sum([trg[0] for trg in trglist])

        # Process each translation independently
        translations = []
        others = []
        for (count, translation, examples) in trglist:
            if len(examples) >= MIN_EXAMPLES_PER_TRANSLATION and len(translations) < MAX_TRANSLATIONS_PER_PHRASE:
                random.shuffle(examples)
                translations.append((translation, count, examples[:MAX_EXAMPLES_PER_TRANSLATION]))
            else:
                others += examples

        # Skip if all the translations are grouped in the special $OTHERS$ group
        if len(translations) == 0:
            return

        # Add the special $OTHERS$ group
        if len(others) > 0:
            random.shuffle(others)
            translations.append(('$OTHERS$', len(others), others[:MAX_EXAMPLES_PER_TRANSLATION]))

        print(src, total, '\t'.join([trg + '\t' + str(count) + '\t' + ' '.join(examples) for (trg, count, examples) in translations]), sep='\t', file=file)


def main():
    # Parse command line arguments
    parser = argparse.ArgumentParser(description='Merge sorted alignments as required by the core library to build dictionary models')
    parser.add_argument('-i', '--input', default=sys.stdin.fileno(), help='the input file (defauts to stdin)')
    parser.add_argument('-o', '--output', default=sys.stdout.fileno(), help='the output file (defaults to stdout)')
    parser.add_argument('--encoding', default='utf-8', help='the character encoding for input/output (defaults to utf-8)')
    args = parser.parse_args()

    fin = open(args.input, encoding=args.encoding, errors='surrogateescape')
    fout = open(args.output, mode='w', encoding=args.encoding, errors='surrogateescape')

    prevsrc = None
    trglist = []
    for line in fin:
        src, trg, example = line.split('\t')
        example = example.strip()
        if src != prevsrc:
            process_entry(prevsrc, trglist, file=fout)
            prevsrc = src
            trglist = [(trg, [example])]
        elif trg != trglist[-1][0]:
            trglist.append((trg, [example]))
        else:
            trglist[-1][1].append(example)
    process_entry(prevsrc, trglist, file=fout)

    fin.close()
    fout.close()


if __name__ == '__main__':
    main()
