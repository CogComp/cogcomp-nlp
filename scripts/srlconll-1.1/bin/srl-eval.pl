#! /usr/bin/perl 

##################################################################
#
#  srl-eval.pl : evaluation program for the CoNLL-2005 Shared Task
#
#  Authors : Xavier Carreras and Lluis Marquez
#  Contact : carreras@lsi.upc.edu
#
#  Created : January 2004
#  Modified: 
#      2005/04/21  minor update; for perl-5.8 the table in LateX
#                  did not print correctly
#      2005/02/05  minor updates for CoNLL-2005
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
           "latex",     # latex output
	   "C",         # confusion matrix
	   "noW"
           ); 


my $script = "srl-eval.pl"; 
my $help = << "end_of_help;";
Usage:   srl-eval.pl <gold props> <predicted props>
Options: 
     -latex      Produce a results table in LaTeX
     -C          Produce a confusion matrix of gold vs. predicted argments, wrt. their role

end_of_help;


############################################################
#  M A I N   P R O G R A M 


my $ns = 0;        # number of sentence
my $ntargets = 0;  # number of target verbs
my %E;             # evaluation results
my %C;             # confusion matrix

my %excluded = ( V => 1); 

##

# open files

if (@ARGV != 2) {
    print $help;
    exit; 
}

my $goldfile = shift @ARGV;
my $predfile = shift @ARGV;

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


##
# read and evaluate propositions, sentence by sentence

my $s = SRL::sentence->read_props($ns, GOLD => \*GOLD, PRED => \*PRED); 

while ($s) {

    my $prop; 

    my (@G, @P, $i); 
    
    map { $G[$_->position] = $_ } $s->gold_props; 
    map { $P[$_->position] = $_ } $s->pred_props; 
    
    for($i=0; $i<@G; $i++) {
	my $gprop = $G[$i]; 
	my $pprop = $P[$i]; 

	if ($pprop and !$gprop) {	    
	    !$options{noW} and print STDERR "WARNING : sentence $ns : verb ", $pprop->verb, 
	    " at position ", $pprop->position, " : found predicted prop without its gold reference! Skipping prop!\n"; 
	}
	elsif ($gprop) {
	    if (!$pprop) {
		!$options{noW} and print STDERR "WARNING : sentence $ns : verb ", $gprop->verb, 
		" at position ", $gprop->position, " : missing predicted prop! Counting all arguments as missed!\n"; 
		$pprop = SRL::prop->new($gprop->verb, $gprop->position); 
	    }
	    elsif ($gprop->verb ne $pprop->verb) {
		!$options{noW} and  print STDERR "WARNING : sentence $ns : props do not match : expecting ", 
		$gprop->verb, " at position ", $gprop->position, 
		", found ", $pprop->verb, " at position ", $pprop->position, "! Counting all gold arguments as missed!\n";
		$pprop = SRL::prop->new($gprop->verb, $gprop->position);
	    }
       	
	    $ntargets++;
	    my %e = evaluate_proposition($gprop, $pprop); 


	    # Update global evaluation results

	    $E{ok} += $e{ok};
	    $E{op} += $e{op};
	    $E{ms} += $e{ms};
	    $E{ptv} += $e{ptv};

	    my $t; 
	    foreach $t ( keys %{$e{T}} ) {
		$E{T}{$t}{ok} += $e{T}{$t}{ok};
		$E{T}{$t}{op} += $e{T}{$t}{op};
		$E{T}{$t}{ms} += $e{T}{$t}{ms};
	    }
	    foreach $t ( keys %{$e{E}} ) {
		$E{E}{$t}{ok} += $e{E}{$t}{ok};
		$E{E}{$t}{op} += $e{E}{$t}{op};
		$E{E}{$t}{ms} += $e{E}{$t}{ms};
	    }

	    if ($options{C}) {
		update_confusion_matrix(\%C, $gprop, $pprop); 
	    }
	}
    }

    $ns++; 
    $s = SRL::sentence->read_props($ns, GOLD => \*GOLD, PRED => \*PRED); 

}


