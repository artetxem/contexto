ConTexto
========

ConTexto is an open source contextual dictionary similar to [Linguee](http://www.linguee.com/). It includes tools to automatically build such dictionary models from parallel corpora, a Java core library to use them, and a user-friendly web interface built on top of it.


Project structure
-----------------

- **core-library**: A Java SE core library that provides a simple API to search contextual dictionaries and perform autocomplete queries.
- **training**: Scripts for training contextual dictionay models from parallel corpora.
- **web-app**: A Java EE web app built on top of the core-library, consisting of a RESTful web service and a web client based on Bootstrap.


Building
--------

The core library and the web app can be built using Maven:

```
cd core-library
mvn install
cd ../web-app
mvn install
cd ..
```

Note that the web-app project depends on the core library, so you will have to build the latter first. Alternatively, you can open both projects in Netbeans and use them from there.

In addition to that, the alignment script used for training (see below) makes use of [fast_align](https://github.com/clab/fast_align), which is not included in this repository. In order to download and compile it, you will need a modern C++ compiler and CMake. In addition to that, you can optionally install OpenMP, libtcmalloc (part of Google's perftools) and libsparsehash for better performance. You can run the following command to install them on Ubuntu:

```
sudo apt-get install libgoogle-perftools-dev libsparsehash-dev
```

Once you have installed all the dependencies, you can download and compile fast_align by running the following commands:

```
git clone https://github.com/clab/fast_align.git training/third-party/fast_align 
cd training/third-party/fast_align
mkdir build
cd build
cmake ..
make
cd ../../../..
```

Usage
-----

ConTexto dictionary models use a custom binary format and are self-contained. In order to build a dictionary model, you will need a plain text parallel corpus (`SRC.txt` and `TRG.txt`), its tokenized and lowercased counterpart (`SRC.tok.txt` and `TRG.tok.txt`), and its word alignment in the standard Pharaoh format (`SRC2TRG.align.txt`), and run the following command:

```
training/build-model SRC.txt TRG.txt SRC.tok.txt TRG.tok.txt SRC2TRG.align.txt > SRC2TRG.dict.bin
```

Note that the original and tokenized text should be exactly the same except for the casing of the former and the additional whitespaces of the latter. Among others, this means that the tokenized text should not escape special characters.

[OPUS](http://opus.lingfil.uu.se/) offers a large collection of open parallel corpora for many language pairs. If you only have a raw parallel corpus in plaintext (e.g. downloaded from OPUS) but not its tokenized counterpart or the word alignments, you can use the tools included with ConTexto to obtain them as detailed in the two subsections below.

Once you have built all your dictionary models, place them in one directory and deploy the web-app WAR under `web-app/target/web-app-1.0-SNAPSHOT.war` with Tomcat setting the `dictionary.models` system property to point to the directory in question. You can use a parameter like `-Ddictionary.models=/path/to/your/dictionary/models` for that purpose.


### Corpus preprocessing and tokenization

Instructions for preprocessing the corpus are different depending on whether it comes tokenized or not.

#### a) The original corpus is not tokenized (recommended)

Fix possible formatting issues:

```
cat CORPUS.raw.txt | training/preprocess > CORPUS.txt
```

Fix possible formatting issues, tokenize and lowercase the corpus:

```
cat CORPUS.raw.txt | training/preprocess | training/tokenize | training/lowercase > CORPUS.tok.txt
```

#### b) The original corpus is tokenized

Some corpora are only distributed in tokenized (and sometimes truecased) format. In this case, ConTexto offers tools to try to detokenize and detruecase it. However, it is not always possible to perfectly recover the original text, so prefer option a) if possible.

Fix possible formatting issues and lowercase the tokenized corpus:

```
cat CORPUS.raw.tok.txt | training/preprocess-tokenized | training/lowercase > CORPUS.tok.txt
```

Fix possible formatting issues, detruecase and detokenize the corpus:

```
cat CORPUS.raw.tok.txt | training/preprocess-tokenized | training/detokenize > CORPUS.txt
```

### Word alignment

You can run the following script to word align a tokenized parallel corpus.

```
training/align SRC.tok.txt TRG.tok.txt > SRC2TRG.align.txt
```

The script runs fast_align under the hood, so it is necessary to have it installed as detailed above.

To obtain the word alignments in the opposite direction, simply swap the arguments in the above call or, faster, invert the alignments with the following commmand:

```
cat SRC2TRG.align.txt | training/invert-alignment > TRG2SRC.align.txt
```


License
-------

Copyright (C) 2017, Mikel Artetxe

Licensed under the terms of the GNU General Public License, either version 2 or (at your option) any later version. A full copy of the license can be found in LICENSE.txt.

This project includes third party libraries; please see `training/third-party/` and `web-app/src/main/webapp/libs/` for copyright details pertaining to them.

