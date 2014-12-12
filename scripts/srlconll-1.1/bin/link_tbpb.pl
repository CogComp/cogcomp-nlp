#! /usr/bin/perl 

$| = 1; 

use strict; 

use SRL::syntree; 
use SRL::phraseset; 
use SRL::prop;


######## Arguments and Help

use Getopt::Long; 
my %options;
GetOptions(\%options, 
	   "v:i",     # print wsj tree
	   "noi", 
	   
	   "nb",

	   "al",

	   "sv",    # skip ambiguous verbs	   

	   "ss",    # 
	   "sc",    # 
	   "sd",    # 
	   "sm", 
	   "st",    # skip traces
	   "ft",     # filter traces
	   
	   "u"
	   
	   ); 


if (@ARGV != 2) {
    my $help = << "end_of_help;";
Usage:   link_tbpb.pl <tb file> <pb file>
Options: 
    -v     Verbosity level (to STDERR) 
            0 : no verbose
	    1 : log messages for filtered props   (default)
	    2 : log messages and syntactic tree for filtered props
	    3 : syntactic tree and props of all sentences

    -noi   Do NOT print input 

    -al    Consider all arg labels (i.e. do not filter out weird argument labels)

    -sv    Skip ambiguous verbs

    -ss    Skip "simple" predicates
    -sc    Skip predicates with correferenced arguments 
    -sd    Skip predicates with discontinuous arguments
    -sm    Skip coordinated predicates, ie. multiple predicates related to the same verb

    -st    Skip word traces (argument reestructuration and, if NULL, elimination)
    -ft    Filter out traces, ie. do not output -NONE- words

    -u     Check unicity of arguments in a predicate 

    -nb    Read NomBank propositions
    

end_of_help;
    print $help; 
    exit; 
}

my $tbfile = shift @ARGV;
my $pbfile = shift @ARGV;


########  Read PropBank file of annotations

my $PB = read_propbank($pbfile); 

########  Read TB trees and link PB annotations

if ($tbfile =~ /\.gz$/) {
    open TB, "gunzip -c $tbfile | " or die "error opening gzipped tb file! $!\n"; 
}
else {
    open TB, "$tbfile" or die "error opening tb file! $!\n"; 
}

my $verbose = exists($options{v}) ? $options{v} : 1; 

my $tree = SRL::syntree->read_wsj_mrg(\*TB); 
my $nsentence = 0; 
while ( defined $tree ) {

    if ($verbose >= 3) {
	print STDERR "\n********** Sentence $nsentence ***********\n"; 
	print STDERR "********** Tree \n"; 
	print STDERR $tree->to_pretty_string, "\n\n";
	print STDERR "********** Predicates\n"; 
	my $p; 
	foreach $p ( @{$PB->[$nsentence]} ) {
	    print STDERR "\t", join (" ", $p->{term}, $p->{verb}, $p->{sense}, @{$p->{args}}), "\n"
	}
	print STDERR "**********\n"; 
    }

    ## @PAS is the list of pred-arg structures of the sentence
    my @PROPS = link_predicates($PB, $tree, $nsentence); 

    ## Output the structures: words, pos, predicates, and a list of pas
    my @T = $tree->terminals; 

    my @output; # vector of terminals; for each one, one vector of Ncols columns 
    my $Ncols = 6; # sentence, number, form, PoS, verb, sense
    my $Vcol = 5; 
    my $format = "%3i %2i %-20s %-6s %-2s %-15s"; 
    if ($options{noi}) {
	$format = "%-2s %-15s"; 
	$Ncols = 2; 
	$Vcol = 1; 
    }

    my $i; 
    for ($i=0; $i<@T; $i++) {
	my $w = $T[$i]->content; 
	if ($options{noi}) {
	    @{$output[$i]} = ("-","-"); 
	}
	else {
	    @{$output[$i]} = ($nsentence, $i, $w->form, $w->pos, "-", "-"); 
	}
    }


    ## @PROPS is assumed to be sorted by verb terminal number
    my $prop; 
    foreach $prop ( @PROPS ) {

	my $t = $prop->{term}; 
	if ($output[$t][$Vcol] != "-") {
	    die "link_tbpb.pl: $pbfile : s $nsentence t $t : already defined predicate at position $t!\n";
	}
	$output[$t][$Vcol] = $prop->{verb}; 
	$output[$t][$Vcol-1] = $prop->{sense}; 

	my @tags = $prop->{args}->to_SE_tagging(scalar(@output)); 

	my $j; 
	for ($j=0; $j<@output; $j++) {
	    $output[$j][$Ncols] = $tags[$j]; 
	}
	$Ncols++;
	$format .= " %-15s"; 
    }


    # Finally, print the output, word by word
    for ($i=0; $i<@output; $i++) {
	if (!$options{ft} or $T[$i]->content->pos ne "-NONE-") {
	    printf $format,  @{$output[$i]};
	    print "\n"; 
	}
    }

    # Print empty line at the end of each sentence
    print "\n"; 
    
    $tree = SRL::syntree->read_wsj_mrg(\*TB); 
    $nsentence++;
}

