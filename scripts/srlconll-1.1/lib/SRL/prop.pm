##################################################################
#
#  Package    p r o p  :  A proposition (verb + args)
#
#  January 2004
#
##################################################################


package SRL::prop;

use SRL::arg;
use SRL::phraseset;
use strict; 


# Constructor: creates a new prop, with empty arguments
# Parameters: verb form, position of verb
sub new {
    my ($pkg, $v, $position) = @_;

    my $p = [];
    
    $p->[0] = $v;         # the verb
    $p->[1] = $position;  # verb position
    $p->[2] = undef;      # verb sense
    $p->[3] = [];         # args, empty by default 

    return bless $p, $pkg; 
}

## Accessor/Initialization methods

# returns the verb form of the prop
sub verb {
    my $p = shift; 
    return $p->[0];
}

# returns the verb position of the verb in the prop
sub position {
    my $p = shift; 
    return $p->[1];
}

# returns the verb sense of the verb in the prop
sub sense {
    my $p = shift; 
    return $p->[2];
}

# initializes the verb sense of the verb in the prop
sub set_sense {
    my $p = shift; 
    $p->[2] = shift;
}


# returns the list of arguments of the prop
sub args {
    my $p = shift; 
    return @{$p->[3]};
}

# initializes the list of arguments of the prop
sub set_args {
    my $p = shift; 
    @{$p->[3]} = @_;
}

# adds arguments to the prop
sub add_args {
    my $p = shift; 
    push @{$p->[3]}, @_;
}

# Returns the list of phrases of the prop
# Each argument corresponds to one phrase, except for
# discontinuous arguments, where each piece forms a phrase
sub phrases {
    my $p = shift; 
    return map { $_->single ? $_ : $_->phrases} @{$p->[3]};
}


######   Methods

# Adds arguments represented in Start-End tagging
# Receives a list of Start-End tags (one per word in the sentence)
# Creates an arg object for each argument in the taggging 
#  and modifies the prop so that the arguments are part of it
# Takes into account special treatment for discontinuous arguments
sub load_SE_tagging {
    my ($prop, @tags) = @_;

    # auxiliar phrase set
    my $set = SRL::phrase_set->new(); 
    $set->load_SE_tagging(@tags); 
    
    # store args per type, to be able to continue them
    my %ARGS;
    my $a; 

    # add each phrase as an argument, with special treatment for multi-phrase arguments (A C-A C-A)
    foreach $a ( $set->phrases ) {

	# the phrase continues a started arg
	if ($a->type =~ /^C\-/) {
	    my $type = $';   # '
	    if (exists($ARGS{$type})) {
		my $pc = $a;  
		$a = $ARGS{$type}; 
		if ($a->single) {
		    # create the head phrase, considered arg until now
		    my $ph = SRL::phrase->new($a->start, $a->end, $type);
		    $a->add_phrases($ph); 
		}
		$a->add_phrases($pc); 
		$a->set_end($pc->end);
	    }
	    else {
#		print STDERR "WARNING : found continuation phrase \"C-$type\" without heading phrase: turned into regular $type argument.\n";
		# turn the phrase into arg
		bless $a, "SRL::arg"; 
		$a->set_type($type); 
		push @{$prop->[3]}, $a; 
		$ARGS{$a->type} = $a; 
	    }
	}
	else {
	    # turn the phrase into arg
	    bless $a, "SRL::arg"; 
	    push @{$prop->[3]}, $a; 
	    $ARGS{$a->type} = $a; 
	}
    }

}


