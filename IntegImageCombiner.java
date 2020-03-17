package gb.esac.integral;


import nom.tam.util.ArrayFuncs;
import org.apache.log4j.Logger;


public final class IntegralImageCombiner {

    private static Logger logger  = Logger.getLogger(IntegralImageCombiner.class);

    public static float[][] sumIntensityImages(float[][] imageData1, float[][] imageData2) {
	//  Get dimensions of input images
	int[] dimsOfIma1 = ArrayFuncs.getDimensions(imageData1);
	int[] dimsOfIma2 = ArrayFuncs.getDimensions(imageData2);
	//  Define size of summed image
	int ncols = Math.max(dimsOfIma1[0], dimsOfIma2[0]);
	int nrows = Math.max(dimsOfIma1[1], dimsOfIma2[1]);
	float[][] summedIntensity = new float[ncols][nrows];
	//  Sum intensities
	for ( int col=0; col < ncols; col++ ) {
	    for ( int row=0; row < nrows; row++ ) {
		if ( Float.isNaN(imageData1[col][row]) ) {
		    summedIntensity[col][row] = imageData2[col][row];
		}
		else if ( Float.isNaN(imageData2[col][row]) ) {
		    summedIntensity[col][row] = imageData1[col][row];
		}
		else {
		    summedIntensity[col][row] = imageData1[col][row] + imageData2[col][row];
		}
	    }
	}
	return summedIntensity;
    }

    public static float[][] sumVarianceImages(float[][] varianceData1, float[][] varianceData2) {
	//  Get dimensions of input images
	int[] dimsOfIma1 = ArrayFuncs.getDimensions(varianceData1);
	int[] dimsOfIma2 = ArrayFuncs.getDimensions(varianceData2);
	//  Define size of summed image
	int ncols = Math.max(dimsOfIma1[0], dimsOfIma2[0]);
	int nrows = Math.max(dimsOfIma1[1], dimsOfIma2[1]);
	float[][] combinedVariance = new float[ncols][nrows];
	//  Combine variances
	for ( int col=0; col < ncols; col++ ) {
	    for ( int row=0; row < nrows; row++ ) {
		if ( Float.isNaN(varianceData1[col][row]) ) {
		    combinedVariance[col][row] = varianceData2[col][row];
		}
		else if ( Float.isNaN(varianceData1[col][row]) ) {
		    combinedVariance[col][row] = varianceData1[col][row];
		}
		else {
		    combinedVariance[col][row] = varianceData1[col][row] + varianceData2[col][row];
		}
	    }
	}
	return combinedVariance;
    }

    public static float[][] sumSignificanceImages(float[][] signifData1, float[][] signifData2) {
	//  Get dimensions of input images
	int[] dimsOfIma1 = ArrayFuncs.getDimensions(signifData1);
	int[] dimsOfIma2 = ArrayFuncs.getDimensions(signifData2);
	//  Define size of summed image
	int ncols = Math.max(dimsOfIma1[0], dimsOfIma2[0]);
	int nrows = Math.max(dimsOfIma1[1], dimsOfIma2[1]);
	float[][] combinedSignif = new float[ncols][nrows];
	//  Sum intensities
	for ( int col=0; col < ncols; col++ ) {
	    for ( int row=0; row < nrows; row++ ) {
		if ( Float.isNaN(signifData1[col][row]) ) {
		    combinedSignif[col][row] = signifData2[col][row];
		}
		else if ( Float.isNaN(signifData2[col][row]) ) {
		    combinedSignif[col][row] = signifData1[col][row];
		}
		else {
		    double data1Sqrd = Math.pow(signifData1[col][row], 2);
		    double data2Sqrd = Math.pow(signifData2[col][row], 2);
		    combinedSignif[col][row] = (float) Math.sqrt(data1Sqrd+data2Sqrd);
		}
	    }
	}
	return combinedSignif;
    }

}