# end of main program

############################################################



sub link_predicates {
    my ($PB, $tree, $nsentence) = @_;

    my @T = $tree->terminals; 

    # list of predicate-argument structures (pas)
    my @PROPS; 

    # if true, the tree and pbann are printed to stderr
    my $tree2stderr = 0;  

    my $pbann; 
    foreach $pbann ( @{$PB->[$nsentence]} ) {
	
	my $prop = {};
	$prop->{args} = SRL::phrase_set->new(); 
	$prop->{verb} = $pbann->{verb};
	$prop->{sense} = $pbann->{sense};
	$prop->{term} = $pbann->{term};
	
	# flag indicating if the prop is ok
	my $ok = 1; 
	
	my $allSimples = 1; # Checks whether all arguments in a proposition are simple

	if ($options{sv} and $prop->{sense} eq "XX") {
	    $verbose and print STDERR "link_tb_pb : $pbfile : s ", $nsentence, " t ", $prop->{term} , " : ", 
	    $prop->{verb} , " : ambiguous verb : skipping predicate\n"; 
	    $ok = 0;
	}
	
	my @ARGS = @{$pbann->{args}};
	my @PHRASES = (); 
	while ($ok and @ARGS ) {
	    my $arg = shift @ARGS;

	    if (0 and $arg =~ /REF$/) { 
		$verbose and print STDERR "link_tb_pb : $pbfile : s ", $nsentence, " t ", $prop->{term} , " : ", 
		$prop->{verb} , " : filtering out argument $arg\n"; 
		next; 
	    }
	    
	    $arg =~ /^([^-]+)\-(.+)$/ or die "Unrecognized format for predicate argument: $arg!\n"; 
	    
	    # reference to a node in the tree in terminal-height
	    my $noderef = $1;

	    # Check and Map label 
	    my $tmplabel = $2;
	    my $label = map_arglabel($2); 

	    if (!defined($label)) {
		$verbose and print STDERR "link_tb_pb : $pbfile : s ", $nsentence, " t ", $prop->{term} , " : ", 
		$prop->{verb} , " : wrong label $tmplabel : Skipping\n"; 
		$ok = 0; 
	    }

	    if ( $noderef =~ /^(\d+):(\d+)$/) {
		my $p = pbarg_to_phrase($tree, $1, $2, $label); 
		defined($p) and push @PHRASES, $p; 
	    }
	    elsif ($noderef =~ /\*/) {
		$allSimples = 0; 

		if ($options{sc}) {
		    $verbose and print STDERR "link_tb_pb : $pbfile : s ", $nsentence, " t ", $prop->{term}, " : ", 
		    $prop->{verb} , " : found node with correference ($noderef)! Skipping\n"; 
		    $ok = 0; 
		}
		else {
		    $ok = process_coreferenced_annotation($tree, $nsentence, $prop, $label, $noderef, \@PHRASES); 
		}
	    }
	    elsif ($noderef =~ /,/) {
		$allSimples = 0; 

		if ($options{sd}) {
		    $verbose and print STDERR "link_tb_pb : $pbfile : s ", $nsentence, " t ", $prop->{term} , " : ", 
		    $prop->{verb} , " : found node with discontinuous argument ($noderef)! Skipping\n"; 
		    $ok = 0; 
		}
		else {
		    $ok = process_split_annotation($tree, $nsentence, $prop, $label, $noderef, \@PHRASES); 
		}	
	    }
	}

	while ($ok and @PHRASES ) {
	    my $p1 = shift @PHRASES; 
	    my $p2 = $prop->{args}->phrase($p1->start, $p1->end); 
	    if (defined($p2) and ($p2->type ne $p1->type)) {
		$verbose and print STDERR "link_tb_pb : $pbfile : s ", $nsentence, " t ", $prop->{term} , " : ", 
		$prop->{verb} , " : overlapping at (", $p1->start, ",", $p1->end, "): ! Skipping\n"; 
		$ok = 0; 
	    }
	    else {
		$prop->{args}->add_phrases($p1); 
	    }
	}

    
 	if ($allSimples and $options{ss}) {
	    $ok = 0; 
	    $verbose and print STDERR "link_tb_pb : $pbfile : s ", $nsentence, " t ", $prop->{term} , " : ", 
	    $prop->{verb} , " : simple predicate : Skipping\n"; 
	}
	
	if ( $ok and test_overlapping($prop->{args})) {
	    $verbose and print STDERR "link_tb_pb : $pbfile : s ", $nsentence, " t ", $prop->{term} , " : ", 
	    $prop->{verb} , " : two or more phrases overlap! Skipping\n"; 
	    $ok = 0;
	}

	if ( $ok and $options{u} and !test_arguniq($prop->{args})) {
	    $verbose and print STDERR "link_tb_pb : $pbfile : s ", $nsentence, " t ", $prop->{term} , " : ", 
	    $prop->{verb} , " : some arguments are not uniq! Skipping\n"; 
	    $ok = 0;
	}

	if ($ok) {
	    push @PROPS, $prop; 
	}
	else {
	    $tree2stderr = 1; 
	}
	       
    }


    #### Merge coordinate predicates
    
    # index verb -> pas
    my $pas; 
    my @verb;
    while ( @PROPS ) {
	my $prop = shift @PROPS; 
	push @{$verb[$prop->{term}]}, $prop; 
    }
    my $t; 
    for ($t=0; $t<@verb; $t++) {
	if (!defined($verb[$t])) {
	    ##
	}
	elsif (@{$verb[$t]}==1) {
	    push @PROPS, @{$verb[$t]};
	}
	elsif (@{$verb[$t]}>1) {
	    if ($options{sm}) {
		$verbose and print STDERR "link_tb_pb : $pbfile : s ", $nsentence, " t ", $t , " : ", 
		"found ", scalar(@{$verb[$t]}) , " multiple predicates (", join(" ", map { $_->{verb} } @{$verb[$t]}), 
		")! Skipping\n"; 
		$tree2stderr = 1; 
	    }
	    else {
		my $merged= merge_predicates(@{$verb[$t]}); 
		if ($merged) {
		    if (!test_overlapping($merged->{args})) {
			$verbose and print STDERR "link_tb_pb : $pbfile : s ", $nsentence, " t ", $t , " : ", 
			"found ", scalar(@{$verb[$t]}) , " multiple predicates (", join(" ", map { $_->{verb} } @{$verb[$t]}), 
			")! merged into a single predicate, Resolved\n"; 
			push @PROPS, $merged; 
		    }
		    else {
			$verbose and print STDERR "link_tb_pb : $pbfile : s ", $nsentence, " t ", $t , " : ", 
			"found ", scalar(@{$verb[$t]}) , " multiple predicates (", join(" ", map { $_->{verb} } @{$verb[$t]}), 
			")! args overlap after merging, Skipping\n";
			$tree2stderr = 1; 
		    }			
		}
		else {
		    $verbose and print STDERR "link_tb_pb : $pbfile : s ", $nsentence, " t ", $t , " : ", 
		    "found ", scalar(@{$verb[$t]}) , " multiple predicates (", join(" ", map { $_->{verb} } @{$verb[$t]}), 
		    ")! unable to merge them, Skipping\n"; 
		    $tree2stderr = 1; 
		}
	    }
	}       
    }

    
    ###
    if ($verbose==2 and $tree2stderr) {
	print STDERR "********** Sentence $nsentence:  WSJ Tree \n"; 
	print STDERR $tree->to_pretty_string, "\n";
	print STDERR "********** Predicates, some skipped\n"; 
	my $p; 
	foreach $p ( @{$PB->[$nsentence]} ) {
	    print STDERR "\t", join (" ", $p->{term}, $p->{verb}, $p->{sense}, @{$p->{args}}), "\n"
	}
	print STDERR "**********\n"; 
    }

    return @PROPS; 
}



