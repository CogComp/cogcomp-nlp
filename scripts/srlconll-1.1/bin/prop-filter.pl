#! /usr/bin/perl

use strict; 


use SRL::prop;
use SRL::sentence;



my $n = 0; 
my $s;

my $help = << "end_of_help;";
Usage:  props_file >  prop_filter.pl [filtering conditions] > filtered_props
Filtering Conditions: 
    -type    <RE type>       RegExp on the type
    -max     <max words>     Maximum number of words
    -min     <min words>     Minimum number of words
    -single  [0|1]           The arg is single or not (wrt. C tags)
    -verb    <RE verbs>      RegExp on the verb predicate
    -fverbs  <verbs file>    File containing selected verbs

end_of_help;

if (!@ARGV) {
    print $help;
    exit; 
}


use Getopt::Long; 
my %options;
GetOptions(\%options, 
	   "i",          # 
           "type:s",     # verbose
	   "max:i",      # max siz
	   "min:i",      # min size
	   "single:i",   
	   "verb:s", 
	   "fverbs:s"
           ); 


my %V; 

if ($options{fverbs}) {
    open F, $options{fverbs} or die "File ", $options{fverbs}, ": $!\n"; 
    my $l; 
    while ($l = <F>) {
	my ($v, ) = split(" ", $l); 
	$V{$v} = 1; 
    }
    close F; 
}

if ($options{i}) {
    $s = SRL::sentence->read_from_stream($n, \*STDIN); 
}
else {
    $s = SRL::sentence->read_props($n, GOLD => \*STDIN); 
}

while ($s) {

     my $prop; 

    if (!$s->pred_props)  {
	foreach $prop ( $s->gold_props ) {
	    $s->add_pred_props( SRL::prop->new($prop->verb, $prop->position) ); 
	}
    }
    
    my @GP; 
    map { $GP[$_->position] = $_ } $s->gold_props; 
    
    foreach $prop ( $s->pred_props ) {
	my @Args = $GP[$prop->position]->args;

	if (exists($options{fverbs})) {
	    if (!$V{$prop->verb}) {
		@Args = (); 
	    }
	}

	if (exists($options{verb})) {
	    my $re = $options{verb};
	    if ($prop->verb !~ /$re/) {
		@Args = (); 
	    }
	}

	if (exists($options{type})) {
	    my $re = $options{type};
	    @Args = grep { $_->type =~ /$re/ } @Args;
	}

	if (exists($options{min})) {
	    @Args = grep { ($_->end - $_->start +1) >= $options{min} } @Args;
	}

	if (exists($options{max})) {
	    @Args = grep { ($_->end - $_->start +1) <= $options{max} } @Args;
	}

	if (exists($options{single})) {
	    if ($options{single}) {
		@Args = grep { $_->single  } @Args;
	    }
	    else {
		@Args = grep { !$_->single  } @Args;
	    }	
	}

	$prop->set_args(@Args); 
    }

    if ($options{i}) {
	$s->write_to_stream(\*STDOUT); 
	$s = SRL::sentence->read_from_stream($n, \*STDIN); 
    }
    else {
	$s->write_pred_props(\*STDOUT); 
	$s = SRL::sentence->read_props($n, GOLD => \*STDIN); 
    }

}

