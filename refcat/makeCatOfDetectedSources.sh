#!/bin/bash

if [[ $# != 0 ]] ;
then
  echo "Usage: . makeCatOfDetectedSources.sh"
  exit -1
fi

# Set the OSA environment variables and paths
. $HOME/integral/pipeline/osa11.setenv.sh
linkname=$(echo $ISDC_REF_CAT | cut -d"[" -f1)
filename=$(ls -l $linkname | cut -d">" -f2 | awk '{print $1}')
suffix=$(echo $filename | cut -d"/" -f8 | cut -d"[" -f1 | cut -d"_" -f4)

# ISGRI
outfile1=isgri_cat_$suffix
outfile2=isgri_cat_inMosa_$suffix
outfile3=isgri_cat_inScw_$suffix
outfile4=isgri_cat_bright_$suffix
outfile5=isgri_cat_strong_$suffix
ftcopy "$ISDC_REF_CAT[ISGRI_FLAG2 > 0]" !$outfile1
ftcopy "$ISDC_REF_CAT[ISGRI_FLAG2 == 2]" !$outfile2
ftcopy "$ISDC_REF_CAT[ISGRI_FLAG2 == 1]" !$outfile3
ftcopy "$ISDC_REF_CAT[ISGRI_FLAG2 == 5]" !$outfile4
ftcopy "$ISDC_REF_CAT[ISGR_FLUX_1 > 1 || ISGR_FLUX_2 > 1]" !$outfile5

# SPI and JEM-X
outfile5=spi_cat_$suffix
outfile6=jemx_cat_$suffix
ftcopy "$ISDC_REF_CAT[SPI_FLAG == 1]" !$outfile5
ftcopy "$ISDC_REF_CAT[JEMX_FLAG == 1]" !$outfile6

# Make ds9 region files
#. makeReg.sh $outfile1
#. makeReg.sh $outfile2
#. makeReg.sh $outfile3
#. makeReg.sh $outfile4
#. makeReg.sh $outfile5
#. makeReg.sh $outfile6

# Clean up
$HOME/integral/bin/rmlinks

# Note on ISGRI_FLAG and ISGRI_FLAG2
# ISGRI_FLAG=1 : source is detected by ISGRI
# ISGIR_FLAG=2 : source is detected by ISGRI AND position is known to < 3"
# ISGRI_FLAG2=1 : source is detected in scw (i.e., strong)
# ISGRI_FLAG2=2 : source is detected in mosaic (i.e., weak+strong+bright)
# ISGRI_FLAG2=5 : source is bright > 600 mCrab (125 sources)
