################################################################################
#
#   Package    p h r a s e _ s e t 
#
#   A set of phrases
#   Each phrase is indexed by (start,end) positions
#
#   Holds non-overlapping phrase sets. 
#   Embedding of phrases allowed and exploited in class methods
#
#   Brings useful functions on phrase sets, such as: 
#     - Load phrases from tag sequences in IOB1, IOB2, Start-End formats
#     - Retrieve a phrase given its (start,end) positions
#     - List phrases found within a given (s,e) segment
#     - Discriminate a predicted set of phrases with respect to the gold set
#
################################################################################

use strict; 

use SRL::phrase; 

package SRL::phrase_set;

## $phrase_types global variable 
#  If defined, contains a hash table specifying the phrase types to be considered
#  If undefined, any phrase type is considered
my $phrase_types = undef; 
sub set_phrase_types {
    $phrase_types = {};
    my $t; 
    foreach $t ( @_ ) {
        $phrase_types->{$t} = 1; 
    } 
}

# Constructor: creates a new phrase set
# Arguments: an initial set of phrases, which are added to the set
sub new {
    my ($pkg, @P) = @_;
    my $s = [];
    @{$s->[0]} = ();     # NxN half-matrix, storing phrases
    $s->[1] = 0;         # N   (length of the sentence)
    bless $s, $pkg;

    $s->add_phrases(@P);

    return $s;
}


# Adds phrases represented in IOB2 tagging
# Receives a list of IOB2 tags (one per word in the sentence)
# Creates a phrase object for each phrase in the taggging 
#  and modifies the set so that the phrases are part of it
sub load_IOB2_tagging {
    my ($set, @tags) = @_;

    my $wid = 0;  # word id
    my $phrase = undef;  # current phrase 
    my $t;
    foreach $t (@tags) {
        if ($phrase and $t !~ /^I/) {
            $phrase->set_end($wid-1);
            $set->add_phrases($phrase);
	    $phrase = undef;
        }
        if ($t =~ /^B-/) {
            my $type = $';
            if (!defined($phrase_types) or $phrase_types->{$type}) {
		$phrase = SRL::phrase->new($wid);
		$phrase->set_type($type);
	    }
        }
        $wid++;
    }
    if ($phrase) {
        $phrase->set_end($wid-1);
        $set->add_phrases($phrase);
    }
}


# Adds phrases represented in IOB1 tagging
# Receives a list of IOB1 tags (one per word in the sentence)
# Creates a phrase object for each phrase in the taggging 
#  and modifies the set so that the phrases are part of it
sub load_IOB1_tagging {
    my ($set, @tags) = @_;

    my $wid = 0;  # word id
    my $phrase = undef;  # current phrase 
    my $t = shift @tags;
    while (defined($t)) {
	if ($t =~ /^[BI]-/) {
	    my $type = $';
            if (!defined($phrase_types) or $phrase_types->{$type}) {
		$phrase = SRL::phrase->new($wid);
		$phrase->set_type($type);
		my $tag = "I-".$type;
		$t = shift @tags;
		$wid++;
		while ($t eq $tag) {
		    $t = shift @tags;
		    $wid++;
		}
		$phrase->set_end($wid-1);
		$set->add_phrases($phrase);
	    }
	    else {
		$t = shift @tags;
		$wid++;
	    }
	}
	else {
	    $t = shift @tags;
	    $wid++;
	}
    }
}