########################################


sub process_coreferenced_annotation {
    my ($tree, $nsentence, $prop, $label, $noderef, $PHRASES) = @_;


    my $ok = 1; 
    my @NR = split (/\*/, $noderef); 
    my $clabel = "R-$label";

    my $T = $tree->ref_terminals(); 
    
    my $npr = 0; # number of real phrases
    my $npl = 0; # number of link phrases    
    my @tmpP; 
    foreach $noderef ( @NR ) {
	if ($noderef =~ /,/) {	    
	    $ok = process_split_annotation($tree, $nsentence, $prop, $label, $noderef, \@tmpP); 
	    $npr++;  # We assume a split phrase is never a reference
	}
	else {
	    $noderef =~ /^(\d+):(\d+)$/ or die "Unrecognized format for predicate correferenced argument: $noderef!\n"; 
	    my $p = pbarg_to_phrase($tree, $1, $2, $clabel);
	    if (defined($p)) {

		my $length = $p->end - $p->start +1;
		if (($length == 1) and
		    ($T->[$p->start]->content->pos =~ /^(WDT|WRB|WP|\-NONE\-)$/ or
		     $T->[$p->start]->content->form eq "that" or
		     $T->[$p->start]->content->form eq "which")) {
		    $npl++; 
		}
		elsif (($length>1) and 
		       # most of which
		       # in/to/upon which/whom
		       # seven of whom
		       # the bulk of which
		       # just when
		       $T->[$p->end]->content->pos =~ /^(WDT|WP|WRB)$/) {
		    $npl++;
		}
		elsif (($length>1) and 
		       $T->[$p->start]->content->pos =~ /^(WDT)$/) {
		    $npl++;
		}
		elsif ($T->[$p->start]->content->pos eq 'WP$') {      # '
		       $npl++; 
		   }
		else {
		    $npr++;
		    $p->set_type($label); 
		}

		push @tmpP, $p;
	    }
	}
    }

    if ($npr + $npl > 1) {       
	my $p; 
	if ($verbose) {
	    print STDERR "link_tb_pb : $pbfile : s ", $nsentence, " t ", $prop->{term} , " : ", 
	    "correferenced arguments : ";
	    my $p; 
	    foreach $p ( @tmpP ) {
		print STDERR "(", join(" ", map { $T->[$_]->content->form . "~" . $T->[$_]->content->pos } 
				       ($p->start .. $p->end)), ") "; 
	    }
	}
	if ($npr<2) {
	    $verbose and print STDERR "! Found $npr reals; $npl links! Resolved\n"; 
	}
	else {
	    @tmpP = (); 
	    $ok = 0; 
	    $verbose and print STDERR "! Found $npr reals; $npl links! Skipping\n"; 
	}
    }
    
    push @$PHRASES, @tmpP;

    return $ok; 
}


