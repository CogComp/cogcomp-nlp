##################################################################
#
#  Package    a r g  :  An argument
#
#  January 2004
#
#  This class inherits from the class "phrase".
#  An argument is identified by start-end positions of the 
#  string spanned by the argument in the sentence.
#  An argument has a type. 
#
#  Most of the arguments consist of a single phrase; in this
#  case the argument and the phrase objects are the same.
#
#  In the special case of discontinuous arguments, the argument
#  is an "arg" object which contains a number of phrases (one
#  for each discontinuous piece). Then, the argument spans from
#  the start word of its first phrase to the end word of its last 
#  phrase. As for the composing phrases, the type of the first one
#  is the type of the argument, say A, whereas the type of the 
#  subsequent phrases is "C-A" (continuation tag). 
# 
##################################################################

package SRL::arg; 

use SRL::phrase; 
use strict; 

#push @SRL::arg::ISA, 'SRL::phrase'; 
use base qw(SRL::phrase); 


# Constructor "new" inherited from SRL::phrase

# Checks whether the argument is single (returning true)
# or discontinuous (returning false)
sub single {
    my ($a) = @_;
    return scalar(@{$a->[3]}==0);
}

# Generates a string representing the argument
sub to_string {
    my $a = shift; 

    my $s = $a->type."_(" . $a->start . " ";
    map { $s .= $_->to_string." " } $a->phrases;
    $s .= $a->end . ")";

    return $s;
}


1;









