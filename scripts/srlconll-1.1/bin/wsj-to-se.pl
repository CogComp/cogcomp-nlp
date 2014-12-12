#! /usr/bin/perl 

use strict; 
use SRL::syntree; 

my $help = << "end_of_help;";
Usage:   WSJ trees > wsj-to-se.pl [options] > trees in Start-End
Options: 
     -w [0|1]     Output a column of words (default 0).
     -p [0|1]     Output a column of PoS tags (default 1).

end_of_help;



my $words = 1; 
my $pos = 1; 

while (@ARGV) {
    my $a = shift @ARGV; 

    if ($a eq "-w") {
	$words = shift @ARGV; 
    }
    elsif ($a eq "-p") {
	$pos = shift @ARGV; 
    }
    else {
	

    }
}


my $st = SRL::syntree->read_wsj_mrg(\*STDIN); 

while ( defined $st ) {
#    my @tags = $st->to_SE_tagging; 
#    print join("\n", @tags), "\n\n"; 

    my ($n,@S,@E,@W,@P); 
    foreach $n ( $st->dfs) {
	if ($n->is_terminal) {
	    my $w = $n->content; 
	    $W[$w->id] = $w->form; 

	    my $pos = $w->pos; 
	    $pos =~ s/^\-LRB\-$/(/;
	    $pos =~ s/^\-RRB\-$/)/;
	    $P[$w->id] = $pos;
	}
	else {
	    my $p = $n->content; 	    
	    if ($p->type) {
		$S[$p->start] .= "(".$p->type;
		$E[$p->end] = ")".$E[$p->end]; 
#	        $E[$p->end] = $p->type.")".$E[$p->end];
	    }
	}
    }

    my $i; 
    for ($i=0; $i<@W; $i++) {
	if ($words) {
	    
	    $W[$i] =~ s/-LRB-/(/g;
	    $W[$i] =~ s/-RRB-/)/g;
	    $W[$i] =~ s/-LCB-/{/g;
	    $W[$i] =~ s/-RCB-/}/g;
 
	    printf("%-30s  ", $W[$i]); 
	}
	if ($pos) {

	    $P[$i] =~ s/-LRB-/(/g;
	    $P[$i] =~ s/-RRB-/)/g;

	    printf("%-6s  ", $P[$i]); 
	}
	printf("%25s*%-25s\n", $S[$i], $E[$i]); 
    }
    print "\n"; 

    $st = SRL::syntree->read_wsj_mrg(\*STDIN); 
}