## discriminates the args of prop $pb wrt the args of prop $pa, returning intersection(a^b), a-b and b-a
# returns a hash reference containing three lists: 
# $out->{ok} : args in $pa and $pb
# $out->{ms} : args in $pa and not in $pb
# $out->{op} : args in $pb and not in $pa
sub discriminate_args {
    my $pa = shift; 
    my $pb = shift; 
    my $check_type = @_ ? shift : 1; 

    my $out = {}; 
    !$check_type and @{$out->{eq}} = (); 
    @{$out->{ok}} = (); 
    @{$out->{ms}} = (); 
    @{$out->{op}} = (); 

    my $a; 
    my %ok; 

    my %ARGS; 

    foreach $a ($pa->args) {
	$ARGS{$a->start}{$a->end} = $a; 
    }

    foreach $a ($pb->args) {
	my $s = $a->start; 
	my $e = $a->end;
       	
	my $gold = $ARGS{$s}{$e}; 
	if (!defined($gold)) {
	    push @{$out->{op}}, $a;
	}
	elsif ($gold->single and $a->single) {
	    if (!$check_type or ($gold->type eq $a->type)) {
		!$check_type and push @{$out->{eq}}, $gold;
		push @{$out->{ok}}, $a;
		delete($ARGS{$s}{$e}); 
	    }
	    else {
		push @{$out->{op}}, $a;
	    }
	}
	elsif (!$gold->single and $a->single) {
	    push @{$out->{op}}, $a;
	}
	elsif ($gold->single and !$a->single) {
	    push @{$out->{op}}, $a;
	}
	else {
	    # Check phrases of arg
	    my %P; 
	    my $ok = (!$check_type or ($gold->type eq $a->type)); 
	    $ok and map { $P{ $_->start.".".$_->end } = 1 } $gold->phrases;
	    my @P = $a->phrases; 
	    while ($ok and @P) {
		my $p = shift @P; 
		if ($P{ $p->start.".".$p->end }) {
		    delete $P{ $p->start.".".$p->end }
		}
		else {
		    $ok = 0;
		}
	    }
	    if ($ok and !(values %P)) {
		!$check_type and push @{$out->{eq}}, $gold;
		push @{$out->{ok}}, $a;
		delete $ARGS{$s}{$e}
	    }
	    else {
		push @{$out->{op}}, $a;
	    }
	}
    }
    
    my ($s); 
    foreach $s ( keys %ARGS ) {
	foreach $a ( values %{$ARGS{$s}} ) {
	    push @{$out->{ms}}, $a;
	}
    }

    return $out; 
}


# Generates a Start-End tagging for the prop arguments
# Expects the prop object, and l=length of the sentence
# Returns a list of l tags
sub to_SE_tagging {
    my $prop = shift; 
    my $l = shift; 
    my @tags = (); 

    my ($a, $p); 
    foreach $a ( $prop->args ) {
	my $t = $a->type; 
	my $cont = 0; 
	foreach $p ( $a->single ? $a : $a->phrases ) {
	    if (defined($tags[$p->start])) {
		die "prop->to_SE_tagging: Already defined tag in start position ", $p->start, "! Prop phrases overlap or embed!\n";
	    }
	    if ($p->start != $p->end) {
		$tags[$p->start] = sprintf("%7s", "(".$t)."*   ";
		if (defined($tags[$p->end])) {
		    die "prop->to_SE_tagging: Already defined tag in end position ", $p->end, "! Prop phrases overlap or embed!\n";
		}
#		$tags[$p->end] = "       *".sprintf("%-7s", $t.")"); 
		$tags[$p->end] = "       *".sprintf("%-3s", ")"); 
	    }
	    else {
#		$tags[$p->start] = sprintf("%7s", "(".$t)."*".sprintf("%-7s", $t.")"); 
		$tags[$p->start] = sprintf("%7s", "(".$t)."*".sprintf("%-3s",")"); 
	    }

	    if (!$cont) {
		$cont = 1; 
		$t = "C-".$t;
	    }
	}
    }
    
    my $i; 
    for ($i=0; $i<$l; $i++) {
	if (!defined($tags[$i])) {
	    $tags[$i] = "       *   ";
	}
    }

    return @tags; 
}


# generates a string representing the proposition
sub to_string {
    my $p = shift; 

    my $s = "[". $p->verb . "@" . $p->position . ": ";
    $s .= join(" ", map { $_->to_string } $p->args);
    $s .= " ]";
    
    return $s; 
}


1;


