#!/usr/bin/perl
#
# This software is released under the University of Illinois/Research and Academic Use License. See
# the LICENSE file in the root folder for details. Copyright (c) 2016
#
# Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
# http://cogcomp.org/
#


####
# extract a set of files of POS data in parenthesis "(<TAG> <WORD>)..." 
#   format for use with CCG POS Tagger from the Penn Treebank
#   parse tree files.
#
# extracts one file per section of the WSJ, with one sentence per line.
#
# PROBLEMS
# 1.  actually have to count parens because WSJ doesn't demarcate sentence
#     boundaries consistently.
#
# 2.  Now the modified regex will remove all right parentheses in the word 
#	  to extract the word. This works because the current version of WSJ 
#	  (LDC2015T13) doesn't include right parathesis as a word. So it is 
#	  easier to just remove all right parentheses. For further ptb releases, 
#	  this script might need further modification (more possibly rewrite). 
#	  

use strict;
use Carp;


# wsj-dir must be the path to the 'parsed/' directory of a Penn Treebank
#   distribution.
croak "Usage: $0 wsj-dir out-dir" unless @ARGV == 2;

my $wsjDir = shift;
my $outDir = shift;

my @wsjSections = `ls $wsjDir`;

foreach my $section ( @wsjSections )
  {
    chomp $section;

    next unless ( -d "$wsjDir/$section" );
    my $outFile = "wsj-$section";
    
    my @files = `ls $wsjDir/$section`;

    open ( OUT, ">$outDir/$outFile" ) or croak "ERROR: $0: Can't open output file '$outDir/$outFile': $!\n";

    foreach my $file ( @files )
      {
	chomp $file;
	next if ( $file =~ /\.html/ );

	my $parenData = &generateParenFormat( "$wsjDir/$section/$file" );

	print OUT $parenData;
      }

    close OUT;
  }


exit;

sub generateParenFormat()
  {
    my $file = shift;

    open( IN, "<$file" ) or croak "ERROR: $0: can't open input file '$file': $!\n";

    my $output = "";
    my $currentLine = "";
    my $nestingLevel = 0;

    while( my $line = <IN> )
      {
	next if ( $line =~ /^=+/ );
	
#	if ( $line =~ /^\( \(S\s*$/ )

	my @bits = split( /\s+/, $line );
	    
	my $isOpened = 0;
	my $tag = "";
	my $word = "";
	
	foreach my $bit ( @bits )
	  {

## figure out depth in tree (needed for sentence boundaries)
	    if ( $bit =~ /^(\(+)/ )
	      {
		$nestingLevel += length( $1 );
	      }
	    elsif ( $bit =~ /(\)+)$/ )
	      {
		$nestingLevel -= length( $1 )
	      }
	    #		    print STDERR "## bit is '$bit'; nestingLevel is '$nestingLevel'\n";

	    if ( $bit =~ /^\((.+)$/ )
	      {
		$tag = $1;

#		    print STDERR "## tag is '$tag'...\n";

		if ( $tag ne '-NONE-' )
		  {
		    #			print STDERR "## tag is fine, so now opened...\n";
		    $isOpened = 1;
		  }
		else
		  {
		    $isOpened = 0;
		  }
	      }
	    elsif ( $bit =~ /^(\S+)\)$/ )
	      {
		$word = $1;
#		    print STDERR "## word is '$word'...\n";

		## Delete every right parenthesis in the word
		$word =~ s/\)+//;

		if ( $isOpened )
		  {
		    if ( "" ne $currentLine )
		      {
			$currentLine .= " ";
		      }

#			print STDERR "## isOpened, so printing output '($tag $word)'.\n";
		    $currentLine .= "($tag $word)";
		  }
		
		$isOpened = 0;
	      }
	  }
	if ( $nestingLevel == 0 )
	  {
	    if ( "" ne $currentLine )
	      {
		$output .= $currentLine . "\n";
		$currentLine = "";
	      }
	  }
      }


    close IN;

    return $output;

  }
