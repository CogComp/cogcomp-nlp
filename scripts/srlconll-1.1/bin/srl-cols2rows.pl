#! /usr/bin/perl





use strict; 
use SRL::sentence; 
use SRL::word; 
use SRL::phraseset; 


if (@ARGV) {
    print "Usage: paste -d ' ' sample.words sample.synt.upc sample.synt.cha sample.ne.cn sample.props | srl-cols2rows.pl\n\n"; 
    exit; 
}


# The following hash specifies the column number (starting at 0) of
# the input file that contains the annotation.
#
# The "words", "pos" and "props" columns are mandatory, the rest
# are optional (remove the hash entry to disable them, and comment
# the corresponding printing below)

my %COLUMNS = (  words   => 0,
		 pos     => 1,
		 chunks  => 2,
		 clauses => 3,
		 syntree => 5,
		 ne      => 6,
		 props   => 8
		 );


my $s = SRL::sentence->read_from_stream(0,\*STDIN, %COLUMNS); 

while ( $s ) {
    
    # words   
    print "W   ", join(" ", map { $_->form } $s->words ), "\n"; 
    
    # PoS 
    print "P   ", join(" ", map { $_->pos } $s->words ), "\n"; 
    
    # chunks 
    print "C   ", join(" ", map { p2s($_) } $s->chunking->phrases), "\n"; 

    # clauses 
    print "S   ", join(" ", map { p2s($_) } $s->clausing->phrases), "\n"; 

    # tree
    print "T   ", $s->syntree->to_string, "\n"; 

    # named entities
    print "N   ", join(" ", map { p2s($_) } $s->named_entities->phrases), "\n"; 

    # props
    my $p;
    foreach $p ( $s->gold_props ) {
	print "R   ", $p->verb, " ", $p->position, " ", 
	join(" ", map { p2s($_) } $p->phrases), "\n"; 
    }

    print "\n"; 


    
    $s = SRL::sentence->read_from_stream(0,\*STDIN, %COLUMNS); 
}

sub p2s {
    my $p = shift; 
    return "(".$p->start.",".$p->end.")_".$p->type;
}
