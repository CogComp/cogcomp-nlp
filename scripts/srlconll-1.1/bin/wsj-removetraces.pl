#! /usr/bin/perl 

use SRL::syntree; 


my $st = SRL::syntree->read_wsj_mrg(\*STDIN); 
my $nt = 1; 
while ( defined $st ) {

    my $nt = remove_traces($st); 

    print $nt->to_pretty_string, "\n"; 

    $st = SRL::syntree->read_wsj_mrg(\*STDIN); 
    $nt++;
}


sub remove_traces {
    my $st = shift; 

    my $t; 
    my @T  = $st->terminals; 
    foreach $t (@T) {
	if ($t->content->pos eq "-NONE-") {
	    my $n = $t->dad; 
	    my $pos = $t->content->id;
	    $t->set_content(undef); 
	    while (defined($n)) {
		if (($n->content->start == $pos) and ($n->content->end == $pos)) {
		    $n->set_content(undef); 
		    $n = $n->dad; 
		}
		elsif ($n->content->end==$pos) {
		    my $i = $pos-1;
		    while ($n->content->start<=$i and (!defined($T[$i]->content))) {   #  or $T[$i]->content->pos eq "-NONE-")) {
			$i--;
		    }
		    if ($n->content->start>$i) {
			$n->set_content(undef); 
			$n = $n->dad; 			
		    }
		    else {
			$n = undef; 
		    }
		}       
		else {
		    $n = undef; 
		}
	    }
	}	
    }
    
    clean_null_childs($st->root); 
    return $st; 

}

sub clean_null_childs {
    my $n = shift; 

    if (!$n->is_terminal) {
	my @S = grep { defined($_->content) } $n->sons; 
	$n->set_sons(@S); 
	map { clean_null_childs($_) } @S; 
    }
    
}





