##################################
#
#
##################################

use strict; 

package SRL::syntree; 

# a tree node links to a phrase (for non-terminals) or a word (for terminals)
use SRL::word; 
use SRL::phrase; 

=head1 NAME

SRL::syntree - a syntactic tree


=head1 SYNOPSIS

 use SRL::syntree; 
 open F, "mytrees.mrg";
 $tree = SRL::syntree->read_from_stream(\*F);
 $tree->pretty_print();

=head1 DESCRIPTION

Data structure for a syntactic tree;

=head1 METHODS

=over 4

=cut


########################################


=item $tree = SRL::syntree->new() 

Creates a new syntactic tree, empty

=cut

sub new {
    my ($pkg) = @_; 

    my $t = []; 

    # pointer to root
    $t->[0] = undef; 
    # pointer to terminals (leaves)
    @{$t->[1]} = ();

    return bless $t, $pkg; 
}

########################################

sub DESTROY {
    my $t = shift; 
#    warn "SRL::syntree : Destroying tree $t"; 

    $t->root->destroy_node();
}


########################################

=item $tree = SRL->read_wsj_mrg($fh) 

Creates a new syntactic tree, read from the filehandle $fh, in "WSJ mrg" format.

=cut

sub read_wsj_mrg {
    my ($pkg, $fh) = @_;

    my $tree = $pkg->new; 
    bless $tree, $pkg; 

    my @N;  ## open nodes
    my $complete = 0; 

    while ( !$complete and !eof($fh)) {

	my $line = <$fh>; 
	chomp($line); 
	$line =~ s/\s+$//;
	
	while ($line ne "") {	
	    $line =~ s/^\s+//;

	    if ($line =~ /^\(([^ ()]+) ([^ ()]+)\)/) {
		## Terminal 
		my $tag = $1; 
		my $word = $2; 
		$line = $';   #'

		my $w = SRL::word->new(scalar(@{$tree->[1]}), $word, $tag);
		my $n = SRL::synnode->new_terminal($w); 
		push @{$tree->[1]}, $n;
		
		if (@N) {
		    $N[$#N]->add_sons($n); 
		    $n->set_dad($N[$#N]); 
		}
		else {
		    die "syntree->read_wsj_mrg: found terminal without non-terminal parent!\n"; 
		}
	    }
	    elsif ($line =~ /^\(([^ ()]+)?/) {
		## Start of Non-Terminal 
#	    print "branch 2\n";
		$line = $';     #'
		my $type = ($1) ? $1 : undef; 
		my $p = SRL::phrase->new(scalar(@{$tree->[1]}), undef, $type);
		my $n = SRL::synnode->new_non_terminal($p); 
		if (@N) {
		    $N[$#N]->add_sons($n); 
		    $n->set_dad($N[$#N]); 
		}		
		else {
		    defined($tree->[0]) and die "syntree->read_wsj_mrg: found a root node when root was already defined!\n"; 
		    $tree->[0] = $n; 
		}
		push @N, $n; 
	    }
	    elsif ($line =~ /^\)/) {
		## End of Non-Terminal 
#	        print "branch 3\n";
		$line = $';  #'
		my $n = pop @N; 
		$n->content->set_end(scalar(@{$tree->[1]})-1); 
		!@N and $complete = 1; 
	    }
	    elsif ($line !~ /^\s*$/) {
		die "syntree->read_wsj_mrg: Unknown token in line : $line\n"; 
	    }
	}
	
    }

    if ($complete) {
	return $tree; 
    }
    else {
	return undef; 
    }  
}


########################################

=item $tree->load_SE_tagging($W, @tags)

 $W is a reference to the list of words (SRL::word objects)
 @tags are the Start-End tags associated to each word.

=cut

sub load_SE_tagging {
    my ($tree, $W, @tags) = @_;

    # $W is a ref to the sequence of words

    my (@N);          # open nodes
    my $wid = 0;


    # Create artificial root if necessary 
    if (($tags[0] !~ /^\(/) or ($tags[$#tags] !~ /\)$/)) {
    }

    while (@tags) {
	my $tag = shift @tags;

	# Open non-terminals at $wid-th word
	while ($tag !~ /^\*/) {
	    # In the RegExp below, note that "\*" is permitted to be part of the tag
	    $tag =~ /^\(((\\\*|[^*(])+)/ or die "syntree->load_SE_tagging: opening nodes -- bad format in $tag at $wid-th position!\n";
	    my $type = ($1) ? $1 : undef; 
	    $tag = $';           #'

	    my $p = SRL::phrase->new($wid, undef, $type);
	    my $n = SRL::synnode->new_non_terminal($p); 
	    if (@N) {
		$N[$#N]->add_sons($n); 
		$n->set_dad($N[$#N]); 
	    }		
	    elsif (!defined($tree->[0])) {
		$tree->[0] = $n; 
	    }
	    else {
		my $root = SRL::synnode->new_non_terminal(undef); 
		$root->add_sons($tree->[0],$n); 
		$tree->[0] = $root;		
		push @N, $root; 		
	    }
	    push @N, $n; 	    	    
	}
	
	# Create terminal node for the $wid-th word
	$tag =~ s/^\*//;
	my $n = SRL::synnode->new_terminal($W->[$wid]); 
	push @{$tree->[1]}, $n;
		
	if (@N) {
	    $N[$#N]->add_sons($n); 
	    $n->set_dad($N[$#N]); 
	}
	else {
	    my $root = SRL::synnode->new_non_terminal(undef); 
	    if (defined($tree->[0])) {
		$root->add_sons($tree->[0]); 
	    }
	    $tree->[0] = $root;
	    $root->add_sons($n); 
	    push @N, $root; 		
	}

	# Close non-terminals at $wid-th word
	while ($tag ne "") {
	    $tag =~ /^([^\)]*)\)/  or die "syntree->load_SE_tagging: closing nodes -- bad format in $tag at $wid-th position!\n";
	    my $type = $1;
	    $tag = $';             #'

	    my $n = pop @N or die "syntree->load_SE_tagging : unbalanced start-end tags!\n";
	    (!$type) or ($type eq $n->content->type) or die "syntree->load_SE_tagging: types in start-end tags do not match!\n";
	    $n->content->set_end($wid);	    	    
	}	
	$wid++;
    }

}


########################################

=item $root = $tree->root()

Returns the root node of the tree.

=cut


sub root {
    my $t = shift; 
    return $t->[0];
}

########################################

=item $tree->set_root($node)

Sets $node as the root node of the tree.

=cut


sub set_root {
    my ($t,$r) = @_; 
    $t->[0] = $r;
}


########################################

=item @T = $tree->terminals()

Returns the list of terminal nodes of the tree.

=cut


sub terminals {
    my $t = shift; 
    return @{$t->[1]};
}

########################################

=item @T = $tree->ref_terminals()

Returns a reference to the list of terminal nodes of the tree.

=cut


sub ref_terminals {
    my $t = shift; 
    return $t->[1];
}

########################################

=item $tree->add_terminals($t1, $t2, $t3, @newT)

Adds new terminal nodes to the tree. 

=cut


sub add_terminals {
    my $t = shift; 
    push @{$t->[1]}, @_;
}

########################################

=item $tree->set_terminals(@newT)

Initializes the list of terminal nodes of the tree to @newT. 

=cut

sub set_terminals {
    my $t = shift; 
    @{$t->[1]} = @_;
}

########################################

=item @N = $tree->dfs(@newT)

Returns the list of nodes of the tree in depth-first-search order (i.e., preorder)

=cut

sub dfs {
    my $t = shift; 
    return $t->[0]->dfs; 
}


########################################

=item $s = $tree->to_string()

Generates a string that represents the tree in "WSJ mrg" format, with no linebreaks.

=cut

sub to_string {
    my $t = shift; 
    return $t->[0]->to_string;
}

########################################

=item $s = $tree->to_pretty_string()

Generates a string that represents the tree in "WSJ mrg" format, with linebreaks

=cut

sub to_pretty_string {
    my $t = shift; 
    if ($t->[0]->is_terminal or ($t->[0]->content and $t->[0]->content->type)) {
	return $t->[0]->to_pretty_string("  ");
    }
    else {
	return "( " . join("\n  ", map { $_->to_pretty_string("    ") } $t->[0]->sons ), ")"; 
    }
}

########################################

=item @SEtags = $tree->to_SE_tagging()



=cut

sub to_SE_tagging {
    my $t = shift; 

    my @S; 
    my @E; 
    
    $t->[0]->to_SE_tagging(\@S, \@E); 

    my $l = scalar(@{$t->[1]}); 
        
    my (@tags, $i); 
    for ($i=0; $i<$l; $i++) {
#	$tags[$i] = sprintf("%15s*%-15s", $S[$i], $E[$i]); 
	$tags[$i] = sprintf("%s*%s", $S[$i], $E[$i]); 
    }

    return @tags; 
}


############################################################

package SRL::synnode; 

sub new_terminal {
    my ($pkg, $w) = @_;

    my $n = []; 
    
    # if terminal -> undef 
    # else -> list of sons
    $n->[0] = undef; 

    $n->[1] = $w; 

    # daddy
    $n->[2] = undef; 

    return bless $n, $pkg; 
}

sub new_non_terminal {
    my ($pkg, $p) = @_;

    my $n = []; 
    
    # if terminal -> undef 
    # else -> list of sons
    @{$n->[0]} = (); 

    $n->[1] = $p; 

    # daddy
    $n->[2] = undef; 

    return bless $n, $pkg; 
}

sub destroy_node {
    my $n = shift; 
    
    # Destroy sons if not terminal
    if (defined($n->[0])) {
	my $s; 
	foreach $s ( @{$n->[0]} ) {
	    $s->destroy_node;
	}
    }
    $n->[0] = undef;
    
    # undef the dad
    $n->[2] = undef;
}

sub is_terminal {
    my $n = shift; 
    return !defined($n->[0]); 
}

sub content {
    my $n = shift; 
    return $n->[1]; 
}

sub set_content {
    my $n = shift; 
    $n->[1] = shift; 
}


sub dad {
    my $n = shift; 
    return $n->[2]; 
}

sub set_dad {
    my $n = shift; 
    $n->[2] = shift; 
}

sub sons {
    my $n = shift; 
    return @{$n->[0]}; 
}

sub set_sons {
    my $n = shift; 
    @{$n->[0]} = @_; 
}

sub add_sons {
    my $n = shift; 
    push @{$n->[0]}, @_; 
}

sub dfs {
    my $n = shift; 
    if (defined($n->[0])) {
	return ($n, map { $_->dfs } @{$n->[0]}); 
    }
    else {
	return ($n); 
    }
}

sub to_string {
    my $n = shift; 
    if (defined($n->[0])) {
	my $str = "("; 
	if (defined($n->content)) {
	    $str .= $n->content->type; 
	}
	return $str . " " . join(" ", map { $_->to_string } @{$n->[0]}) . ")";  
    }
    else {
	return "(" . $n->content->pos . " " . $n->content->form . ")"; 
    }
}


sub to_pretty_string {
    my $n = shift; 
    my $iniline = shift; 

    if ($n->is_terminal) {
	return "(" . $n->content->pos . " " . $n->content->form . ") "; 
    }
    else {
	my $str; 
	if (defined($n->content)) {
	    $str = "(". $n->content->type . " "; 
	}
	else {
	    $str = "( "; 
	}	
	my $son; 
	my $sep = ""; 
	foreach $son ( @{$n->[0]} ) {
	    if ($son->is_terminal) {
		$str .= $sep . $son->to_pretty_string();
		$sep = ""; 
	    }
	    else {
		$str .= "\n" . $iniline . $son->to_pretty_string($iniline."  "); 
		$sep = "\n" . $iniline; 		
	    }
	}
	$str .= ")"; 
	return $str; 
    }
}


sub to_SE_tagging {
    my ($n, $S, $E) = @_;

    if (!$n->is_terminal) {
	my $p = $n->content; 
		
	defined($p) and $S->[$p->start] .= "(".$p->type; 
	
	map { $_->to_SE_tagging($S,$E) } @{$n->[0]};

	defined($p) and $E->[$p->end] .= ")"; #$p->type.")"; 	
    }
}


=head1 SEE ALSO

Documentation of the SRL package for CoNLL-2005.
Check : 

  http://www.lsi.upc.edu/~srlconll

=head1 COPYRIGHT

Copyright 2004-2005 

Xavier Carreras and Lluís Màrquez

Technical University of Catalonia (UPC)

This software is free for research and educational purposes. 

Published work containing results derived from use of this software
must contain an appropriate acknowledgement.

=cut


1;





1;












