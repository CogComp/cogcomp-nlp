#!/bin/sh

mkdir cmd
mkdir lib
mkdir bin
mkdir doc
echo ''

if [ -r tree-tagger-linux-3.2.1.tar.gz ]
then
    gzip -cd tree-tagger-linux-3.2.1.tar.gz | tar -xf -
    echo 'Linux version of TreeTagger installed.'
fi

if [ -r tree-tagger-linux-3.2-old.tar.gz ]
then
    gzip -cd tree-tagger-linux-3.2-old.tar.gz | tar -xf -
    echo 'Linux version of TreeTagger (older kernels) installed.'
fi

if [ -r tree-tagger-MacOSX-3.2.tar.gz ]
then
    gzip -cd tree-tagger-MacOSX-3.2.tar.gz | tar -xf -
    echo 'Mac OS-X version of TreeTagger for PowerPC installed.'
fi

if [ -r tree-tagger-MacOSX-3.2-intel.tar.gz ]
then
    gzip -cd tree-tagger-MacOSX-3.2-intel.tar.gz | tar -xf -
    echo 'Mac OS-X version of TreeTagger for Intel CPUs installed.'
fi

if [ -r tagger-scripts.tar.gz ] 
then
    gzip -cd tagger-scripts.tar.gz | tar -xf -
    chmod +x cmd/*
    echo 'Tagging scripts installed.'
fi

if [ -r estonian-par-linux-3.2-utf8.bin.gz ] 
then
    gzip -cd estonian-par-linux-3.2-utf8.bin.gz > lib/estonian-utf8.par
    echo 'Estonian parameter file (Linux, UTF8) installed.'
fi

if [ -r finnish-par-linux-3.2-utf8.bin.gz ] 
then
    gzip -cd finnish-par-linux-3.2-utf8.bin.gz > lib/finnish-utf8.par
    echo 'Finnish parameter file (Linux, UTF8) installed.'
fi

if [ -r german-par-linux-3.2-utf8.bin.gz ]
then
    gzip -cd german-par-linux-3.2-utf8.bin.gz > lib/german-utf8.par
    echo 'German parameter file (Linux, UTF8) installed.'
fi

if [ -r german-chunker-par-linux-3.2-utf8.bin.gz ] 
then
    gzip -cd german-chunker-par-linux-3.2-utf8.bin.gz > lib/german-chunker-utf8.par
    echo 'German chunker parameter file (Linux, UTF8) installed.'
fi

if [ -r english-par-linux-3.2-utf8.bin.gz ]
then
    gzip -cd english-par-linux-3.2-utf8.bin.gz > lib/english-utf8.par
    echo 'English parameter file (Linux, UTF8) installed.'
fi

if [ -r english-chunker-par-linux-3.2-utf8.bin.gz ] 
then
    gzip -cd english-chunker-par-linux-3.2-utf8.bin.gz > lib/english-chunker-utf8.par
    echo 'English chunker parameter file (Linux, UTF8) installed.'
fi

if [ -r spanish-chunker-par-linux-3.2-utf8.bin.gz ] 
then
    gzip -cd spanish-chunker-par-linux-3.2-utf8.bin.gz > lib/spanish-chunker-utf8.par
    echo 'Spanish chunker parameter file (Linux, UTF8) installed.'
fi

if [ -r french-par-linux-3.2-utf8.bin.gz ]
then
    gzip -cd french-par-linux-3.2-utf8.bin.gz > lib/french-utf8.par
    echo 'French parameter file (Linux, UTF8) installed.'
fi

if [ -r french-chunker-par-linux-3.2-utf8.bin.gz ] 
then
    gzip -cd french-chunker-par-linux-3.2-utf8.bin.gz > lib/french-chunker-utf8.par
    echo 'French chunker parameter file (Linux, UTF8) installed.'
fi

if [ -r romanian-par-linux-3.2-utf8.bin.gz ]
then
    gzip -cd romanian-par-linux-3.2-utf8.bin.gz > lib/romanian-utf8.par
    echo 'Romanian parameter file (Linux, UTF8) installed.'
fi

if [ -r italian-par-linux-3.2-utf8.bin.gz ]
then
    gzip -cd italian-par-linux-3.2-utf8.bin.gz > lib/italian-utf8.par
    echo 'Italian parameter file (Linux, UTF8) installed.'
fi

if [ -r italian-par2-linux-3.2-utf8.bin.gz ]
then
    gzip -cd italian-par2-linux-3.2-utf8.bin.gz > lib/italian.par
    echo 'alternative Italian parameter file (Linux) installed.'
fi

if [ -r bulgarian-par-linux-3.2-utf8.bin.gz ]
then
    gzip -cd bulgarian-par-linux-3.2-utf8.bin.gz > lib/bulgarian-utf8.par
    echo 'Bulgarian parameter file (Linux, UTF8) installed.'
fi

if [ -r polish-par-linux-3.2-utf8.bin.gz ]
then
    gzip -cd polish-par-linux-3.2-utf8.bin.gz > lib/polish-utf8.par
    echo 'Polish parameter file (Linux, UTF8) installed.'
fi

if [ -r portuguese-par-linux-3.2-utf8.bin.gz ]
then
    gzip -cd portuguese-par-linux-3.2-utf8.bin.gz > lib/portuguese-utf8.par
    echo 'Portuguese parameter file (Linux, UTF8) installed.'
fi

if [ -r portuguese-finegrained-par-linux-3.2-utf8.bin.gz ]
then
    gzip -cd portuguese-finegrained-par-linux-3.2-utf8.bin.gz > lib/portuguese-finegrained-utf8.par
    echo 'Portuguese parameter file (Linux, UTF8) with fine-grained tagset installed.'
fi

if [ -r russian-par-linux-3.2-utf8.bin.gz ]
then
    gzip -cd russian-par-linux-3.2-utf8.bin.gz > lib/russian-utf8.par
    echo 'Russian parameter file (Linux, UTF8) installed.'
fi

if [ -r spanish-par-linux-3.2-utf8.bin.gz ]
then
    gzip -cd spanish-par-linux-3.2-utf8.bin.gz > lib/spanish-utf8.par
    echo 'Spanish parameter file (Linux, UTF8) installed.'
fi

if [ -r galician-par-linux-3.2.bin.gz ]
then
    gzip -cd galician-par-linux-3.2.bin.gz > lib/galician-utf8.par
    echo 'Galician parameter file (Linux, UTF8) installed.'
fi

if [ -r dutch-par-linux-3.2-utf8.bin.gz ]
then
    gzip -cd dutch-par-linux-3.2-utf8.bin.gz > lib/dutch-utf8.par
    echo 'Dutch parameter file (Linux, UTF8) installed.'
fi

if [ -r dutch2-par-linux-3.2-utf8.bin.gz ]
then
    gzip -cd dutch2-par-linux-3.2-utf8.bin.gz > lib/dutch-utf8.par
    echo 'Dutch parameter file (Linux, UTF8) installed.'
fi

if [ -r swahili-par-linux-3.2.bin.gz ]
then
    gzip -cd swahili-par-linux-3.2.bin.gz > lib/swahili.par
    echo 'Swahili parameter file (Linux) installed.'
fi

if [ -r slovak-par-linux-3.2-utf8.bin.gz ]
then
    gzip -cd slovak-par-linux-3.2-utf8.bin.gz > lib/slovak-utf8.par
    echo 'Slovak parameter file (Linux, UTF8) installed.'
fi

if [ -r slovak2-par-linux-3.2-utf8.bin.gz ]
then
    gzip -cd slovak2-par-linux-3.2-utf8.bin.gz > lib/slovak-utf8.par
    echo 'Slovak parameter file (Linux, UTF8, full tagset) installed.'
fi

if [ -r slovenian-par-linux-3.2-utf8.bin.gz ]
then
    gzip -cd slovenian-par-linux-3.2-utf8.bin.gz > lib/slovenian-utf8.par
    echo 'Slovenian parameter file (Linux, UTF8) installed.'
fi

if [ -r latin-par-linux-3.2.bin.gz ]
then
    gzip -cd latin-par-linux-3.2.bin.gz > lib/latin.par
    echo 'Latin parameter file (Linux) installed.'
fi

if [ -r latinIT-par-linux-3.2.bin.gz ]
then
    gzip -cd latinIT-par-linux-3.2.bin.gz > lib/latin.par
    echo 'Latin Index Thomisticus parameter file (Linux) installed.'
fi

for file in cmd/*
do
    awk '$0=="BIN=./bin"{print "BIN='`pwd`'/bin";next}\
         $0=="CMD=./cmd"{print "CMD='`pwd`'/cmd";next}\
         $0=="LIB=./lib"{print "LIB='`pwd`'/lib";next}\
         {print}' $file > $file.tmp;
    mv $file.tmp $file;
done
echo 'Path variables modified in tagging scripts.'

chmod 0755 cmd/*

echo ''
echo 'You might want to add '`pwd`'/cmd and '`pwd`'/bin to the PATH variable so that you do not need to specify the full path to run the tagging scripts.'
echo ''