sub process_split_annotation {
    my ($tree, $nsentence, $prop, $label, $noderef, $PHRASES) = @_;

    my $ok = 1; 

    my @NR = split (/,/, $noderef); 
    my $clabel = "C-$label";
    my @tmpP; 
    foreach $noderef ( @NR ) {
	$noderef =~ /^(\d+):(\d+)$/ or die "Unrecognized format for predicate discontinuous argument: $noderef!\n"; 
	my $p = pbarg_to_phrase($tree, $1, $2, $label);
	defined($p) and push @tmpP, $p;
    }
    if (@tmpP == 0) {
	$verbose and print STDERR "link_tb_pb : $pbfile : s ", $nsentence, " t ", $prop->{term} , " : ", 
	$prop->{verb} , " : discontinuous argument ($noderef)! all are -NONE-'s, Resolved\n"; 
    }
    else {
	if (@tmpP == 1) {
	    $verbose and print STDERR "link_tb_pb : $pbfile : s ", $nsentence, " t ", $prop->{term} , " : ", 
	    $prop->{verb} , " : discontinuous argument ($noderef)! only one real argument, Resolved\n"; 
	}
	
	@tmpP = sort_and_join($tree, @tmpP); 
	
	
	if (@tmpP == 0) {
	    $verbose and print STDERR "link_tb_pb : $pbfile : s ", $nsentence, " t ", $prop->{term} , " : ", 
	    $prop->{verb} , " : overlapping in phrases of discontinuous argument! Skipping\n"; 
	    $ok = 0; 
	}
	elsif (@tmpP == 1) {
	    $verbose and print STDERR "link_tb_pb : $pbfile : s ", $nsentence, " t ", $prop->{term} , " : ", 
	    $prop->{verb} , " : discontinuous argument ($noderef)! actually contiguous, Resolved\n"; 
			}
	else {
	    $verbose and print STDERR "link_tb_pb : $pbfile : s ", $nsentence, " t ", $prop->{term} , " : ", 
	    $prop->{verb} , " : discontinuous argument ($noderef)! Using C- tags, Resolved\n"; 
	    
	    $tmpP[0]->set_type($label); 
	    my $i; 
	    for ($i=1; $i<@tmpP; $i++)  {
		$tmpP[$i]->set_type($clabel); 			
			    }
	}
    }
    push @$PHRASES, @tmpP;

    return $ok; 
}



