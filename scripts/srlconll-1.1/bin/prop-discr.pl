#! /usr/bin/perl 

##################################################################
#
#
##################################################################


use strict; 

use SRL::sentence; 
use SRL::phraseset; 
use SRL::prop;


############################################################
#  A r g u m e n t s   a n d   H e l p 

use Getopt::Long; 
my %options;
GetOptions(\%options, 

           ); 


my $script = "prop_discr.pl"; 
my $help = << "end_of_help;";
Usage:   prop-discr.pl <A prop file> <B prop file> <output_label>

Generates three files containing propositions: 
  <output_label>.AB   : props found in A and B
  <output_label>.AnoB : props found in A but not in B 
  <output_label>.BnoA : props found in B but not in A 

end_of_help;


############################################################
#  M A I N   P R O G R A M 


my $ns = 0;        # number of sentence
my $ntargets = 0;  # number of target verbs

# open files

if (@ARGV != 3) {
    print $help;
    exit; 
}

my $goldfile = shift @ARGV;
my $predfile = shift @ARGV;
my $ofiles = shift @ARGV; 


if ($goldfile =~ /\.gz/) {
    open GOLD, "gunzip -c $goldfile |" or die "$script: could not open gzipped file of gold props ($goldfile)! $!\n"; 
}
else {
    open GOLD, $goldfile or die "$script: could not open file of gold props ($goldfile)! $!\n"; 
}
if ($predfile =~ /\.gz/) {
    open PRED, "gunzip -c $predfile |" or die "$script: could not open gzipped file of predicted props ($predfile)! $!\n"; 
}
else {
    open PRED, $predfile or die "$script: could not open file of predicted props ($predfile)! $!\n"; 
}

open OK, ">$ofiles.AB" or die "Can't open file ($ofiles.AB) for OK: $!\n"; 
open MS, ">$ofiles.AnoB" or die "Can't open file ($ofiles.AnoB) for MS: $!\n"; 
open OP, ">$ofiles.BnoA" or die "Can't open file ($ofiles.BnoA) for OP: $!\n"; 


##
# read and evaluate propositions, sentence by sentence

my $s = SRL::sentence->read_props($ns, GOLD => \*GOLD, PRED => \*PRED); 

while ($s) {

    my $prop; 

    my (@G, @P, $i); 

    my (@OK, @MS, @OP); 
    
    map { $G[$_->position] = $_ } $s->gold_props; 
    map { $P[$_->position] = $_ } $s->pred_props; 
    
    for($i=0; $i<@G; $i++) {
	my $gprop = $G[$i]; 
	my $pprop = $P[$i]; 

	if ($pprop and !$gprop) {	    
	    print STDERR "WARNING : sentence $ns : verb ", $pprop->verb, " at position ", $pprop->position, 
	    " : found predicted prop without its gold reference! Skipping prop!\n"; 
	}
	elsif ($gprop) {
	    if (!$pprop) {
		print STDERR "WARNING : sentence $ns : verb ", $gprop->verb, " at position ", $gprop->position, 
		" : missing predicted prop! Counting all arguments as missed!\n"; 
		$pprop = SRL::prop->new($gprop->verb, $gprop->position); 
	    }
	    elsif ($gprop->verb ne $pprop->verb) {
		print STDERR "WARNING : sentence $ns : props do not match : expecting ", 
		$gprop->verb, " at position ", $gprop->position, 
		", found ", $pprop->verb, " at position ", $pprop->position, "! Counting all gold arguments as missed!\n";
		$pprop = SRL::prop->new($gprop->verb, $gprop->position);
	    }
       	
	    $ntargets++;
	    my ($ok, $ms, $op) = discriminate_propositions($gprop, $pprop); 

	    defined($ok) and push @OK, $ok; 
	    defined($ms) and push @MS, $ms; 
	    defined($op) and push @OP, $op; 

	}
    }

    # write'em
    $s->write_to_stream(\*OK, PROPS => \@OK); 
    $s->write_to_stream(\*OP, PROPS => \@OP); 
    $s->write_to_stream(\*MS, PROPS => \@MS); 



    $ns++; 
    $s = SRL::sentence->read_props($ns, GOLD => \*GOLD, PRED => \*PRED); 

}



# end of main program
#####################

############################################################
#  S U B R O U T I N E S


# evaluates a predicted proposition wrt the gold correct proposition
# returns a hash with the following keys
#   ok  :  number of correctly predicted args
#   ms  :  number of missed args
#   op  :  number of over-predicted args
#   T   :  a hash indexed by argument types, where
#           each value is in turn a hash of {ok,ms,op} numbers
#   E   :  a hash indexed by excluded argument types, where
#           each value is in turn a hash of {ok,ms,op} numbers
sub discriminate_propositions {
    my ($gprop, $pprop) = @_;

    my $o = $gprop->discriminate_args($pprop); 
    
    my ($ok, $ms, $op) = (undef, undef, undef); 

    if (@{$o->{ok}}) {
	$ok = SRL::prop->new($pprop->verb, $pprop->position); 
	$ok->add_args(@{$o->{ok}}); 
    }
    if (@{$o->{op}}) {
	$op = SRL::prop->new($pprop->verb, $pprop->position); 
	$op->add_args(@{$o->{op}}); 
    }
    if (@{$o->{ms}}) {
	$ms = SRL::prop->new($pprop->verb, $pprop->position); 
	$ms->add_args(@{$o->{ms}}); 
    }

    return ($ok, $ms, $op); 
}


# end of script
###############





