# Print Evaluation results
my $t; 

if ($options{latex}) {
#    print '\begin{table}[t]', "\n"; 
#    print '\centering', "\n"; 
    print '\begin{tabular}{|l|r|r|r|}\cline{2-4}',  "\n"; 
    print '\multicolumn{1}{c|}{datalabel}', "\n"; 
    print '           & Precision & Recall & F$_{\beta=1}$', '\\\\', "\n", '\hline', "\n";  #'

    printf("%-18s & %6.2f\\%% & %6.2f\\%% & %6.2f\\\\\n", "Overall", precrecf1($E{ok}, $E{op}, $E{ms})); 
    print '\hline', "\n"; 

    foreach $t ( sort keys %{$E{T}} ) {
	printf("%-18s & %6.2f\\%% & %6.2f\\%% & %6.2f\\\\\n", "\\texttt{".$t."}", precrecf1($E{T}{$t}{ok}, $E{T}{$t}{op}, $E{T}{$t}{ms})); 
    }
    print '\hline', "\n"; 
    
    if (%excluded) {
	print '\hline', "\n"; 
	foreach $t ( sort keys %{$E{E}} ) {
	    printf("%-18s & %6.2f\\%% & %6.2f\\%% & %6.2f\\\\\n", "\\texttt{".$t."}", precrecf1($E{E}{$t}{ok}, $E{E}{$t}{op}, $E{E}{$t}{ms})); 
	}
	print '\hline', "\n"; 
    }
    
    print '\end{tabular}', "\n"; 
#    print '\end{table}', "\n"; 
}
else {
    printf("Number of Sentences    :      %6d\n", $ns); 
    printf("Number of Propositions :      %6d\n", $ntargets); 
    printf("Percentage of perfect props : %6.2f\n",($ntargets>0 ? 100*$E{ptv}/$ntargets : 0)); 
    print "\n"; 
    
    printf("%10s   %6s  %6s  %6s   %6s  %6s  %6s\n", "", "corr.", "excess", "missed", "prec.", "rec.", "F1"); 
    print "------------------------------------------------------------\n"; 
    printf("%10s   %6d  %6d  %6d   %6.2f  %6.2f  %6.2f\n", 
	   "Overall", $E{ok}, $E{op}, $E{ms}, precrecf1($E{ok}, $E{op}, $E{ms})); 
#    print "------------------------------------------------------------\n"; 
    print "----------\n"; 
    
#    printf("%10s   %6d  %6d  %6d   %6.2f  %6.2f  %6.2f\n", 
#	   "all - {V}", $O2{ok}, $O2{op}, $O2{ms}, precrecf1($O2{ok}, $O2{op}, $O2{ms})); 
#    print "------------------------------------------------------------\n"; 
    
    foreach $t ( sort keys %{$E{T}} ) {
	printf("%10s   %6d  %6d  %6d   %6.2f  %6.2f  %6.2f\n", 
	       $t, $E{T}{$t}{ok}, $E{T}{$t}{op}, $E{T}{$t}{ms}, precrecf1($E{T}{$t}{ok}, $E{T}{$t}{op}, $E{T}{$t}{ms})); 
    }
    print "------------------------------------------------------------\n"; 

   foreach $t ( sort keys %{$E{E}} ) {
	printf("%10s   %6d  %6d  %6d   %6.2f  %6.2f  %6.2f\n", 
	       $t, $E{E}{$t}{ok}, $E{E}{$t}{op}, $E{E}{$t}{ms}, precrecf1($E{E}{$t}{ok}, $E{E}{$t}{op}, $E{E}{$t}{ms})); 
    }
    print "------------------------------------------------------------\n"; 
}


