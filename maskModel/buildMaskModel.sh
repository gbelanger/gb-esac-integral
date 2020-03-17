#!/bin/bash

# Define filenames
mask="mask-47x47.txt"
flipX="mask-47x47-flipX.txt"
flipY="mask-47x47-flipY.txt"
flipXY="mask-47x47-flipXY.txt"

# Make sure there are no blank lines in mother file mask-47x47.txt
sed '/^$/d' mask-47x47.txt > tmp
mv tmp $mask


# Construct mask reflections
echo Constructing symmetric reflections of mask pattern ...
flipAlongX.sh $mask $flipX
flipAlongY.sh $mask $flipY
flipAlongY.sh $flipX $flipXY


# Construct FITS images
fitsmask="mask-47x47.fits"
fitsflipX="mask-47x47-flipX.fits"
fitsflipY="mask-47x47-flipY.fits"
fitsflipXY="mask-47x47-flipXY.fits"

fimgcreate 16 47,47 $mask $fitsmask clobber=yes
fimgcreate 16 47,47 $flipX $fitsflipX clobber=yes
fimgcreate 16 47,47 $flipY $fitsflipY clobber=yes
fimgcreate 16 47,47 $flipXY $fitsflipXY clobber=yes


# Create the empty 95x95 mask
echo Constructing 95x95 pixel mask ...
fitsfullmaskTemp="mask-95x95-template.fits"
fimgcreate 16 95,95 none $fitsfullmaskTemp clobber=yes


# Construct a file with a single col of 95 ones for the central col of the mask
centralCol="centralCol.txt"
if [ -f $centralCol ]
then
    /bin/rm $centralCol
fi

i=0
while ((i < 95))
do
    echo 1 >> $centralCol
    i=$((i+1))
done

fitscentralCol="centralCol.fits"
fimgcreate 16 1,95 $centralCol $fitscentralCol clobber=yes
/bin/rm $centralCol

# Construct list of files to be merged with x,y offsets

if [ -f filelist ]
then
    /bin/rm filelist
fi

# The mother pattern goes in the top right quadrant
#
#     flipY   1   mask
#               1
#     000001000000
#               1
#    flipXY  1   flipX
#
echo $fitsmask,48,48 >> filelist
echo $fitsflipX,48,0 >> filelist
echo $fitsflipY,0,48 >> filelist
echo $fitsflipXY,0,0 >> filelist
echo $fitscentralCol,47,0 >> filelist

# Merge the files
echo Merging the different parts of the mask ...
fitsfullmask="mask-95x95.fits"
fimgmerge $fitsfullmaskTemp @filelist $fitsfullmask clobber=yes
/bin/rm $fitsfullmaskTemp
/bin/rm filelist


# Pad the mask to 159x159 as in pipeline
echo Padding with zero to make mask 159x159 pixels
temp="temp.fits"
fimgcreate 16 159,159 none $temp clobber=yes
echo $fitsfullmask,32,32 >> filelist
fitsPaddedMask="mask-159x159.fits"
fimgmerge $temp @filelist $fitsPaddedMask clobber=yes
/bin/rm $temp
/bin/rm filelist


# Scale the mask pixels to ISGRI detector pixel size
echo Scaling from 95x95 to 231x231 ...

# mask elements = 11.2 x 11.2 mm
# pixel size (centre to centre) = 4.6 x 4.6 mm
# ratio is 11.2 / 4.6 = 2.43478 
# 95*2.43478 = 231.304
# But we use 2.43158 instead of 2.43478 to have exactly 231 pixels

ratio=2.43158

# Perform the mapping of the mask from 95 to 231 pixels

mask="mask-95x95-doNotDelete.txt"
scaledMask="mask-231x231.txt"
if [ -f $scaledMask ]
then
   /bin/rm $scaledMask
fi

n=231
i=1
j=1
while ((i <= 231))
do
    row_index=`calc int\($i/$ratio\)+1`
    while ((j <= 231))
    do
	col_index=`calc int\($j/$ratio\)+1`
	element=`head -$row_index $mask | tail -1 | awk '{print $col_index}'`
	line=$line" $element"
	j=$((j+1))
    done
    echo $line >> $scaledMask
    i=$((i+1))
done

fitsScaledMask="mask-231x231.fits"
fimgcreate 16 231,231 $scaledMask $fitsScaledMask clobber=yes

temp="temp.fits"
fimgcreate 16 400,400 none $temp clobber=yes

if [ -f filelist ]
then
   /bin/rm filelist
fi

echo $fitsScaledMask,85,85 > filelist

fitsFullMask="mask-400x400.fits"
fimgmerge $temp @filelist $fitsFullMask clobber=yes

echo 
