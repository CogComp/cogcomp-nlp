#!/usr/bin/perl -w
# srl-baseline04: baseline result generator for CoNLL-2004 shared task
# usage: paste -d' ' sample.words sample.synt.upc sample.props | srl-baseline04.pl
# 20040112 erikt@uia.ua.ac.be
# 20040223 current version 
# 20040301 modified by carreras@lsi.upc.es so as to accept
#           a target verb column (without argument annotations) as final column
# 20050216 modified by carrears@lsi.upc.edu to output end tags without type
# 20050304 carreras@lsi.upc.edu : column numbers changed 


if (@ARGV) {
    print "Usage: paste -d' ' devel/words/devel.24.words devel/synt.upc/devel.24.synt.upc devel/props/devel.24.props | srl-baseline04.pl";
    exit; 
}


@buffer = ();
while (<STDIN>) {
   $line = $_;
   chomp($line);
   $line =~ s/\s+$//;
   if ($line !~ /^\s*$/) {
      push(@buffer,$line);
   } else {
      &process(@buffer);
      @buffer = ();
   }
}
exit(0);

sub process {
   my ($i,$j,$k,
       $beSeen,$line,$nbrOfVerbs,$tag,$verbSeen,
       @buffer,@chunk,@fields,@pos,@verb,@verbIndex,@words);

   @buffer = @_;
   if ($#buffer < 0) { return(); }
   else {
      @chunk = ();
      @pos = ();
      @verb = ();
      @verbIndex = ();
      $nbrOfVerbs = 0; 
      for ($i=0;$i<=$#buffer;$i++) {
         @fields = split(/\s+/,$buffer[$i]);
         push(@words,$fields[0]);
         push(@pos,$fields[1]);
         push(@chunk,$fields[2]);
         push(@verb,$fields[4]);
         if ($verb[$#verb] ne "-") { push(@verbIndex,$#verb); $nbrOfVerbs++;}
         $buffer[$i] = "-";
      }
     for ($k=0;$k<$nbrOfVerbs;$k++) {
         if ($k > $#verbIndex) { die "cannot happen: $#verbIndex $k\n"; }
         $buffer[$verbIndex[$k]] =~ s/^-/$verb[$verbIndex[$k]]/; 
         for ($i=0;$i<=$#buffer;$i++) { $buffer[$i] .= " *"; }
         for ($i=0;$i<=$#buffer;$i++) {
            if ($buffer[$i] =~ /^\w[\w\-]*\s/) {
               # identify (V*): 
               # {main verb + successive particles}
               $buffer[$i] =~ s/\s(\S+)$/ (V$1/;
               $j = $i;
               while ($j < $#buffer and $chunk[$j+1] =~ /-PRT$/) { $j++; }
               $buffer[$j] =~ s/\s(\S+)$/ $1)/;
               # identify (AM-NEG*): 
               # {not or n't} followed by verbs and main verb
               $j = $i-1;
               $beSeen = 0;
               while ($pos[$j] =~ /^VB/) {
                   if ($words[$j] =~ /^(be|am|is|are|was|were|been|being)$/ and
                       $words[$i] !~ /ing$/i) { 
                      $beSeen = 1;
                  }
                  $j--; 
               }
               if ($words[$j] =~ /^(not|n't)$/) {   #'
                  $buffer[$j] =~ s/\s(\S+)$/ (AM-NEG$1)/;
                  $j--;
               }
               while ($pos[$j] =~ /^VB/) {
                   if ($words[$j] =~ /^(be|am|is|are|was|were|been|being)$/ and
                       $words[$i] !~ /ing$/i) { 
                      $beSeen = 1;
                  }
                  $j--; 
               }
               # identify (AM-MOD*AM-MOD)
               # {word tagged MD} followed by AM-NEG, verbs and main verb
               if ($pos[$j] eq "MD") {
                  $buffer[$j] =~ s/\s(\S+)$/ (AM-MOD$1)/;
               }
               # identify (A0*A0):
               # {first NP} before main verb
               $tag = $beSeen ? "A1" : "A0"; 
               $j = $i-1;
               while ($j >= 0 and $pos[$j] =~ /[A-Z]/ and $pos[$j] ne "CC" and
                      ($chunk[$j] !~ /-NP$/ or $words[$j] =~ /^(that|which|who)$/)) {
                  if ($words[$j] =~ /^(that|which|who)$/) { 
                     $buffer[$j] =~ s/\s(\S+)$/ (R-$tag$1)/;
                  }
                  $j--; 
               }
               if ($j >= 0 and $chunk[$j] =~ /-NP$/) {
                  $buffer[$j] =~ s/\s(\S+)$/ $1)/;
                  while ($j > 0 and $chunk[$j] ne "B-NP" and
                         $chunk[$j-1] =~ /-NP$/) { $j--; }
                  $buffer[$j] =~ s/\s(\S+)$/ ($tag$1/;
               }
               # identify (A1*A1)
               $j = $i+1;
               $tag = $beSeen ? "A0" : "A1"; 
               while ($j <= $#buffer and $words[$j] !~ /^(that|which|who)$/ and
                      $pos[$j] !~ /^(CC|IN|TO)$/ and $pos[$j] =~ /[A-Z]/ and 
                      $chunk[$j] !~ /-NP$/) { $j++; }
               if ($j <= $#buffer and $chunk[$j] =~ /-NP$/) {
                  $buffer[$j] =~ s/\s(\S+)$/ ($tag$1/;
                  while ($j < $#buffer and $chunk[$j+1] ne "B-NP" and
                         $chunk[$j+1] =~ /-NP$/) { $j++; }
                  $buffer[$j] =~ s/\s(\S+)$/ $1)/;
               }
               $buffer[$verbIndex[$k]] =~ s/^\S+\s/- /; 
            }
         }
      }
      for ($k=0;$k<$nbrOfVerbs;$k++) {
         $buffer[$verbIndex[$k]] =~ s/^-/$verb[$verbIndex[$k]]/;
      }
      for ($i=0;$i<=$#buffer;$i++) { print "$buffer[$i]\n"; }
   }
   print "\n";
}
