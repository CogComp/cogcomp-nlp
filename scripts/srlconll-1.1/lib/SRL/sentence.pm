################################################################################
#
#   Package    s e n t e n c e
#
#   February 2004
#
#   Stores information of a sentence, namely words, chunks, clauses,
#   named entities and propositions (gold and predicted). 
#
#   Provides access methods.
#   Provides methods for reading/writing sentences from/to files in 
#    CoNLL-2004/CoNLL-2005 formats. 
#
#
################################################################################


package SRL::sentence; 
use strict; 

use SRL::word; 
use SRL::phraseset; 
use SRL::prop;
use SRL::syntree; 


sub new {
    my ($pkg, $id) = @_; 
    
    my $s = []; 

    $s->[0] = $id;      # sentence number
    $s->[1] = undef;    # words (the list or the number of words)
    $s->[2] = [];       # gold props
    $s->[3] = [];       # predicted props
    $s->[4] = undef;    # chunks
    $s->[5] = undef;    # clauses
    $s->[6] = undef;    # full syntactic tree
    $s->[7] = undef;    # named entities

    return bless $s, $pkg; 
}

#-----

sub id {
    my $s = shift; 
    return $s->[0];
}

#-----

sub length {
    my $s = shift;
    if (ref($s->[1])) {
	return scalar(@{$s->[1]}); 
    }
    else {
	return $s->[1]; 
    }
}

sub set_length {
    my $s = shift; 
    $s->[1] = shift; 
}

#-----

# returns the i-th word of the sentence
sub word {
    my ($s, $i) = @_;
    return $s->[1][$i];
}


# returns the list of words of the sentence
sub words {
    my $s = shift;
    if (@_) {
        return map { $s->[1][$_] } @_;
    }
    else {
        return @{$s->[1]};
    }
}

sub ref_words {
    my $s = shift; 
    return $s->[1];
}


sub chunking {
    my $s = shift; 
    return $s->[4];
}

sub clausing {
    my $s = shift; 
    return $s->[5];
}

sub syntree {
    my $s = shift; 
    return $s->[6];
}

sub named_entities {
    my $s = shift; 
    return $s->[7];
}

#-----

sub add_gold_props {
    my $s = shift;
    push @{$s->[2]}, @_;
}

sub gold_props {
    my $s = shift; 
    return @{$s->[2]};
}

sub add_pred_props {
    my $s = shift;
    push @{$s->[3]}, @_;
}

sub pred_props {
    my $s = shift; 
    return @{$s->[3]};
}


#------------------------------------------------------------
# I/O  F U N C T I O N S
#------------------------------------------------------------

# Reads a complete (words, synt, props) sentence from a stream
# Returns: the reference to the sentence object or
#          undef if no sentence found 
# The propositions in the file are stored as gold props
# For each gold prop, an empty predicted prop is created
#
# The %C hash contains the column number for each annotation of 
# the datafile. 
#
sub read_from_stream {
    my ($pkg, $id, $fh, %C) = @_;

    if (!%C) {
	%C = (  words   => 0,
		pos     => 1,
		chunks  => 2,
		clauses => 3,
		syntree => 4,
		ne      => 5,
		props   => 6
		)
    }

#    my $k;
#    foreach $k ( "words", "pos", "props" ) {
#	if (!exists($C{$k}) {
#	    die "sentence->read_from_stream :: undefined column number for $k.\n"; 
#	}
#    }

    my $cols = read_columns($fh); 

    if (!@$cols) {
	return undef; 
    }
    
    my $s = $pkg->new($id); 
    
    # words and PoS
    my $words = $cols->[$C{words}];
    my $pos = $cols->[$C{pos}];

    # initialize list of words
    $s->[1] = [];
    my $i;    
    for ($i=0;$i<@$words;$i++) {
	push @{$s->[1]}, SRL::word->new($i, $words->[$i], $pos->[$i]);
    }
    
    my $c; 

    # chunks
    if (exists($C{chunks})) {
	$c = $cols->[$C{chunks}]; 
	# initialize chunking
	$s->[4] = SRL::phrase_set->new(); 
	$s->[4]->load_SE_tagging(@$c); 
    }

    # clauses
    if (exists($C{clauses})) {
	$c = $cols->[$C{clauses}]; 
	# initialize clauses
	$s->[5] = SRL::phrase_set->new(); 
	$s->[5]->load_SE_tagging(@$c); 
    }

    # syntree
    if (exists($C{syntree})) {
	$c = $cols->[$C{syntree}]; 
	# initialize syntree
	$s->[6] = SRL::syntree->new(); 
	$s->[6]->load_SE_tagging($s->[1], @$c);
    }

    # named entities
    if (exists($C{ne})) {
	$c = $cols->[$C{ne}]; 
	$s->[7] = SRL::phrase_set->new(); 
	$s->[7]->load_SE_tagging(@$c); 
    }
       

    my $i = 0; 
    while ($i<$C{props}) {
	shift @$cols; 
	$i++;
    }

    # gold props
    my $targets = shift @$cols or die "error :: reading sentence $id :: no targets found!\n"; 
    if (@$cols) {
	$s->load_props($s->[2], $targets, $cols); 
    }

    # initialize predicted props
    foreach $i ( grep { $targets->[$_] ne "-" } ( 0 .. scalar(@$targets)-1 ) ) {
	push @{$s->[3]}, SRL::prop->new($targets->[$i], $i);
    }

    return $s; 
}