# Adds phrases represented in Start-End tagging
# Receives a list of Start-End tags (one per word in the sentence)
# Creates a phrase object for each phrase in the taggging 
#  and modifies the set so that the phrases are part of it
sub load_SE_tagging {
    my ($set, @tags) = @_;

    my (@SP);          # started phrases
    my $wid = 0;
    my ($tag, $p); 
    foreach $tag ( @tags ) {
	while ($tag !~ /^\*/) {
	    $tag =~ /^\(((\\\*|[^*(])+)/ or die "phrase_set->load_SE_tagging: opening nodes -- bad format in $tag at $wid-th position!\n";
	    my $type = $1;
	    $tag = $';
	    if (!defined($phrase_types) or $phrase_types->{$type}) {
		$p = SRL::phrase->new($wid);
		$p->set_type($type);
		push @SP, $p;
	    }
	}
	$tag =~ s/^\*//;
	while ($tag ne "") {
	    $tag =~ /^([^\)]*)\)/  or die "phrase_set->load_SE_tagging: closing phrases -- bad format in $tag!\n";
	    my $type = $1;
	    $tag = $';
	    if (!$type or !defined($phrase_types) or $phrase_types->{$type}) {
		$p = pop @SP;
		(!$type) or ($type eq $p->type) or die "phrase_set->load_SE_tagging: types do not match!\n";
		$p->set_end($wid);	    
	    
		if (@SP) {
		    $SP[$#SP]->add_phrases($p);
		}
		else {
		    $set->add_phrases($p);
		}
	    }
	}
	$wid++;
    }
    (!@SP) or die "phrase_set->load_SE_tagging: some phrases are unclosed!\n";     
}


sub refs_start_end_tags {
    my ($s, $l) = @_;
    
    my (@S,@E,$i); 
    for ($i=0; $i<$l; $i++) {
	$S[$i] = ""; 
	$E[$i] = ""; 
    }
    
    my $p; 
    foreach $p ( $s->phrases ) {
	$S[$p->start] .= "(".$p->type;
#	$E[$p->end] = $E[$p->end].$p->type.")";
	$E[$p->end] .= ")";
    }

    return (\@S,\@E);
}


sub to_SE_tagging {
    my ($s, $l) = @_;
    
#     my (@S,@E,$i); 
#     for ($i=0; $i<$l; $i++) {
# 	$S[$i] = ""; 
# 	$E[$i] = ""; 
#     }

#     my $p; 
#     foreach $p ( $s->phrases ) {
# 	$S[$p->start] .= "(".$p->type;
# #	$E[$p->end] = $E[$p->end].$p->type.")";
# 	$E[$p->end] .= ")";
#     }

    my ($S,$E) = refs_start_end_tags($s,$l); 

    my $i;
    my @tags; 
    for ($i=0; $i<$l; $i++) {
#	$tags[$i] = sprintf("%8s*%-12s", $S->[$i], $E->[$i]); 
	$tags[$i] = sprintf("%8s*%-5s", $S->[$i], $E->[$i]); 
    }
    return @tags; 
}


sub to_IOB2_tagging {
    my ($s, $l) = @_;
    
    my (@tags,$p,$i); 

    foreach $p ( $s->phrases ) {
	my $tag = $p->type; 
	$i = $p->start; 
	$tags[$i] and $tags[$i] .= "/";
	$tags[$i] .= "B-".$tag; 
	$i++; 
	while ($i<=$p->end) {
	    $tags[$i] and $tags[$i] .= "/";
	    $tags[$i] .= "I-".$tag; 
	    $i++;
	}
    }
    for ($i=0; $i<$l; $i++) {
	if (!defined($tags[$i])) {
	    $tags[$i] = "O     "; 
	}
	else {
	    $tags[$i] = sprintf("%-6s", $tags[$i]); 
	}
    }
    return @tags; 
}


# ------------------------------------------------------------

#  Adds phrases in the set, recursively (ie. internal phrases are also added)
sub add_phrases {
    my ($s, @P) = @_;
    my $ph;
    foreach $ph ( map { $_->dfs } @P ) {
	$s->[0][$ph->start][$ph->end] = $ph;
	if ($ph->end >= $s->[1]) {
	    $s->[1] = $ph->end +1;
	}
    }
}

# returns the number of phrases in the set
sub size {
    my $set = shift;

    my ($i,$j);
    my $n;
    for ($i=0; $i<@{$set->[0]}; $i++) {
	if (defined($set->[0][$i])) {
	    for ($j=$i; $j<@{$set->[0][$i]}; $j++) {
		if (defined($set->[0][$i][$j])) {
		    $n++;
		}
	    }
	}
    }
    return $n;
}

# returns the phrase starting at word position $s and ending at $e
#  or undef if it doesn't exist
sub phrase {
    my ($set, $s, $e) = @_;    
    return $set->[0][$s][$e];
}


# Returns phrases in the set, recursively in depth first search order
#  that is, if a phrase is returned, all its subphrases are also returned
# If no parameters, returns all phrases
# If a pair of positions is given ($s,$e), returns phrases included
#  within the $s and $e positions
sub phrases {
    my $set = shift;
    my ($s, $e);
    if (!@_) {
	$s = 0;
	$e = $set->[1]-1;
    }
    else {
	($s,$e) = @_;
    }
    my ($i,$j); 
    my @P = ();
    for ($i=$s;$i<=$e;$i++) {
	if (defined($set->[0][$i])) {
	    for ($j=$e;$j>=$i;$j--) {
		if (defined($set->[0][$i][$j])) {
		    push @P, $set->[0][$i][$j];
		}
	    }
	}
    }
    return @P;
}


# Returns phrases in the set, non-recursively in sequential order
#  that is, if a phrase is returned, its subphrases are not returned
# If no parameters, returns all phrases
# If a pair of positions is given ($s,$e), returns phrases included
#  within the $s and $e positions
sub top_phrases {
    my $set = shift;
    my ($s, $e);
    if (!@_) {
	$s = 0;
	$e = $set->[1]-1;
    }
    else {
	($s,$e) = @_;
    }
    my ($i,$j); 
    my @P = ();
    $i = $s;
    while ($i<=$e) {
	$j=$e;
	while ($j>=$s) {
	    if (defined($set->[0][$i][$j])) {
		push @P, $set->[0][$i][$j];
		$i=$j;
		$j=-1;
	    }
	    else {
		$j--;
	    }
	}
	$i++;
    }
    return @P;
}


# returns the phrases which contain the terminal $wid, in bottom-up order
sub ancestors {
    my ($set, $wid) = @_;

    my @A; 
    my $N = $set->[1];

    my ($s,$e); 

    for ($s = $wid; $s>=0; $s--) {
	if (defined($set->[0][$s])) {
	    for ($e = $wid; $e<$N; $e++) {
		if (defined($set->[0][$s][$e])) {
		    push @A, $set->[0][$s][$e];
		}
	    }
	}
    }
    
    return @A; 
}


# returns a TRUE value if the phrase $p ovelaps with some phrase in
#  the set; the returned value is the reference to the conflicting phrase
# returns FALSE otherwise 
sub check_overlapping { 
    my ($set, $p) = @_;

    my ($s,$e); 
    for ($s=0; $s<$p->start; $s++) {
	if (defined($set->[0][$s])) {
	    for ($e=$p->start; $e<$p->end; $e++) {
		if (defined($set->[0][$s][$e])) {
		    return $set->[0][$s][$e];
		}
	    }
	}
    }
    for ($s=$p->start+1; $s<=$p->end; $s++) {
	if (defined($set->[0][$s])) {
	    for ($e=$p->end+1; $e<$set->[1]; $e++) {
		if (defined($set->[0][$s][$e])) {
		    return $set->[0][$s][$e];
		}
	    }
	}
    }
    
    return 0; 
}


## ----------------------------------------

# Discriminates a set of phrases (s1) wrt the current set (s0), returning 
#  intersection (s0^s1), over-predicted (s1-s0) and missed (s0-s1)
# Returns a hash reference containing three lists: 
#   $out->{ok} : phrases in $s0 and $1
#   $out->{op} : phrases in $s1 and not in $0
#   $out->{ms} : phrases in $s0 and not in $1
sub discriminate {
    my ($s0, $s1) = @_; 

    my $out; 
    @{$out->{ok}} = (); 
    @{$out->{ms}} = (); 
    @{$out->{op}} = (); 

    my $ph; 
    my %ok; 

    foreach $ph ($s1->phrases) {
	my $s = $ph->start; 
	my $e = $ph->end; 
	
	my $gph = $s0->phrase($s,$e); 
	if ($gph and $gph->type eq $ph->type) {
	    # correct
	    $ok{$s}{$e} = 1; 
	    push @{$out->{ok}}, $ph;
	}
	else {
	    # overpredicted
	    push @{$out->{op}}, $ph;
	}
    }

    foreach $ph ($s0->phrases) {
	my $s = $ph->start; 
	my $e = $ph->end; 
	
	if (!$ok{$s}{$e}) {
	    # missed
	    push @{$out->{ms}}, $ph;
	}
    }
    return $out; 
}


# compares the current set (s0) to another set (s1)
# returns the number of correct, missed an over-predicted phrases
sub evaluation {
    my ($s0, $s1) = @_;

    my $o = $s0->discriminate($s1); 

    my %e; 
    $e{ok} = scalar(@{$o->{ok}}); 
    $e{op} = scalar(@{$o->{op}}); 
    $e{ms} = scalar(@{$o->{ms}}); 

    return %e;
}


# generates a string representing the phrase set, 
# for printing purposes
sub to_string {
    my $s = shift;
    return join(" ", map { $_->to_string } $s->top_phrases);
}


1;
















