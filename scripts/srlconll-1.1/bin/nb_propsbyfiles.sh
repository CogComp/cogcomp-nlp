#! /bin/tcsh

# for NomBank release 0.8

# This script splits the prop file containing the NomBank annotations for the whole WSJ TreeBank 
#  into many files, one for each file of the TreeBank. 
# The WSJ TreeBank, "mrg" distribution, is assumed to be under a directory named WSJ. 
# Only CoNLL sections are considered, ie. 02-21, 23 and 24


# path to the file containing all NomBank propositions and arguments
set propfile = nombank.0.8-release/nombank.0.8.gz


if (! -d NomBank-Args ) then
   echo Creating directory NomBank-Args
   mkdir NomBank-Args
endif  

foreach d ( WSJ/0[2-9] WSJ/1[0-9] WSJ/2[0134] ) 
  set s = $d:t
  mkdir NomBank-Args/$s
  foreach f ( WSJ/$s/* )
     set n = `echo $f:t:r:r | sed 's/wsj_//'`
     echo WSJ/$s/wsj_$n.mrg 
     zcat $propfile | grep wsj/$s/wsj_$n.mrg | sort -n -k 2 | gzip > NomBank-Args/$s/nb_$n.gz
  end
end
     