########################################
# Low-level functions
#

## Gets a reference to a tree node, and a pred-arg label
## Returns a phrase corresponding to the predicate argument
## Returns undef if null trace argument
## Option -st: Eliminates traces, reestructuring phrases if needed
sub pbarg_to_phrase {
    my ($tree, $t, $h, $label) = @_;
    
    my $p; 

    my $T = $tree->ref_terminals(); 
    my $node = $T->[$t]; 
    if ($h==0) {
	if ($options{st} and $node->content->pos eq "-NONE-") {
#	    print STDERR "Skipping argument in trace terminal\n"; 
	    return undef; 
	}
#	print "Found node for $t:$h:$label\n"; 
	return SRL::phrase->new($node->content->id, $node->content->id, $label); 
    }
    else {
	while ($h) {
	    $node = $node->dad; 
	    $h--;
	}
	
	my $s = $node->content->start;
	my $e = $node->content->end;
	if ($options{st}) {
	    while ($s<=$e and ($T->[$s]->content->pos eq "-NONE-")) {
#		print STDERR "Restructuring argument starting in trace terminal\n"; 
		$s++;
	    }
	    while ($s<=$e and $T->[$e]->content->pos eq "-NONE-") {
#		print STDERR "Restructuring argument ending in  trace terminal\n"; 
		$e--;
	    }
	    if ($s>$e) {
#		print STDERR "Skipping NULL argument after eliminating trace terminals\n"; 
		return undef; 
	    }
	}
	return SRL::phrase->new($s, $e, $label); 
    }
}



# checks if phrases in a set P overlap
# returns 1 if two phrases overlap, 0 otherwise
sub test_overlapping {
    my $P = shift; 

    my $p; 
    my @W;
#    print STDERR "test->overlapping : listing phrases\n"; 
    foreach $p ( $P->phrases ) {
#	print STDERR "test->overlapping : (", $p->start, ",", $p->end, ")_", $p->type, "\n"; 
	my $i; 
	for ($i = $p->start; $i<=$p->end; $i++) {
	    $W[$i] and return 1; 
	    $W[$i] = 1; 
	}
    }
#    print STDERR "test->overlapping : no overlapping\n"; 
    return 0; 
}

# checks if argument labels in a set P are unique
# returns 1 if two uniq, 0 otherwise
sub test_arguniq {
    my $P = shift; 

    my $p; 
    my %L; # labels seen in the PAS
    foreach $p ( $P->phrases ) {
	my $t = $p->type;
	if ($t !~ /^L/ and $t !~ /^AM/ and $t !~ /^C/ and $L{$t} ) {
	    return 0; 
	}
	$L{$t} = 1; 
    }
    return 1; 
}


