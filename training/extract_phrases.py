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
import sys


ENCODING = 'utf-8'
ENCODING_ERRORS = 'surrogateescape'
MAX_PHRASE_LEN = 5
UNALIGNED_MAX = -1
UNALIGNED_MIN = 1000000000


def is_punctuation(s):
    return not any([c.isalnum() for c in s])


# TODO Better error management (mismatching lengths...)
def tokens2boundaries(tokens, s):
    boundaries = []
    charpos = 0
    bytepos = 0
    for token in tokens:
        # Skip whitespaces
        while s[charpos].isspace():
            bytepos += len(s[charpos].encode(encoding=ENCODING, errors=ENCODING_ERRORS))
            charpos += 1

        # Check that the token is matched
        start = bytepos
        for i in range(len(token)):
            if token[i].lower() != s[charpos].lower():
                return None
            bytepos += len(s[charpos].encode(encoding=ENCODING, errors=ENCODING_ERRORS))
            charpos += 1
        end = bytepos

        # Add boundaries
        boundaries.append((start, end))

    return boundaries


def main():
    # Parse command line arguments
    parser = argparse.ArgumentParser(description='Extract phrase pairs from a parallel corpus')
    parser.add_argument('src', help='the source language side of the parallel corpus')
    parser.add_argument('trg', help='the target language side of the parallel corpus')
    parser.add_argument('src_tok', help='the tokenized source language side of the parallel corpus')
    parser.add_argument('trg_tok', help='the tokenized target language side of the parallel corpus')
    parser.add_argument('align', help='the word alignments in the standard Pharaoh format')
    parser.add_argument('-o', '--output', default=sys.stdout.fileno(), help='the output file (defaults to stdout)')
    parser.add_argument('-r', '--reverse', action='store_true', help='invert alignments')
    args = parser.parse_args()

    srcfile = open(args.src, encoding=ENCODING, errors=ENCODING_ERRORS)
    trgfile = open(args.trg, encoding=ENCODING, errors=ENCODING_ERRORS)
    srctokfile = open(args.src_tok, encoding=ENCODING, errors=ENCODING_ERRORS)
    trgtokfile = open(args.trg_tok, encoding=ENCODING, errors=ENCODING_ERRORS)
    alignfile = open(args.align, encoding=ENCODING, errors=ENCODING_ERRORS)
    out = open(args.output, mode='w', encoding=ENCODING, errors=ENCODING_ERRORS)

    for nseg, (srcline, trgline, srctokline, trgtokline, alignline) in enumerate(zip(srcfile, trgfile, srctokfile, trgtokfile, alignfile)):
        src = srctokline.split()
        trg = trgtokline.split()
        align = [[int(x) for x in link.split('-')] for link in alignline.split()]
        if args.reverse:
            align = [list(reversed(link)) for link in align]

        # Find token boundaries
        src2boundaries = tokens2boundaries(src, srcline)
        trg2boundaries = tokens2boundaries(trg, trgline)
        if src2boundaries == None:
            print('WARNING: Tokenization mismatch in source language (line {0})'.format(nseg + 1), file=sys.stderr)
            print('\t' + srcline.strip(), file=sys.stderr)
            print('\t' + srctokline.strip(), file=sys.stderr)
            print(file=sys.stderr)
            continue
        if trg2boundaries == None:
            print('WARNING: Tokenization mismatch in target language (line {0})'.format(nseg + 1), file=sys.stderr)
            print('\t' + trgline.strip(), file=sys.stderr)
            print('\t' + trgtokline.strip(), file=sys.stderr)
            print(file=sys.stderr)
            continue

        # Get maximum/minimum alignments in both directions
        src2trgmax = [UNALIGNED_MAX]*len(src)
        src2trgmin = [UNALIGNED_MIN]*len(src)
        trg2srcmax = [UNALIGNED_MAX]*len(trg)
        trg2srcmin = [UNALIGNED_MIN]*len(trg)
        for link in align:
            src2trgmax[link[0]] = max(src2trgmax[link[0]], link[1])
            src2trgmin[link[0]] = min(src2trgmin[link[0]], link[1])
            trg2srcmax[link[1]] = max(trg2srcmax[link[1]], link[0])
            trg2srcmax[link[1]] = min(trg2srcmax[link[1]], link[0])

        # Check punctuation tokens
        srcpunct = [is_punctuation(s) for s in src]
        trgpunct = [is_punctuation(s) for s in trg]

        # Source phrase goes from i to j (both inclusive)
        for i in range(len(src)):
            for j in range(i, min(i + MAX_PHRASE_LEN, len(src))):

                # Skip if boundary entries are unaligned
                if src2trgmin[i] == UNALIGNED_MIN or src2trgmin[j] == UNALIGNED_MIN:
                    continue

                # Skip if the source phrase contains punctuation tokens
                if any(srcpunct[i:(j+1)]):
                    continue

                # Find aligned phrase
                mintrg = UNALIGNED_MIN
                maxtrg = UNALIGNED_MAX
                for k in range(i, j + 1):
                    mintrg = min(mintrg, src2trgmin[k])
                    maxtrg = max(maxtrg, src2trgmax[k])

                # Skip if the aligned phrase is bigger than the limit
                alignment_length = maxtrg - mintrg + 1
                if alignment_length > MAX_PHRASE_LEN:
                    continue

                # Skip if the aligned phrase contains punctuation tokens
                if any(trgpunct[mintrg:(maxtrg+1)]):
                    continue

                # Skip if there are backward alignments outside the source phrase
                minsrc = UNALIGNED_MIN
                maxsrc = UNALIGNED_MAX
                for k in range(mintrg, maxtrg + 1):
                    minsrc = min(minsrc, trg2srcmin[k])
                    maxsrc = max(maxsrc, trg2srcmax[k])
                if minsrc < i or maxsrc > j:
                    continue

                # Print phrase pair
                boundaries = '{0}:{1}:{2}:{3}:{4}'.format(
                        nseg,
                        src2boundaries[i][0],
                        src2boundaries[j][1],
                        trg2boundaries[mintrg][0],
                        trg2boundaries[maxtrg][1])
                print(' '.join(src[i:(j+1)]), ' '.join(trg[mintrg:(maxtrg+1)]), boundaries, sep='\t', file=out)


if __name__ == '__main__':
    main()
