#!/usr/bin/perl

use Getopt::Std;

getopts('n:f');

$numIterations = $opt_n ? $opt_n : 10000;

if (@ARGV < 2) {
    print "usage: [-n <iterations>] [-f] <model1> <model2>\n";
    print "\twhere -f indicates only to compare sentences of length <= 40\n";
    exit 1;
}

$model1name = $ARGV[0];
$model2name = $ARGV[1];

open MODEL1, $model1name or die "couldn't open \"$model1name\"";
open MODEL2, $model2name or die "couldn't open \"$model2name\"";

$recallIdx = 0;
$precisionIdx = 1;
$matchIdx = 2;
$goldIdx = 3;
$producedIdx = 4;

while (<MODEL1>) {
    if (m/([\d.]+\s+){12}/) {
	chop;
	@items = split;
	if ($opt_f && $items[1] > 40) {
	    next;
	}
	@items = @items[3..7];
	push @m1Data, [@items];
    }
}

while (<MODEL2>) {
    if (m/([\d.]+\s+){12}/) {
	chop;
	@items = split;
	if ($opt_f && $items[1] > 40) {
	    next;
	}
	@items = @items[3..7];
	push @m2Data, [@items];
    }
}

close(MODEL1);
close(MODEL2);

@models = (\@m1Data, \@m2Data);


print total(\@m1Data,$producedIdx), "\n";


foreach $i (0 .. $#models) {
    push @recalls, avgRecall($models[$i]);
    push @precisions, avgPrecision($models[$i]);
    $modelNum = $i + 1;
    print "model$modelNum: recall=$recalls[$i], precision=$precisions[$i]\n";
}
# calculate recall of model2 - recall of model1
$recallDiff = $recalls[1] - $recalls[0];
print "model2 recall - model1 recall = $recallDiff\n";
# calculate precision of model2 - precision of model1
$precisionDiff = $precisions[1] - $precisions[0];
print "model2 precision - model1 precision = $precisionDiff\n";

print STDERR "Doing random shuffle $numIterations times.\n";

srand(time() ^ ($$ + ($$ << 15)) );
$randomRecallDiff = 0;
$randomPrecisionDiff = 0;
for $iter (1 .. $numIterations) {
    if ($iter > 1 && $iter % 1000 == 1) {
	print STDERR "Completed ", ($iter - 1), " iterations\n";
    }
    for $i (0 .. $#m1Data) {
	$shuffle = rand;
	if ($shuffle > 0.5) {
	    $tmp = $m1Data[$i];
	    $m1Data[$i] = $m2Data[$i];
	    $m2Data[$i] = $tmp;
	}
    }
    $currRecallDiff = avgRecall(\@m2Data) - avgRecall(\@m1Data);
    if ($recallDiff >= 0 && $currRecallDiff >= $recallDiff) {
	$randomRecallDiff++;
    }
    elsif ($recallDiff < 0 && $currRecallDiff <= $recallDiff) {
	$randomRecallDiff++;
    }
    $currPrecisionDiff = avgPrecision(\@m2Data) - avgPrecision(\@m1Data);
    if ($precisionDiff >= 0 && $currPrecisionDiff >= $precisionDiff) {
	$randomPrecisionDiff++;
    }
    elsif ($precisionDiff < 0 && $currPrecisionDiff <= $precisionDiff) {
	$randomPrecisionDiff++;
    }
}

print "number of random recall diferences equal to or greater than\n\t",
    "original observed difference: ", $randomRecallDiff, "\n";
print "number of random precision diferences equal to or greater than\n\t",
    "original observed difference: ", $randomPrecisionDiff, "\n";

print ("p-value for recall diff: ",
       (($randomRecallDiff + 1)/($numIterations + 1)), "\n");
print ("p-value for precision diff: ",
       (($randomPrecisionDiff + 1)/($numIterations + 1)), "\n");

sub avgRecall {
    return avg(shift(@_),$recallIdx,$goldIdx);
}

sub avgPrecision {
    return avg(shift(@_),$precisionIdx,$producedIdx);
}

sub avg {
    my ($data,$idx,$weightIdx) = @_;
    my $total = total($data,$weightIdx);
    my $weight = 0, $avg = 0;
    for $i (0 .. $#$data) {
	$weight = $data->[$i][$weightIdx] / $total;
	$avg += $data->[$i][$idx] * $weight;
    }
    return $avg;
}

sub total {
    my ($data,$idx) = @_;
    my $total = 0;
    for $i (0 .. $#$data) {
	$total += $data->[$i][$idx];
    }
    return $total;
}