# print confusion matrix
if ($options{C}) {

    my $k; 

    # Evaluation of Unlabelled arguments
    my ($uok, $uop, $ums, $uacc) = (0,0,0,0); 
    foreach $k ( grep { $_ ne "-NONE-" && $_ ne "V" } keys %C ) {	
	map { $uok += $C{$k}{$_} } grep { $_ ne "-NONE-" && $_ ne "V" } keys %{$C{$k}};
	$uacc += $C{$k}{$k};
	$ums += $C{$k}{"-NONE-"};
    }
    map { $uop += $C{"-NONE-"}{$_} } grep { $_ ne "-NONE-" && $_ ne "V" } keys %{$C{"-NONE-"}};

    print "--------------------------------------------------------------------\n"; 
    printf("%10s   %6s  %6s  %6s   %6s  %6s  %6s  %6s\n", "", "corr.", "excess", "missed", "prec.", "rec.", "F1", "lAcc"); 
    printf("%10s   %6d  %6d  %6d   %6.2f  %6.2f  %6.2f  %6.2f\n", 
	   "Unlabeled", $uok, $uop, $ums, precrecf1($uok, $uop, $ums), 100*$uacc/$uok); 
    print "--------------------------------------------------------------------\n"; 



    print "\n---- Confusion Matrix: (one row for each correct role, with the distribution of predictions)\n"; 

    my %AllKeys; 
    map { $AllKeys{$_} = 1 } map { $_, keys %{$C{$_}} } keys %C;
    my @AllKeys = sort keys %AllKeys; 


    
    my $i = -1; 
    print "             ";
    map { printf("%4d ", $i); $i++} @AllKeys;
    print "\n";
    $i = -1;
    foreach $k ( @AllKeys ) {
	printf("%2d: %-8s ", $i++, $k); 
	map { printf("%4d ", $C{$k}{$_}) } @AllKeys;
	print "\n"; 
    }


    my ($t1,$t2); 
    foreach $t1 ( sort keys %C ) {
	foreach $t2 ( sort keys %{$C{$t1}} ) {
#	    printf("    %-6s vs %-6s :  %-5d\n", $t1, $t2, $C{$t1}{$t2});
	}
    }    
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
sub evaluate_proposition {
    my ($gprop, $pprop) = @_;

    my $o = $gprop->discriminate_args($pprop); 
    
    my %e; 

    my $a; 
    foreach $a (@{$o->{ok}}) {
	if (!$excluded{$a->type}) {
	    $e{ok}++;
	    $e{T}{$a->type}{ok}++;
	}
	else {
	    $e{E}{$a->type}{ok}++;
	}
    }
    foreach $a (@{$o->{op}}) {
	if (!$excluded{$a->type}) {
	    $e{op}++;
	    $e{T}{$a->type}{op}++;
	}
	else {
	    $e{E}{$a->type}{op}++;
	}
    }
    foreach $a (@{$o->{ms}}) {
	if (!$excluded{$a->type}) {
	    $e{ms}++;
	    $e{T}{$a->type}{ms}++;
	}
	else {
	    $e{E}{$a->type}{ms}++;
	}
    }

    $e{ptv} = (!$e{op} and !$e{ms}) ? 1 : 0; 
    
    return %e; 
}


# computes precision, recall and F1 measures
sub precrecf1 {
    my ($ok, $op, $ms) = @_;

    my $p = ($ok + $op > 0) ? 100*$ok/($ok+$op) : 0; 
    my $r = ($ok + $ms > 0) ? 100*$ok/($ok+$ms) : 0; 

    my $f1 = ($p+$r>0) ? (2*$p*$r)/($p+$r) : 0; 

    return ($p,$r,$f1); 
}




sub update_confusion_matrix {
    my ($C, $gprop, $pprop) = @_;

    my $o = $gprop->discriminate_args($pprop, 0); 

    my $a; 
    foreach $a ( @{$o->{ok}} ) {
	my $g = shift @{$o->{eq}};
	$C->{$g->type}{$a->type}++;
    }
    foreach $a ( @{$o->{ms}} ) {
	$C->{$a->type}{"-NONE-"}++;
    }
    foreach $a ( @{$o->{op}} ) {
	$C->{"-NONE-"}{$a->type}++;
    }
}


# end of script
###############





















