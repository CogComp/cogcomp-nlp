##################################################################
#
#  Package   w o r d  :  a word
#
#  April 2004
#
#  A word, containing id (position in sentence), form and PoS tag
#
##################################################################

use strict; 

package SRL::word; 

# Constructor: creates a new word
# Parameters: id (position), form and PoS tag
sub new {
    my ($pkg, @fields) = @_;

    my $w = []; 

    $w->[0] = shift @fields;  # id (position in sentence)
    $w->[1] = shift @fields;  # form
    $w->[2] = shift @fields;  # PoS
    
    return bless $w, $pkg; 
}

# returns the id of the word
sub id {
    my $w = shift;
    return $w->[0];
}

# returns the form of the word
sub form {
    my $w = shift;
    return $w->[1];
}

# returns the PoS tag of the word
sub pos {
    my $w = shift;
    return $w->[2];
}

sub to_string {
    my $w = shift; 
    return "w@".$w->[0].":".$w->[1].":".$w->[2];
}

1;





