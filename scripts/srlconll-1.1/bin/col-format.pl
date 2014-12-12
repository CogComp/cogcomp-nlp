#! /usr/bin/perl

use strict; 
use SRL::sentence; 
use SRL::word; 
use SRL::phraseset; 


my $help = << "EOH";
col-format : changes the format of a specified column
  Usage:    cat mycolfile | col-format.pl -2 -i bio -o se
  Options:
    -N            column number (starts at 0; default: no column) 
    -i  bio|se    input format  (default: bio)
    -o  bio|se    output format (default: se)
    -P            do NOT print pretty columns (faster)

EOH

my $ncol = undef; 
my $iformat = "bio"; 
my $oformat = "se"; 
my $pretty = 1; 

while (@ARGV) {
    $_ = shift @ARGV; 
    if ($_ =~ /^\-(\d+)/) {
	$ncol = $1;
    }
    elsif ($_ eq "-i") {
	$iformat = shift @ARGV; 
	$iformat =~ /^(bio|se)$/ or die "Bad input format for -i option ($iformat)\n"; 
    }
    elsif ($_ eq "-o") {
	$oformat = shift @ARGV; 
	$oformat =~ /^(bio|se)$/ or die "Bad output format for -o option ($oformat)\n"; 
    }
    elsif ($_ eq "-P") {
	$pretty = 0; 
    }
    else {
	print $help; 
	exit; 
    }
}


my $cols = SRL::sentence::read_columns(\*STDIN); 


while (@$cols) {


    my $c;
    if (defined($ncol)) {
	$c = $cols->[$ncol];
    }
    my $l; 
    if ($c) {
	$l = scalar(@$c); 

	# initialize ps
	my $ps = SRL::phrase_set->new(); 
	if ($iformat eq "se") {
	    $ps->load_SE_tagging(@$c);
	}
	elsif ($iformat eq "bio") {
	    $ps->load_IOB1_tagging(@$c);
	}

	my @otags;
	if ($oformat eq "bio") {
	    @otags = $ps->to_IOB2_tagging($l); 
	}
	elsif ($oformat eq "se") {
	    my ($stags,$etags) = $ps->refs_start_end_tags($l); 
	    @otags = map { sprintf("%-s*%s",$stags->[$_],$etags->[$_]) } (0 .. $l-1); 
	}

	$cols->[$ncol] = \@otags; 
    }
    else {
	$c = $cols->[0];
	$l = scalar(@$c); 	
    }

    my $i;

    # pretty columns
    if ($pretty) {
	
	SRL::sentence::reformat_columns($cols); 

#	for ($i=0;$i<scalar(@$cols);$i++) {
#	    $c = col_pretty_print($cols->[$i]); 
#	    $cols->[$i] = $c; 
#	}
	
    }

    # finally, print columns word by word
    for ($i=0;$i<$l;$i++) {
	print join(" ",  map { $_->[$i] } @$cols), "\n"; 
    }
    print "\n"; 
        
    $cols = SRL::sentence::read_columns(\*STDIN); 
}



sub col_pretty_print {    
    my $col = shift @_;

    (!@$col) and return undef; 

    my (@oc,$i); 
    if ($col->[0] =~ /\*/) {
	
	# Start-End
	my (@s,@e,$t,$ms,$me); 
	$ms = 2; $me = 2; 
	for ($i=0; $i<@$col; $i++) {
	    $col->[$i] =~ /^(.*\*)(.*)$/ or die "Sanity check.";
	    $s[$i] = $1; 
	    $e[$i] = $2; 
	    if (length($s[$i]) > $ms) {
		$ms = length($s[$i]);
	    }
	    if (length($e[$i]) > $me) {
		$me = length($e[$i]);
	    }
	}
#	print "M $ms $me\n"; 
	my $f = "%".($ms+2)."s%-".($me+2)."s";
	@oc = map { sprintf($f, $s[$_], $e[$_]) } (0 .. $#$col);
    }
    else {
	
	# Tokens or Taks
	my $l; 
	map { (length($_)>$l) and ($l=length($_)) } @$col;
	my $f = "%-".($l+1)."s";
	@oc = map { sprintf($f, $_) } @$col;
    }    
    return \@oc; 
}
