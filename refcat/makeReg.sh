#!/bin/bash

if [[ $# != 1 ]] ;
then
  echo "Usage: . makeReg.sh genRefCat.fits"
  return -1
fi
infile=$1
outfile=`echo $infile | sed s/".fits"/".reg"/g`
# . $HOME/integral/pipeline/osa.setenv.sh
cat2ds9 catDOL=${infile}'[GNRL-REFR-CAT]' symbol=cross color=yellow ploterr=n raoff=0 decoff=0 fileName=$outfile
# . $HOME/integral/bin/rmlinks
# rm log
