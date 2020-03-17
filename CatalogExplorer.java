package gb.esac.integral;

import gb.esac.tools.FitsUtils;
import gb.esac.io.AsciiDataFileWriter;
import gb.esac.binner.Binner;

import hep.aida.IHistogram1D;
import nom.tam.fits.BinaryTableHDU;
import nom.tam.fits.Fits;
import nom.tam.util.BufferedDataInputStream;


public class CatalogExplorer {

    static String FILENAME = "refcat/isgri_cat_0042.fits";

    public static void main(String[] args) throws Exception {

	double min = 1, max = 100;
	int bins = 100;
	if (args.length == 3) {
	    min = Double.valueOf(args[0]).doubleValue();
	    max = Double.valueOf(args[1]).doubleValue();
	    bins = Integer.valueOf(args[2]).intValue();	
	}
	
	//  Read the data from the catalog
	Fits f = FitsUtils.openFits(FILENAME);
	BinaryTableHDU hdu = (BinaryTableHDU) f.getHDU(1);
	String[] source_id = (String[]) hdu.getColumn("SOURCE_ID");
	String[] name = (String[]) hdu.getColumn("NAME");
	//byte[] classType = (byte[]) hdu.getColumn("CLASS");
	float[] ra_obj = (float[]) hdu.getColumn("RA_OBJ");
	float[] dec_obj = (float[]) hdu.getColumn("DEC_OBJ");
	float[] err_rad = (float[]) hdu.getColumn("ERR_RAD");
	byte[] isgri_flag = (byte[]) hdu.getColumn("ISGRI_FLAG");
	byte[] isgri_flag2 = (byte[]) hdu.getColumn("ISGRI_FLAG2");
	byte[] jemx_flag2 = (byte[]) hdu.getColumn("JEMX_FLAG");
	byte[] spi_flag2 = (byte[]) hdu.getColumn("SPI_FLAG");
	byte[] picsit_flag2 = (byte[]) hdu.getColumn("PICSIT_FLAG");	
	float[] spi_flux_1 = (float[]) hdu.getColumn("SPI_FLUX_1");
	float[] spi_flux_2 = (float[]) hdu.getColumn("SPI_FLUX_2");	
	float[] isgri_flux_1 = (float[]) hdu.getColumn("ISGR_FLUX_1");
	float[] isgri_flux_2 = (float[]) hdu.getColumn("ISGR_FLUX_2");	
	float[] pics_flux_1 = (float[]) hdu.getColumn("PICS_FLUX_1");
	float[] pics_flux_2 = (float[]) hdu.getColumn("PICS_FLUX_2");
	float[] jemx_flux_1 = (float[]) hdu.getColumn("JEMX_FLUX_1");
	float[] jemx_flux_2 = (float[]) hdu.getColumn("JEMX_FLUX_2");	

	//  Make histogram of isgri_flux
	IHistogram1D histo_isgri_flux = Binner.makeHisto(isgri_flux_1,min,max,bins);
	AsciiDataFileWriter out = new AsciiDataFileWriter("histo_isgri_flux.qdp");
	out.writeHisto(histo_isgri_flux, "ISGRI Count Rate (cps)");
	
    }

}
