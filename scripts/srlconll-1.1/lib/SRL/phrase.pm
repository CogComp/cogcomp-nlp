##################################################################
#
#  Package   p h r a s e  :  a generic phrase
#
#  January 2004
#
#  This class represents generic phrases. 
#  A phrase is a sequence of contiguous words in a sentence.
#  A phrase is identified by the positions of the start/end words
#  of the sequence that the phrase spans.
#  A phrase has a type. 
#  A phrase may contain a list of internal subphrases, that is, 
#  phrases found within the phrase. Thus, a phrase object is seen
#  eventually as a hierarchical structure. 
#  
#  A syntactic base chunk is a phrase with no internal phrases.
#  A clause is a phrase which may have internal phrases
#  A proposition argument is implemented as a special class which
#  inherits from the phrase class.
#
##################################################################

use strict; 

package SRL::phrase; 

# Constructor: creates a new phrase
# Parameters: start position, end position and type
sub new {
    my $pkg = shift;

    my $ph = [];

    # start word index
    $ph->[0] = (@_) ? shift : undef;
    # end word index
    $ph->[1] = (@_) ? shift : undef;
    # phrase type
    $ph->[2] = (@_) ? shift : undef;
    # 
    @{$ph->[3]} = ();

    return bless $ph, $pkg; 
}

# returns the start position of the phrase
sub start {
    my $ph = shift;
    return $ph->[0];
}

# initializes the start position of the phrase
sub set_start {
    my $ph = shift;
    $ph->[0] = shift;
}

# returns the end position of the phrase
sub end {
    my $ph = shift;
    return $ph->[1];
}

# initializes the end position of the phrase
sub set_end {
    my $ph = shift;
    $ph->[1] = shift;
}

# returns the type of the phrase
sub type {
    my $ph = shift;
    return $ph->[2];
}

# initializes the type of the phrase
sub set_type {
    my $ph = shift;
    $ph->[2] = shift;
}

# returns the subphrases of the current phrase
sub phrases {
    my $ph = shift;
    return @{$ph->[3]};
}

# adds phrases as subphrases
sub add_phrases {
    my $ph = shift;
    push @{$ph->[3]}, @_;
}

# initializes the set of subphrases
sub set_phrases {
    my $ph = shift;
    @{$ph->[3]} = @_;
}


# depth first search
# returns the phrases rooted int the current phrase in dfs order 
sub dfs {
    my $ph = shift;
    return ($ph, map { $_->dfs } $ph->phrases);
}


# generates a string representing the phrase (and subphrases if arg is a TRUE value), for printing
sub to_string {
  my $ph = shift;
  my $rec = ( @_ ) ? shift : 1; 

  my $str = "(" . $ph->start . " ";

  $rec and  map { $str .= $_->to_string." " } $ph->phrases;

  $str .= $ph->end . ")";
  if (defined($ph->type)) {
      $str .= "_".$ph->type;
  }
  return $str;
}


1;