#------------------------------------------------------------


# reads the propositions of a sentence from files
# allows to store propositions as gold and/or predicted, 
#  by specifying filehandles as values in the %FILES hash
#  indexed by {GOLD,PRED} keys
# expects: each prop file: first column specifying target verbs,
#          and remaining columns specifying arguments
# returns a new sentence, containing the list of prop 
#         objects, one for each column, in gold/pred contexts
# returns undef when EOF
sub read_props {
    my ($pkg, $id, %FILES) = @_; 

    my $s = undef; 
    my $length = undef; 

    if (exists($FILES{GOLD})) {
	my $cols = read_columns($FILES{GOLD}); 

	# end of file 
	if (!@$cols) {
	    return undef; 
	}

	$s = $pkg->new($id); 
	my $targets = shift @$cols;     
	$length = scalar(@$targets); 
	$s->set_length($length); 
	$s->load_props($s->[2], $targets, $cols); 	
    }
    if (exists($FILES{PRED})) {
	my $cols = read_columns($FILES{PRED}); 

	if (!defined($s)) {
	    # end of file 
	    if (!@$cols) {
		return undef; 
	    }
	    $s = $pkg->new($id); 
	}
	my $targets = shift @$cols;

	if (defined($length)) {
	    ($length != scalar(@$targets))  and
		die "ERROR : sentence $id : gold and pred sentences do not align correctly!\n"; 
	}
	else {
	    $length = scalar(@$targets); 
	    $s->set_length($length); 
	}	
	$s->load_props($s->[3], $targets, $cols); 
    }

    return $s; 
}


sub load_props {
    my ($s, $where, $targets, $cols) = @_;
    
    my $i; 
    for ($i=0; $i<@$targets; $i++) {
	if ($targets->[$i] ne "-") {
	    my $prop = SRL::prop->new($targets->[$i], $i); 

	    my $col = shift @$cols;
	    if (defined($col)) {
#	    print "SE Tagging: ", join(" ", @$col), "\n"; 
		$prop->load_SE_tagging(@$col); 
	    }
	    else {
		print STDERR "WARNING : sentence ", $s->id, " : can't find column of args for prop ", $prop->verb, "!\n"; 
	    }
	    push @$where, $prop;
	}
    }
}


# writes a sentence to an output stream
# allows to specify which parts of the sentence are written
#  by giving true values to the %WHAT hash, indexed by
#  {WORDS,SYNT,GOLD,PRED} keys
sub write_to_stream {
    my ($s, $fh, %WHAT) = @_;

    if (!%WHAT) {
	%WHAT = ( WORDS => 1, 
		  PSYNT => 1,
		  FSYNT => 1,
		  GOLD => 0,
		  PRED => 1
		  );
    }

    my @columns; 

    if ($WHAT{WORDS}) {
	my @words = map { $_->form } $s->words; 
	push @columns, \@words; 
    }
    if ($WHAT{PSYNT}) {
	my @pos = map { $_->pos } $s->words; 
	push @columns, \@pos; 
	my @chunks = $s->chunking->to_SE_tagging($s->length);
	push @columns, \@chunks; 
	my @clauses = $s->clausing->to_SE_tagging($s->length);
	push @columns, \@clauses; 
    }
    if ($WHAT{FSYNT}) {
	my @pos = map { $_->pos } $s->words; 
	push @columns, \@pos; 
	my @sttags = $s->syntree->to_SE_tagging();
	push @columns, \@sttags;
    }
    if ($WHAT{GOLD}) {
	push @columns, $s->props_to_columns($s->[2]); 
    }
    if ($WHAT{PRED}) {
	push @columns, $s->props_to_columns($s->[3]); 
    }
    if ($WHAT{PROPS}) {
	push @columns, $s->props_to_columns($WHAT{PROPS}); 
    }

    
    reformat_columns(\@columns);

    # finally, print columns word by word
    my $i;
    for ($i=0;$i<$s->length;$i++) {
	print $fh join(" ", map { $_->[$i] } @columns), "\n"; 
    }
    print $fh "\n"; 


}