# maps an argument label found in PB annotations into a more convenient one
# also, checks whether the label is OK, and returns undef if label is not OK
sub map_arglabel {
    my $in = shift; 

    my $out; 
    if ($in =~ /^ARG([0-6A])(\-.*)?$/) {
	return "A$1"; 
    }
    elsif ($in =~ /^ARGM(\-[A-Z]+)?(\-[a-z]+)?$/) {
	return "AM$1"; 
    }
    elsif ($in eq "rel" or $in eq "REL") {
	return "V"; 
    }
    elsif (lc($in) eq "support") {
	return "SUP"; 
    }
    elsif ($options{al}) {
	return $in;
    }

    return undef; 

}


# gets a list of phrases
# sorts them and joins contiguous phrases into one
# skips NONE words if necessary 
sub sort_and_join {
    my ($tree, @I) = @_;

    my $T = $tree->ref_terminals(); 

    my @tmp = sort { $a->start <=> $b->start } @I; 

    my @O; # the output list of phrases
    my $c = undef;  # the current phrase
    my $ns = undef; # the start-point of the next phrase for being contiguous with $c
    my $p;         #  iterator
    while ( @tmp ) {
	my $p = shift @tmp; 

	if ($c) {
	    if ($c->end >= $p->start) {
		#  die "Overlapping phrases in discontinuous argument!\n"; 
		return (); 
	    }
	    elsif ($ns == $p->start) { 
		# these phrases are contiguous and must be joined
		$c->set_end($p->end);
		$p = undef; 
	    }	    
	}

	if ($p) {
	    push @O, $p; 
	    $c = $p; 
	}

	if ($c) {
	    $ns = $c->end +1; 
	    if ($options{st}) {
		while ($ns<@{$T} and $T->[$ns]->content->pos eq "-NONE-") {
		    $ns++;
		}
	    }
	}
    }

    return @O; 
}


# Receives a list of PROPS
# Returns a single PROPS, which merges all input PROPS
# The following conditions on the input PROPS must be met:
# - verb and position of the predicate must be the same
# - if two arguments with the same span are found in different PAS, 
#    their label must be the same
# If these conditions are not met, then UNDEF is returned
# This function *does not* check whether args of different PROPS overlap
sub merge_predicates {
    my @PROPS = @_;

    # output predicate
    my $out = shift @PROPS; 


    while ( defined($out) and @PROPS ) {
	my $pas = shift @PROPS; 
	
	($pas->{verb} ne $out->{verb}) and $out = undef and next; 
	($pas->{term} != $out->{term}) and $out = undef and next; 

	my @P = $pas->{args}->phrases; 
	while (defined($out) and @P) {
	    my $p = shift @P; 
	    my $p2 = $out->{args}->phrase($p->start, $p->end); 
	    if ($p2) {
		if ($p2->type ne $p->type) {
		    $out = undef; 
		}
	    }
	    else {
		$out->{args}->add_phrases($p); 
	    }
	}
    }
    return $out;
}



########  Reads PB annotations
sub read_propbank {
    my $pbfile = shift; 

    if ($pbfile =~ /\.gz$/) {
	open PB, "gunzip -c $pbfile |" or die "error opening gzipped pb file! $!\n"; 
    }
    else {
	open PB, "$pbfile" or die "error opening pb file! $!\n"; 
    }
    
    my $PB; 
    
    while ($_ = <PB>) {
	my @pba = split(" ", $_); 
	my $pba = {};
	shift @pba;  # the WSJ file
	$pba->{sent} = shift @pba; 
	$pba->{term} = shift @pba; 
	if ($options{nb}) {
	    ## NOMBANK
	    $pba->{verb} = shift @pba; 
	    $pba->{sense} = shift @pba; 
	    @{$pba->{args}} = @pba; 
	}
	else {
	    ## PROPBANK
	    shift @pba; # annotator 
	    my $verb = shift @pba;
	    $verb =~ /^([^\.]+)\.([^\.]+)$/;
	    $pba->{verb} = $1; 
	    $pba->{sense} = $2; 
	    $pba->{infl} = shift @pba; 
	    @{$pba->{args}} = @pba; 
	}
	push @{$PB->[$pba->{sent}]}, $pba; 
    }
    
    close PB; 

    return $PB; 
}





