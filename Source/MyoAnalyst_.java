import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import filters.CoherenceEnhancingDiffusionFilter;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.measure.ResultsTable;
import ij.plugin.ImageCalculator;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import ij.process.AutoThresholder;
import mpicbg.ij.clahe.Flat;
public class MyoAnalyst_ implements PlugIn {
	private static final int BLACK_AND_WHITE_LUT = 0;
	private static final int NO_LUT_UPDATE = 0;
	private JButton inputFolder, outputFolder, runner, cancel;
	private JTextField inputFolderField, outputFolderField;
	private JLabel inputFolderLabel, outputFolderLabel, sampleType;
	private JComboBox sampleChoiceBox;
	private static String[] sampleChoice = { "H&E", "IF"};
	static String DirIn = "";
	static String DirOut = "";
	String he2 = "H&E*2,0.490157340,0.768970850,0.410401730,0.04615336,0.84206840,0.53739250,0.76328504,0.001,0.64606184";
	String he = "H&E,0.6443186, 0.7166757, 0.26688856, 0.09283128, 0.9545457, 0.28324,0.63595444,0.0.001,0.7717266";
	@SuppressWarnings("unchecked")
	@Override
	public void run(String arg) {
		// TODO Auto-generated method stub
		GenericDialog od = new GenericDialog("MyoAnalyst");
		sampleChoiceBox = new JComboBox(sampleChoice);
		Panel inputPanel, samplePanel,  outputPanel, helpPanel;
		// help panel
		helpPanel = new Panel();
		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("Help");
		JMenuItem about = new JMenuItem("About...");
		JMenuItem help = new JMenuItem("How to Use ...");
		ActionListener snd = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				String AboutMessage = new String();
				AboutMessage = "MyoAnalyst v1.0";
				JOptionPane.showMessageDialog(null, AboutMessage, "MyoAnalyst",
						JOptionPane.INFORMATION_MESSAGE);

			}

		};
		about.addActionListener(snd);
		menu.add(about);
		menu.add(help);
		menuBar.add(menu);
		helpPanel.add(menuBar);

		// input folder panel
		inputFolderLabel = new JLabel("Input Folder:    ");
		inputFolderField = new JTextField(DirIn, 11);
		inputPanel = new Panel();
		inputPanel.setLayout(new GridLayout(1, 3));
		inputFolder = new JButton("SELECT");
		inputPanel.add(inputFolderLabel);
		inputPanel.add(inputFolderField);
		inputPanel.add(inputFolder);
		
		// sample type pane1
		sampleType = new JLabel("Sample Type:    ");
		samplePanel = new Panel();
		samplePanel.setLayout(new GridLayout(1, 2, 5, 5));//1,2,5,5
		samplePanel.add(sampleType);
		samplePanel.add(sampleChoiceBox);

		// output folder panel
		outputFolderLabel = new JLabel("Output Folder:    ");
		outputFolderField = new JTextField(DirOut, 11);
		outputPanel = new Panel();
		outputPanel.setLayout(new GridLayout(1, 3));
		outputFolder = new JButton("SELECT");
		outputPanel.add(outputFolderLabel);
		outputPanel.add(outputFolderField);
		outputPanel.add(outputFolder);

		od.addPanel(helpPanel, GridBagConstraints.EAST, new Insets(5, 0, 0, 25));
		od.addPanel(inputPanel);
		od.addPanel(samplePanel);
		od.addPanel(outputPanel);
		JButton run = new JButton("Run");

		// to use for button event handling
		ButtonHandler handler = new ButtonHandler();
		inputFolder.addActionListener(handler);
		outputFolder.addActionListener(handler);

		od.showDialog();

		if (od.wasCanceled()) {
			return;
		} else {
			imageProcessor((String) sampleChoiceBox.getSelectedItem());
		}
	}

	public void imageProcessor(String sample) {
		if (sample.equals("H&E")) {
			HE2(DirIn, DirOut);
		} else {
			ImmunoFluorescent(DirIn, DirOut);
		} 
	}

	public void HE2(String dir1, String dir2) {
		IJ.resetEscape();
		if (!dir2.endsWith(File.separator)) {
			dir2 += File.separator;
		}
		String[] list = new File(dir1).list();
		if (list == null) {
			return;
		}
		for(int i = 0; i<list.length; i++) {
			if (IJ.escapePressed()) {
				break;
			}
			String i_AS_STRING = Integer.toString(i+1);
			String Len_AS_STRING = Integer.toString(list.length);
			System.out.println("HE staining: Analyzing the "+i_AS_STRING+"/"+Len_AS_STRING+" image: " + list[i]);
			File f = new File(dir1 + File.separator + list[i]);
			if (!f.isDirectory() && (list[i].endsWith("jpeg") || list[i].endsWith("JPEG") || list[i].endsWith("jpg")
					|| list[i].endsWith("JPG") || list[i].endsWith("bmp") || list[i].endsWith("BMP")
					|| list[i].endsWith("png") || list[i].endsWith("PNG") 
					|| list[i].endsWith("gif") || list[i].endsWith("GIF") || list[i].endsWith("tiff")
					|| list[i].endsWith("TIFF") || list[i].endsWith("tif") || list[i].endsWith("TIF")
					|| list[i].endsWith("pgm") || list[i].endsWith("PGM"))) {
				ImagePlus img1 = new ImagePlus(dir1 + File.separator + list[i]);
				String t1 = img1.getTitle();
				int w1 = img1.getWidth();
				int h1 = img1.getHeight();
				System.out.println("Do Subtract Background...");
				IJ.run(img1, "Subtract Background...", "rolling=50 light");
				//img1.show();
				if (img1.getBitDepth()!=24){
				      IJ.error("RGB image needed.");
				      return;
				    }
				//stain decomposition and get the eosinophilic image;
				System.out.println("Do Color Deconvolution...");
				StainMatrix mt = new StainMatrix();
				mt.init(he2);
				ImageStack[] img1_stack= mt.compute(false, true, img1);
				ImagePlus img1_stack2=new ImagePlus(t1+"-(Colour_1",img1_stack[1]);
				//IJ.saveAs(img1_stack2, "jpg", "image1_after_color_deconvolution");
			    ///*
				//performing enhance local contrast(CLAHE);
				System.out.println("Do Enhance local contrast(CLAHE)...");
				mpicbg.ij.clahe.Flat.getInstance().run(img1_stack2, 127, 256, 3.0f, null, true);//127,256,3.0
				//performing coherence-enhancing diffusion(CED);
				System.out.println("Do Coherence Enhancing Diffusion...");
				FloatProcessor fp1=(FloatProcessor) img1_stack2.getProcessor().convertToFloat();
				CoherenceEnhancingDiffusionFilter cedf=new CoherenceEnhancingDiffusionFilter();
				FloatProcessor fp2=cedf.run(fp1);
				ByteProcessor fp3=fp2.convertToByteProcessor();
				ImagePlus img4=new ImagePlus("cedf2",fp3);
				for (int j = 0; j < 3; j++) {
					IJ.run(img4, "Despeckle", "");
				}
				//IJ.saveAs(img4, "jpg", "image2_after_enhancement");
				AutoThreshold at=new AutoThreshold();
				int miu=at.computeMiu(fp3);
				fp3.setThreshold(miu, 255, NO_LUT_UPDATE);
				ImagePlus img5=new ImagePlus("cedf1",fp3);
				IJ.run(img5, "Convert to Mask","");
				fp3.invert();
				IJ.run(img5, "Erode", "");
				IJ.run(img5, "Erode", "");
				IJ.run(img5, "Fill Holes", "");
				fp3.invert();
				ImagePlus img_final=new ImagePlus("final",fp3);
				// same as ImmunoFluorence process;
				System.out.println("Do Outline...");
				IJ.run(img_final, "Outline", "");
				System.out.println("Do Skeletonize...");
				IJ.run(img_final, "Skeletonize", "");
				System.out.println("Do Invert...");
				ImageProcessor ip1 = img_final.getProcessor();
				ip1.invert();
				//IJ.saveAs(img_final, "jpg", "image3_after_morphProcess");
				//img_final.show();
				System.out.println("Do Clear Results");
				IJ.run("Clear Results", "");
				// do the first segmentation
				System.out.println("Do 1st segmentation...");
				MyoAnalyst_ seg1 = new MyoAnalyst_();
				ImagePlus imgs1 = seg1.segmentation(img_final, 0, 20);//60;20
				ImageProcessor ip2 = imgs1.getProcessor();
				ip2.invert();
				int Dil_num_half = Math.round(w1*h1/20000000);
				if (Dil_num_half<=4) {
					for (int j = 0; j < 6; j++) {
						IJ.run(imgs1, "Dilate", "");
						}
				}else {
					for (int j = 0; j < Dil_num_half*2; j++) {
						IJ.run(imgs1, "Dilate", "");
						}
				}
				//for (int j = 0; j < 8; j++) {
				//		IJ.run(imgs1, "Dilate", "");
				//	}
				ip2.invert();
				IJ.run("Clear Results", "");
				// do the second segmentation
				System.out.println("Do 2rd segmentation...");
				ImagePlus imgs2 = seg1.segmentation(imgs1, 0, 40);//40;30
				imgs2.setTitle(list[i] + ".results");
				String r2 = imgs2.getTitle();
				System.out.println("Process done!");
				ImageProcessor ip3 = imgs2.getProcessor();
				ip3.invert();
				if (Dil_num_half<=4) {
					for (int j = 0; j < 5; j++) {
						IJ.run(imgs2, "Erode", "");
						}
				}else {
					for (int j = 0; j < Dil_num_half+2; j++) {
						IJ.run(imgs2, "Erode", "");
						}
				}
				//for (int j = 0; j < 5; j++) {
				//		IJ.run(imgs2, "Erode", "");
				//	}
				ip3.invert();
				//IJ.saveAs(imgs2, "jpg", "image3_after_segmentation");
				IJ.run("Clear Results", "");
				// analyze muscle fibres size and save the quantification data
				IJ.run(imgs2, "Analyze Particles...", "size=250-100000 circularity=0.3-1.00 display clear add exclude");//size=500-100000 circularity=0.25-1.00;size=2500-100000 circularity=0.3-1.00
				ResultsTable rt = ResultsTable.getResultsTable();
				rt.save(dir2 + r2 + ".csv");
				if (WindowManager.getFrame("Results") != null) {
					IJ.selectWindow("Results");
					IJ.run("Close");
				}
				RoiManager rm = RoiManager.getInstance();
				int[] indx = rm.getIndexes();
				rm.setSelectedIndexes(indx);
				rm.runCommand("Save", dir2 + r2 + "_RoiSet.zip");
				if (WindowManager.getFrame("ROI Manager") != null) {
					IJ.selectWindow("ROI Manager");
					IJ.run("Close");
				}
			}else {
				System.out.println(list[i]+"is not a image!");
				continue;
			}
			System.out.println(i_AS_STRING+"/"+Len_AS_STRING+" Done!");
		}
		System.out.println("all HE images are Done!");
	}
	public void ImmunoFluorescent(String dir1, String dir2) {
		IJ.resetEscape();
		if (!dir2.endsWith(File.separator)) {
			dir2 += File.separator;
		}
		String[] list = new File(dir1).list();
		if (list == null) {
			return;
		}
		for (int i = 0; i < list.length; i++) {
			if (IJ.escapePressed()) {
				break;
			}
			String i_AS_STRING = Integer.toString(i+1);
			String Len_AS_STRING = Integer.toString(list.length);
			System.out.println("IF: Analyzing the "+i_AS_STRING+"/"+Len_AS_STRING+" image: " + list[i]);
			File f = new File(dir1 + File.separator + list[i]);
			if (!f.isDirectory() && (list[i].endsWith("jpeg") || list[i].endsWith("JPEG") || list[i].endsWith("jpg")
					|| list[i].endsWith("JPG") || list[i].endsWith("bmp") || list[i].endsWith("BMP")
					|| list[i].endsWith("png") || list[i].endsWith("PNG") 
					|| list[i].endsWith("gif") || list[i].endsWith("GIF") || list[i].endsWith("tiff")
					|| list[i].endsWith("TIFF") || list[i].endsWith("tif") || list[i].endsWith("TIF")
					|| list[i].endsWith("pgm") || list[i].endsWith("PGM"))) {
				ImagePlus img1 = new ImagePlus(dir1 + File.separator + list[i]);
				int w1 = img1.getWidth();
				int h1 = img1.getHeight();
				System.out.println("Do Enhance local contrast(CLAHE)...");
				mpicbg.ij.clahe.Flat.getFastInstance().run(img1, 127, 256, 3.0f, null, true);//127,256,3.0
				// performing coherence-enhancing diffusion(CED);
				System.out.println("Do Coherence Enhancing Diffusion...");
				FloatProcessor fp1=(FloatProcessor) img1.getProcessor().convertToFloat();
				CoherenceEnhancingDiffusionFilter cedf=new CoherenceEnhancingDiffusionFilter();
				FloatProcessor fp2=cedf.run(fp1);
				ByteProcessor fp3=fp2.convertToByteProcessor();
				ImagePlus img4=new ImagePlus("cedf2",fp3);
				for (int j = 0; j < 3; j++) {
					IJ.run(img4, "Despeckle", "");
				}
				//IJ.saveAs(img4, "jpg", "image2_after_enhancement");
				AutoThreshold at=new AutoThreshold();
				int miu=at.computeMiu(fp3);
				fp3.setThreshold(miu, 255, NO_LUT_UPDATE);
				ImagePlus img5=new ImagePlus("cedf1",fp3);
				IJ.run(img5, "Convert to Mask","");
				System.out.println("Do Skeletonize...");
				IJ.run(img5, "Skeletonize", "");
				System.out.println("Do Invert...");
				ImageProcessor ip0 = img5.getProcessor();
				ip0.invert();
				//IJ.saveAs(img5, "jpg", "image3_after_morphProcess");
				System.out.println("Do Clear Results");
				IJ.run("Clear Results", "");
				// do the first segmentation
				System.out.println("Do 1st segmentation...");
				MyoAnalyst_ seg1 = new MyoAnalyst_();
				ImagePlus imgs1 = seg1.segmentation(img5, 0, 20);//60;20
				ImageProcessor ip1 = imgs1.getProcessor();
				ip1.invert();
				int Dil_num_half = Math.round(w1*h1/150000000);
				if (Dil_num_half<=4) {
					for (int j = 0; j < 2; j++) {
						IJ.run(imgs1, "Dilate", "");
						}
				}
				if (Dil_num_half>=8) {
					for (int j = 0; j < 8; j++) {
						IJ.run(imgs1, "Dilate", "");
						}
				}
				else {
					for (int j = 0; j < Dil_num_half; j++) {
						IJ.run(imgs1, "Dilate", "");
						}
				}
				ip1.invert();
				// do the second segmentation
				IJ.run("Clear Results", "");
				// do the second segmentation
				System.out.println("Do 2rd segmentation...");
				ImagePlus imgs2 = seg1.segmentation(imgs1, 0, 40);//40;20
				imgs2.setTitle(list[i] + ".results");
				String r2 = imgs2.getTitle();
				System.out.println("Process done!");
				ImageProcessor ip2 = imgs2.getProcessor();
				ip2.invert();
				if (Dil_num_half<=4) {
					for (int j = 0; j < 1; j++) {
						IJ.run(imgs2, "Dilate", "");
						}
				}if (Dil_num_half>=8) {
					for (int j = 0; j < 4; j++) {
						IJ.run(imgs1, "Dilate", "");
						}
				}else {
					for (int j = 0; j < 3; j++) {
						IJ.run(imgs2, "Dilate", "");
						}
				}
				ip2.invert();
				//IJ.saveAs(imgs2, "jpg", "image3_after_segmentation");
				IJ.run("Clear Results", "");
				// analyze muscle fibres size and save the quantification data
				IJ.run(imgs2, "Analyze Particles...", "size=250-100000 circularity=0.25-1.00 display clear add exclude");//size=500-100000 circularity=0.25-1.00;size=2500-100000 circularity=0.3-1.00
				ResultsTable rt = ResultsTable.getResultsTable();
				rt.save(dir2 + r2 + ".csv");
				if (WindowManager.getFrame("Results") != null) {
					IJ.selectWindow("Results");
					IJ.run("Close");
				}
				RoiManager rm = RoiManager.getInstance();
				int[] indx = rm.getIndexes();
				rm.setSelectedIndexes(indx);
				rm.runCommand("Save", dir2 + r2 + "_RoiSet.zip");
				if (WindowManager.getFrame("ROI Manager") != null) {
					IJ.selectWindow("ROI Manager");
					IJ.run("Close");
				}
			}else {
				System.out.println(list[i]+"is not a image!");
				continue;
			}
			System.out.println(i_AS_STRING+"/"+Len_AS_STRING+" Done!");
		}
		System.out.println("all IF images are Done!");
	}

	public ImagePlus segmentation(ImagePlus img, int user_min, int user_max) {
		ImagePlus img2 = img.duplicate();
		img2.setTitle("ttt");
		System.out.println("Do Watershed...");
		IJ.run(img2, "Watershed", "");
		System.out.println("Do Image Calculator...");
		ImageCalculator ic1 = new ImageCalculator();
		ImagePlus img3 = ic1.run("Subtract create", img, img2);
		img3.setTitle("cuts");
		ImagePlus img4 = img3.duplicate();
		img4.setTitle("skels");
		System.out.println("Do Lines8...");
		Lines8 l8_1 = new Lines8();
		l8_1.exec(img4, null, true, true, false, false, false, "Lines", true, 0, 9999999, true, true, "None");
		// ImagePlus imp, ImagePlus imp2, boolean doIwhite, boolean doISkel, boolean
		// doIdeleteborders, boolean doIlabel, boolean doImorpho, String selectedOption,
		// boolean doIminmax, int mi,int ma, boolean doIshowstats, boolean doIoverwrite,
		// String redirected
		System.out.println("get the results table...");
		ResultsTable rt1 = ResultsTable.getResultsTable();
		int n = rt1.getCounter();
		System.out.println(n);
		double[] sl = new double[n];
		int[] xs = new int[n];
		int[] ys = new int[n];
		// get the length
		for (int i = 0; i < n; i++) {
			sl[i] = rt1.getValue("SkelLength", i);
			xs[i] = (int) rt1.getValue("XStart", i);
			ys[i] = (int) rt1.getValue("YStart", i);
		}
		IJ.run(img4, "Set...", "value=0");
		ImageProcessor ip1 = img4.getProcessor();
		for (int j = 0; j < n; j++) {
			if ((sl[j] >= user_min) && (sl[j] <= user_max)) {
				ip1.set(xs[j], ys[j], 255);
			}
		}
		System.out.println("Do the BinaryReconstruction...");
		BinaryReconstruct br = new BinaryReconstruct();
		Object[] result = br.exec(img3, img4, null, false, true, false);
		// parameters above are: mask ImagePlus, seed ImagePlus, name, create new image,
		// white particles, connect4
		ImagePlus recons = (ImagePlus) result[1];
		recons.setTitle("after BR");
		ImageCalculator ic2 = new ImageCalculator();
		ImagePlus img5 = ic2.run("Subtract create", img, recons);
		img5.setTitle("after IC");
		System.out.println("Segmentation finished!");
		img.close();
		img2.close();
		img3.close();
		img4.close();
		recons.close();
		return img5;
	}

	private class ButtonHandler implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			if (e.getSource() == inputFolder) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setDialogTitle("Input Folder");
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

				int results = fileChooser.showOpenDialog(null);
				DirIn = fileChooser.getSelectedFile() + "";

				if (DirIn != null + "") {
					inputFolderField.setText(fileChooser.getSelectedFile() + "");
					inputFolderField.repaint();
				}
			} else if (e.getSource() == outputFolder) {
				JFileChooser fileChooserOutput = new JFileChooser();
				fileChooserOutput.setDialogTitle("Input Folder");
				fileChooserOutput.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

				int results = fileChooserOutput.showOpenDialog(null);
				DirOut = fileChooserOutput.getSelectedFile() + "";

				if (DirOut != null + "") {
					outputFolderField.setText(fileChooserOutput.getSelectedFile() + "");
					outputFolderField.repaint();
				}
			}

		}

	}

	
	public static void main(String[] args) {
		Class<?> clazz = MyoAnalyst_.class;
		String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
		String pluginsDir = url.substring("file:".length(),
				
				url.length() - clazz.getName().length() - ".class".length());
		System.setProperty("plugins.dir", pluginsDir);
		// start ImageJ
	   new ImageJ();
		IJ.runPlugIn(clazz.getName(), "");
	}
	
}