# turns a set of propositions (target verbs + args for each one) into a set of 
#  columns in the CoNLL Start-End format
sub props_to_columns {
    my ($s, $Pref) = @_; 

    my @props = sort { $a->position <=> $b->position } @{$Pref}; 

    my $l = $s->length;
    my $verbs = []; 
    my @cols = ( $verbs ); 
    my $p; 

    foreach $p ( @props ) {
	defined($verbs->[$p->position]) and die "sentence->preds_to_columns: already defined verb at sentence ", $s->id, " position ", $p->position, "!\n"; 
	$verbs->[$p->position] = sprintf("%-15s", $p->verb); 

	my @tags = $p->to_SE_tagging($l); 
	push @cols, \@tags; 
    }

    # finally, define empty verb positions
    my $i;
    for ($i=0;$i<$l;$i++) {
	if (!defined($verbs->[$i])) {
	    $verbs->[$i] = sprintf("%-15s", "-"); 
	}
    }
    
    return @cols; 
}



# Writes the predicted propositions of the sentence to an output file handler ($fh)
# Specifically, writes a column of target verbs, and a column of arguments 
#  for each target verb
# OBSOLETE : the same can be done with write_to_stream($s, PRED => 1)
sub write_pred_props {
    my ($s, $fh) = @_;

    my @props = sort { $a->position <=> $b->position } $s->pred_props; 

    my $l = $s->length;
    my @verbs = (); 
    my @cols = (); 
    my $p; 

    foreach $p ( @props ) {
	defined($verbs[$p->position]) and die "prop->write_pred_props: already defined verb at sentence ", $s->id, " position ", $p->position, "!\n"; 
	$verbs[$p->position] = $p->verb; 

	my @tags = $p->to_SE_tagging($l); 
	push @cols, \@tags; 
    }

    # finally, print columns word by word
    my $i;
    for ($i=0;$i<$l;$i++) {
	printf $fh "%-15s %s\n", (defined($verbs[$i])? $verbs[$i] : "-"), 
	   join(" ", map { $_->[$i] } @cols); 
    }
    print "\n"; 
}



# reads columns until blank line or EOF
# returns an array of columns (each column is a reference to an array containing the column)
# each column in the returned array should be the same size
sub read_columns {
    my $fh = shift; 

    # read columns until blank line or eof
    my @cols; 
    my $i; 
    my @line = split(" ", <$fh>); 
    while (@line) {

	for ($i=0; $i<@line; $i++) {
	    push @{$cols[$i]}, $line[$i]; 
	}
	@line = split(" ", <$fh>); 
    }
    
    return \@cols; 
}



# reformats the tags of a list of columns, so that each 
# column has a fixed width along all tags
#
#
sub reformat_columns {
    my $cols = shift;   # a reference to the list of columns of a sentence
    
    my $i;
    for ($i=0;$i<scalar(@$cols);$i++) {
	column_pretty_format($cols->[$i]); 
    }
}



# reformats the tags of a column, so that each 
# tag has the same width
#
# tag sequences are left justified
# start-end annotations are centered at the asterisk
#
sub column_pretty_format {    
    my $col = shift;    # a reference to the column (array) of tags

    (!@$col) and return undef; 

    my ($i); 
    if ($col->[0] =~ /\*/) {
	
	# Start-End
	my $ok = 1; 

	my (@s,@e,$t,$ms,$me); 
	$ms = 2; $me = 2; 
	$i = 0; 
	while ($ok and $i<@$col) {
	    if ($col->[$i] =~ /^(.*\*)(.*)$/) {
		$s[$i] = $1; 
		$e[$i] = $2; 
		if (length($s[$i]) > $ms) {
		    $ms = length($s[$i]);
		}
		if (length($e[$i]) > $me) {
		    $me = length($e[$i]);
		}
	    }
	    else {
		# In this case, the current token is not compliant with SE format
		# So, we treat format the column as a sequence of tags
		$ok = 0; 
	    }
	    $i++;
	}
#	print "M $ms $me\n"; 

	if ($ok) {
	    my $f = "%".($ms+1)."s%-".($me+1)."s";
	    for ($i=0; $i<@$col; $i++) {
		$col->[$i] = sprintf($f, $s[$i], $e[$i]);
	    }
	    return; 
	}
    }	

    # Tokens
    my $l=0; 
    map { (length($_)>$l) and ($l=length($_)) } @$col;
    my $f = "%-".($l+1)."s";
    for ($i=0; $i<@$col; $i++) {
	$col->[$i] = sprintf($f,$col->[$i]); 
    }
    
}



1;








